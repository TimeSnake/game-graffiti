/*
 * Copyright (C) 2022 timesnake
 */

package de.timesnake.game.graffiti.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.UserInventoryInteractEvent;
import de.timesnake.basic.bukkit.util.user.event.UserInventoryInteractListener;
import de.timesnake.game.graffiti.item.AirPistol;
import de.timesnake.game.graffiti.item.Gun;
import de.timesnake.game.graffiti.item.PaintGun;
import de.timesnake.game.graffiti.main.GameGraffiti;
import de.timesnake.game.graffiti.user.GraffitiUser;
import de.timesnake.library.basic.util.Tuple;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;

public class PaintManager implements Listener, UserInventoryInteractListener {

    public static final PaintGun SNIPER_GUN = new PaintGun();
    public static final AirPistol AIR_PISTOL = new AirPistol();
    public static final ExItemStack PAINT_BALL = new ExItemStack(Material.EGG).setDropable(false).setDisplayName(
            "§6Paint Ball");
    public static final HashMap<Material, Material> BLUE_PAINT_MAP = new HashMap<>();
    public static final HashMap<Material, Material> RED_PAINT_MAP = new HashMap<>();
    public static final HashMap<Material, Material> WHITE_PAINT_MAP = new HashMap<>();

    public static void drawCircle(GraffitiUser user, Block center, Location origin, double radius, double density) {
        drawCircle(user, center, origin, radius, density, false);
    }

    public static void drawCircle(GraffitiUser user, Block center, Location origin, double radius, double density,
                                  boolean white) {
        Server.runTaskAsynchrony(() -> {
            Set<Block> blocksToPaint = new HashSet<>();

            Location middle = center.getLocation().add(0.5, 0.5, 0.5);

            Map<Material, Material> paintMap = white ? WHITE_PAINT_MAP :
                    user.getTeam().equals(GraffitiServer.getGame().getBlueTeam()) ? BLUE_PAINT_MAP : RED_PAINT_MAP;

            for (int x = (int) -radius; x <= radius; x++) {
                for (int y = (int) -radius; y <= radius; y++) {
                    for (int z = (int) -radius; z <= radius; z++) {

                        Block block = center.getRelative(x, y, z);
                        Location blockMiddle = block.getLocation().add(0.5, 0.5, 0.5);

                        if (middle.distanceSquared(blockMiddle) > radius) {
                            continue;
                        }

                        if (paintMap.get(block.getType()) == null) {
                            continue;
                        }

                        for (BlockFace face : BLOCK_FACES) {
                            RayTraceResult res = origin.getWorld().rayTraceBlocks(origin,
                                    blockMiddle.add(face.getDirection().multiply(0.5))
                                            .toVector().subtract(origin.toVector()),
                                    32, FluidCollisionMode.NEVER, true);

                            if (res == null) {
                                continue;
                            }

                            if (block.equals(res.getHitBlock())) {
                                if (density < 1 || RANDOM.nextDouble() < density) {
                                    blocksToPaint.add(block);

                                }
                                break;
                            }
                        }

                    }
                }
            }

            Server.runTaskSynchrony(() -> {
                for (Block block : blocksToPaint) {
                    block.setType(paintMap.get(block.getType()));
                }
            }, GameGraffiti.getPlugin());
        }, GameGraffiti.getPlugin());
    }

    private static final int SPRAY_RADIUS = 5;
    private static final double SPRAY_DENSITY = 1;
    private static final int COOLDOWN = 4;
    private static final int MAX_SPRAY_DISTANCE = 4;
    private static final double SPRAY_DAMAGE = 3;
    public static final ExItemStack BLUE_SPRAY_BOTTLE = new ExItemStack(Material.BLUE_DYE)
            .setDisplayName("§9Blue Spray Bottle").setLore("", "§fDamage: §7" + SPRAY_DAMAGE / 2 + " ❤", "§fPaint: §7"
                    + "blue, " + SPRAY_RADIUS + " blocks").setMoveable(false).setDropable(false).setSlot(0);
    public static final ExItemStack RED_SPRAY_BOTTLE = new ExItemStack(Material.RED_DYE)
            .setDisplayName("§cRed Spray Bottle").setLore("", "§fDamage: §7" + SPRAY_DAMAGE / 2 + " ❤", "§fPaint: §7"
                    + "red, " + SPRAY_RADIUS + " blocks")
            .setMoveable(false).setDropable(false).setSlot(0);
    private static final Random RANDOM = new Random();
    private static final List<BlockFace> BLOCK_FACES = List.of(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH,
            BlockFace.WEST, BlockFace.UP, BlockFace.DOWN);

