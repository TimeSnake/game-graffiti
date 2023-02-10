/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.graffiti.item;

import de.timesnake.basic.bukkit.util.user.inventory.ExItemStack;
import de.timesnake.game.graffiti.server.GraffitiServer;
import de.timesnake.game.graffiti.server.PaintManager;
import de.timesnake.game.graffiti.user.GraffitiUser;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;

public class PaintGun extends Gun {

    private static final double RADIUS = 3;
    private static final float SHOTS_PER_SECOND = 1f;
    private static final double DAMAGE = 10;

    private final Set<GraffitiUser> activeUsers = new HashSet<>();

    public PaintGun() {
        super("sniper", new ExItemStack(Material.BLUE_CANDLE).setDisplayName("§9Blue Paint Gun")
                        .setLore("", "§fDamage: §7" + DAMAGE / 2 + " ❤", "§fPaint: §7white, " + RADIUS + " blocks")
                        .setDropable(false),
                new ExItemStack(Material.RED_CANDLE).setDisplayName("§cRed Paint Gun")
                        .setLore("", "§fDamage: §7" + DAMAGE / 2 + " ❤", "§fPaint: §7white, " + RADIUS + " blocks")
                        .setDropable(false),
                SHOTS_PER_SECOND);
    }

    @Override
    public void onInteract(GraffitiUser user, Action action) {
        if (action.isRightClick()) {
            if (!this.activeUsers.contains(user)) {
                this.activeUsers.add(user);
                user.addPotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 16);
                user.setItem(EquipmentSlot.HEAD, new ExItemStack(Material.CARVED_PUMPKIN).setMoveable(false));

            } else {
                this.activeUsers.remove(user);
                user.removePotionEffects();
                user.setItem(EquipmentSlot.HEAD, new ExItemStack(Material.AIR));
            }
        } else if (action.isLeftClick() && this.activeUsers.contains(user)) {
            super.onInteract(user, action);
        }

    }

    @Override
    public void shoot(GraffitiUser user, Action action) {
        Snowball paintball = user.getWorld().spawn(user.getEyeLocation().add(0, -0.5, 0), Snowball.class);
        paintball.setShooter(user.getPlayer());
        paintball.setCustomName(this.name);
        paintball.setCustomNameVisible(false);
        paintball.setVelocity(user.getLocation().getDirection().normalize().multiply(2));
        paintball.setGravity(false);
    }

    @Override
    public boolean onBlockHit(GraffitiUser user, Block block, Projectile projectile) {
        PaintManager.drawCircle(user, block, user.getLocation(), RADIUS, 1, true);
        return true;
    }

    @Override
    public boolean onUserHit(GraffitiUser user, GraffitiUser hitUser, Projectile projectile) {
        if ((user.getTeam().equals(GraffitiServer.getGame().getBlueTeam())
                && !hitUser.getTeam().equals(GraffitiServer.getGame().getBlueTeam()))
                || (!user.getTeam().equals(GraffitiServer.getGame().getBlueTeam())
                && hitUser.getTeam().equals(GraffitiServer.getGame().getBlueTeam()))) {
            hitUser.damage(DAMAGE, user.getPlayer());
            user.playSound(Sound.ENTITY_PLAYER_LEVELUP, 2);
            return false;
        }
        return true;
    }
}
