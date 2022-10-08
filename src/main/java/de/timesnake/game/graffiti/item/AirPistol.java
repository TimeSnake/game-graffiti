/*
 * game-graffiti.main
 * Copyright (C) 2022 timesnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; If not, see <http://www.gnu.org/licenses/>.
 */

package de.timesnake.game.graffiti.item;

import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.game.graffiti.server.GraffitiServer;
import de.timesnake.game.graffiti.server.PaintManager;
import de.timesnake.game.graffiti.user.GraffitiUser;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Projectile;
import org.bukkit.event.block.Action;

public class AirPistol extends Gun {

    private static final int MAX_PAINT_DISTANCE = 24;
    private static final double RADIUS = 7;
    private static final double DENSITY = 0.6;

    private static final int MAX_DAMAGE_DISTANCE = 12;
    private static final double DAMAGE = 4;

    private static final float SHOTS_PER_SECOND = 3f;

    public AirPistol() {
        super("air_pistol",
                new ExItemStack(Material.LIGHT_BLUE_DYE).setDisplayName("§9Blue Air Pistol")
                        .setLore("", "§fDamage: §7" + DAMAGE / 2 + " ❤", "§fPaint: §7blue, " + RADIUS + " blocks, " +
                                "scattered")
                        .setDropable(false),
                new ExItemStack(Material.ORANGE_DYE).setDisplayName("§cRed Air Pistol")
                        .setLore("", "§fDamage: §7" + DAMAGE / 2 + " ❤", "§fPaint: §7red, " + RADIUS + " blocks, " +
                                "scattered")
                        .setDropable(false),
                SHOTS_PER_SECOND);
    }

    @Override
    public void shoot(GraffitiUser user, Action action) {

        Projectile paintball;

        if (user.getTeam().equals(GraffitiServer.getGame().getBlueTeam())) {
            paintball = user.getWorld().spawn(user.getEyeLocation().add(0, -0.5, 0), EnderPearl.class);
        } else {
            paintball = user.getWorld().spawn(user.getEyeLocation().add(0, -0.5, 0), Egg.class);
        }

        paintball.setShooter(user.getPlayer());
        paintball.customName(Component.text(this.name));
        paintball.setCustomNameVisible(false);
        paintball.setVelocity(user.getLocation().getDirection().normalize().multiply(1.2));
    }

    @Override
    public boolean onBlockHit(GraffitiUser user, Block block, Projectile projectile) {
        PaintManager.drawCircle(user, block, user.getLocation(), RADIUS, DENSITY);
        projectile.remove();
        return true;
    }

    @Override
    public boolean onUserHit(GraffitiUser user, GraffitiUser hitUser, Projectile projectile) {
        if ((user.getTeam().equals(GraffitiServer.getGame().getBlueTeam())
                && !hitUser.getTeam().equals(GraffitiServer.getGame().getBlueTeam()))
                || (!user.getTeam().equals(GraffitiServer.getGame().getBlueTeam())
                && hitUser.getTeam().equals(GraffitiServer.getGame().getBlueTeam()))) {
            hitUser.damage(DAMAGE, user.getPlayer());
        }
        projectile.remove();
        return true;
    }
}