    static {
        BLUE_PAINT_MAP.put(Material.WHITE_WOOL, Material.BLUE_WOOL);
        BLUE_PAINT_MAP.put(Material.RED_WOOL, Material.BLUE_WOOL);

        RED_PAINT_MAP.put(Material.WHITE_WOOL, Material.RED_WOOL);
        RED_PAINT_MAP.put(Material.BLUE_WOOL, Material.RED_WOOL);

        WHITE_PAINT_MAP.put(Material.BLUE_WOOL, Material.WHITE_WOOL);
        WHITE_PAINT_MAP.put(Material.RED_WOOL, Material.WHITE_WOOL);


        BLUE_PAINT_MAP.put(Material.WHITE_STAINED_GLASS_PANE, Material.BLUE_STAINED_GLASS_PANE);
        BLUE_PAINT_MAP.put(Material.RED_STAINED_GLASS_PANE, Material.BLUE_STAINED_GLASS_PANE);

        RED_PAINT_MAP.put(Material.WHITE_STAINED_GLASS_PANE, Material.RED_STAINED_GLASS_PANE);
        RED_PAINT_MAP.put(Material.BLUE_STAINED_GLASS_PANE, Material.RED_STAINED_GLASS_PANE);

        WHITE_PAINT_MAP.put(Material.BLUE_STAINED_GLASS_PANE, Material.WHITE_STAINED_GLASS_PANE);
        WHITE_PAINT_MAP.put(Material.RED_STAINED_GLASS_PANE, Material.WHITE_STAINED_GLASS_PANE);


        BLUE_PAINT_MAP.put(Material.WHITE_STAINED_GLASS, Material.BLUE_STAINED_GLASS);
        BLUE_PAINT_MAP.put(Material.RED_STAINED_GLASS, Material.BLUE_STAINED_GLASS);

        RED_PAINT_MAP.put(Material.WHITE_STAINED_GLASS, Material.RED_STAINED_GLASS);
        RED_PAINT_MAP.put(Material.BLUE_STAINED_GLASS, Material.RED_STAINED_GLASS);

        WHITE_PAINT_MAP.put(Material.BLUE_STAINED_GLASS, Material.WHITE_STAINED_GLASS);
        WHITE_PAINT_MAP.put(Material.RED_STAINED_GLASS, Material.WHITE_STAINED_GLASS);


        BLUE_PAINT_MAP.put(Material.WHITE_TERRACOTTA, Material.BLUE_TERRACOTTA);
        BLUE_PAINT_MAP.put(Material.RED_TERRACOTTA, Material.BLUE_TERRACOTTA);

        RED_PAINT_MAP.put(Material.WHITE_TERRACOTTA, Material.RED_TERRACOTTA);
        RED_PAINT_MAP.put(Material.BLUE_TERRACOTTA, Material.RED_TERRACOTTA);

        WHITE_PAINT_MAP.put(Material.BLUE_TERRACOTTA, Material.WHITE_TERRACOTTA);
        WHITE_PAINT_MAP.put(Material.RED_TERRACOTTA, Material.WHITE_TERRACOTTA);


        BLUE_PAINT_MAP.put(Material.WHITE_CONCRETE, Material.BLUE_CONCRETE);
        BLUE_PAINT_MAP.put(Material.RED_CONCRETE, Material.BLUE_CONCRETE);

        RED_PAINT_MAP.put(Material.WHITE_CONCRETE, Material.RED_CONCRETE);
        RED_PAINT_MAP.put(Material.BLUE_CONCRETE, Material.RED_CONCRETE);

        WHITE_PAINT_MAP.put(Material.BLUE_CONCRETE, Material.WHITE_CONCRETE);
        WHITE_PAINT_MAP.put(Material.RED_CONCRETE, Material.WHITE_CONCRETE);


        BLUE_PAINT_MAP.put(Material.WHITE_CONCRETE_POWDER, Material.BLUE_CONCRETE_POWDER);
        BLUE_PAINT_MAP.put(Material.RED_CONCRETE_POWDER, Material.BLUE_CONCRETE_POWDER);

        RED_PAINT_MAP.put(Material.WHITE_CONCRETE_POWDER, Material.RED_CONCRETE_POWDER);
        RED_PAINT_MAP.put(Material.BLUE_CONCRETE_POWDER, Material.RED_CONCRETE_POWDER);

        WHITE_PAINT_MAP.put(Material.BLUE_CONCRETE_POWDER, Material.WHITE_CONCRETE_POWDER);
        WHITE_PAINT_MAP.put(Material.RED_CONCRETE_POWDER, Material.WHITE_CONCRETE_POWDER);
    }

    private final Set<User> sprayBottleCooldownUsers = new HashSet<>();
    private final HashMap<ExItemStack, Gun> gunByItem = new HashMap<>();
    private final HashMap<String, Gun> gunByName = new HashMap<>();

    public PaintManager() {
        this.addGun(SNIPER_GUN);
        this.addGun(AIR_PISTOL);

        Server.getInventoryEventManager().addInteractListener(this, this.gunByItem.keySet());
        Server.getInventoryEventManager().addInteractListener(this, BLUE_SPRAY_BOTTLE);
        Server.getInventoryEventManager().addInteractListener(this, RED_SPRAY_BOTTLE);
        Server.registerListener(this, GameGraffiti.getPlugin());
    }

