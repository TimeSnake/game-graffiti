package de.timesnake.game.graffiti.user;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.*;
import de.timesnake.game.graffiti.main.GameGraffiti;
import de.timesnake.game.graffiti.server.GraffitiServer;
import de.timesnake.game.graffiti.server.PaintManager;
import de.timesnake.library.basic.util.Status;
import de.timesnake.library.basic.util.chat.ExTextColor;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Chicken;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserManager implements Listener {

    private static final List<BlockFace> JUMP_BLOCK_FACES = List.of(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH,
            BlockFace.WEST);
    private static final List<Material> JUMP_BLOCK_TYPES = List.of(Material.WHITE_STAINED_GLASS,
            Material.BLUE_STAINED_GLASS,
            Material.RED_STAINED_GLASS);


    private final Map<User, BukkitTask> jumpTasksByUser = new HashMap<>();
    private BukkitTask damageTask;

    public UserManager() {
        Server.registerListener(this, GameGraffiti.getPlugin());
    }

    public void run() {
        this.damageTask = Server.runTaskTimerAsynchrony(() -> {
            for (User user : Server.getInGameUsers()) {
                Map<Material, Material> paintMap;
                if (((GraffitiUser) user).getTeam().equals(GraffitiServer.getGame().getBlueTeam())) {
                    paintMap = PaintManager.RED_PAINT_MAP;
                } else {
                    paintMap = PaintManager.BLUE_PAINT_MAP;
                }

                if (paintMap.containsValue(user.getLocation().add(0, -1, 0).getBlock().getType())) {
                    Server.runTaskSynchrony(() -> {
                        user.damage(GraffitiServer.PAINT_DAMAGE);
                    }, GameGraffiti.getPlugin());
                }
            }
        }, 0, 20, GameGraffiti.getPlugin());
    }

    public void cancel() {
        if (this.damageTask != null) {
            this.damageTask.cancel();
        }
    }

    @EventHandler
    public void onUserDamage(UserDamageEvent e) {
        if (!GraffitiServer.isGameRunning()) {
            return;
        }

        if (e.getDamageCause().equals(EntityDamageEvent.DamageCause.FALL)) {
            e.setCancelDamage(true);
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemDrop(UserDropItemEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onJump(PlayerJumpEvent e) {

        if (!GraffitiServer.isGameRunning()) {
            return;
        }

        User user = Server.getUser(e.getPlayer());
        Block userBlock = user.getLocation().getBlock().getRelative(0, 1, 0);

        if (user.getLocation().getPitch() > -45) {
            return;
        }

        for (BlockFace face : JUMP_BLOCK_FACES) {
            Block block = userBlock.getRelative(face);

            if (JUMP_BLOCK_TYPES.contains(block.getType())) {
                user.setVelocity(new Vector(0, 0.9, 0));
                this.runJump(user);
                break;
            }
        }

    }

    private void runJump(User user) {
        this.jumpTasksByUser.put(user, Server.runTaskTimerAsynchrony(() -> {
            if (user.getLocation().getPitch() > -45) {
                if (this.jumpTasksByUser.get(user) != null) {
                    this.jumpTasksByUser.get(user).cancel();
                }
                this.jumpTasksByUser.remove(user);
                return;
            }

            boolean found = false;
            for (BlockFace face : JUMP_BLOCK_FACES) {
                Block block = user.getLocation().getBlock().getRelative(face);

                if (JUMP_BLOCK_TYPES.contains(block.getType())) {
                    user.setVelocity(new Vector(0, 0.9, 0));
                    found = true;
                    break;
                }
            }

            if (!found) {
                this.jumpTasksByUser.get(user).cancel();
                this.jumpTasksByUser.remove(user);
            }
        }, 5, 10, GameGraffiti.getPlugin()));
    }

    @EventHandler
    public void onUserDamageByUser(UserDamageByUserEvent e) {
        if (!GraffitiServer.isGameRunning()) {
            return;
        }

        GraffitiUser user = (GraffitiUser) e.getUser();
        GraffitiUser damager = (GraffitiUser) e.getUserDamager();

        if (!e.getUserDamager().getStatus().equals(Status.User.IN_GAME)) {
            e.setCancelDamage(true);
            e.setCancelled(true);
            return;
        }

        if (user.getTeam().equals(damager.getTeam())) {
            e.setCancelDamage(true);
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onUserDeath(UserDeathEvent event) {
        if (!GraffitiServer.isGameRunning()) {
            return;
        }

        event.getDrops().clear();
        event.setAutoRespawn(true);
        event.setKeepInventory(true);
        event.setBroadcastDeathMessage(false);
    }

    @EventHandler
    public void onUserRespawn(UserRespawnEvent event) {
        if (!GraffitiServer.isGameRunning()) {
            return;
        }

        GraffitiUser user = (GraffitiUser) event.getUser();

        event.setRespawnLocation(user.getTeamSpawn());

        user.lockLocation(true);
        user.setItem(EquipmentSlot.HEAD, new ExItemStack(Material.AIR));

        Server.runTaskTimerSynchrony((time) -> {
            if (time > 0) {
                user.showTitle(Component.text(time, ExTextColor.WARNING), Component.empty(), Duration.ofSeconds(1));
            } else {
                user.lockLocation(false);
            }
        }, GraffitiServer.RESPAWN_TIME, true, 0, 20, GameGraffiti.getPlugin());
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent e) {
        if (e.getEntity() instanceof Chicken) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemSwap(PlayerItemHeldEvent e) {
        GraffitiUser user = (GraffitiUser) Server.getUser(e.getPlayer());
        ExItemStack item = ExItemStack.getItem(user.getInventory().getItem(e.getPreviousSlot()), false);

        if (PaintManager.SNIPER_GUN.getBlueItem().equals(item) || PaintManager.SNIPER_GUN.getRedItem().equals(item)) {
            user.setItem(EquipmentSlot.HEAD, new ExItemStack(Material.AIR));
            user.removePotionEffects();
        }
    }
}