    public void addGun(Gun gun) {
        this.gunByItem.put(gun.getBlueItem(), gun);
        this.gunByItem.put(gun.getRedItem(), gun);
        this.gunByName.put(gun.getName(), gun);
    }

    @Override
    public void onUserInventoryInteract(UserInventoryInteractEvent event) {
        if (!GraffitiServer.isGameRunning()) {
            return;
        }

        GraffitiUser user = (GraffitiUser) event.getUser();

        ExItemStack item = event.getClickedItem();

        if (item.equals(BLUE_SPRAY_BOTTLE) || item.equals(RED_SPRAY_BOTTLE)) {
            if (!this.sprayBottleCooldownUsers.contains(user)) {
                this.sprayBottleCooldownUsers.add(user);

                Server.runTaskLaterSynchrony(() -> this.sprayBottleCooldownUsers.remove(user), COOLDOWN,
                        GameGraffiti.getPlugin());

                Location loc = user.getEyeLocation();
                org.bukkit.util.Vector dir = user.getLocation().getDirection();

                org.bukkit.util.Vector baseVec = dir.clone().rotateAroundY(Math.PI / 8);

                for (double rot = 0; rot < 2 * Math.PI; rot += Math.PI / 8) {
                    Vector sprayVec = baseVec.clone().rotateAroundAxis(dir, rot).normalize();

                    Particle.DustOptions dust = new Particle.DustOptions(user.getTeam().getColor(), 0.5f);
                    loc.getWorld().spawnParticle(Particle.REDSTONE, loc.getX(), loc.getY(), loc.getZ(), 6,
                            sprayVec.getX(), sprayVec.getY(), sprayVec.getZ(), 1, dust);
                }

                Block block = user.getTargetBlock(MAX_SPRAY_DISTANCE);

                if (block != null) {
                    drawCircle(user, block, loc, SPRAY_RADIUS, SPRAY_DENSITY);
                }

                Entity entity = user.getTargetEntity(MAX_SPRAY_DISTANCE);

                if (entity instanceof Player p) {
                    GraffitiUser hitUser = ((GraffitiUser) Server.getUser(p));

                    if ((user.getTeam().equals(GraffitiServer.getGame().getBlueTeam())
                            && !hitUser.getTeam().equals(GraffitiServer.getGame().getBlueTeam()))
                            || (!user.getTeam().equals(GraffitiServer.getGame().getBlueTeam())
                            && hitUser.getTeam().equals(GraffitiServer.getGame().getBlueTeam()))) {
                        hitUser.damage(SPRAY_DAMAGE, user.getPlayer());
                    }
                }

                user.getWorld().playSound(user.getLocation(), Sound.ITEM_BONE_MEAL_USE, 3, 1);
            }
        } else {
            Gun gun = this.gunByItem.get(item);

            if (gun != null) {
                gun.onInteract(user, event.getAction());
            }
        }

    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        if (!GraffitiServer.isGameRunning()) {
            return;
        }

        if (!(e.getEntity().getShooter() instanceof Player p)) {
            return;
        }

        GraffitiUser user = (GraffitiUser) Server.getUser(p);
        Projectile projectile = e.getEntity();
        Location origin = projectile.getOrigin();

        if (projectile.getCustomName() == null) {
            return;
        }

        if (e.getHitEntity() != null && e.getHitEntity() instanceof Player hitPlayer) {
            Gun gun = this.gunByName.get(projectile.getCustomName());

            GraffitiUser hitUser = (GraffitiUser) Server.getUser(hitPlayer);

            if (gun != null) {
                e.setCancelled(gun.onUserHit(user, hitUser, projectile));
            }
        }

        if (e.getHitBlock() != null) {
            Block hitBlock = e.getHitBlock();

            Gun gun = this.gunByName.get(projectile.getCustomName());

            if (gun != null) {
                e.setCancelled(gun.onBlockHit(user, hitBlock, projectile));
            }
        }
    }

    public Tuple<Integer, Integer> calcWinner() {
        int blueBlocks = 0;
        int redBlocks = 0;

        int mapSize = GraffitiServer.getMap().getMapSize();

        Location middle = GraffitiServer.getMap().getSpectatorSpawn();

        for (int x = -mapSize; x <= mapSize; x++) {
            for (int y = -mapSize; y <= mapSize; y++) {
                for (int z = -mapSize; z <= mapSize; z++) {
                    Block block = middle.getBlock().getRelative(x, y, z);

                    if (BLUE_PAINT_MAP.containsValue(block.getType())) {
                        blueBlocks++;
                    } else if (RED_PAINT_MAP.containsValue(block.getType())) {
                        redBlocks++;
                    }
                }
            }
        }

        return new Tuple<>(blueBlocks, redBlocks);
    }


}
