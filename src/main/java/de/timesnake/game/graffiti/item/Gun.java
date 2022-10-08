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

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.game.graffiti.main.GameGraffiti;
import de.timesnake.game.graffiti.user.GraffitiUser;
import org.bukkit.block.Block;
import org.bukkit.entity.Projectile;
import org.bukkit.event.block.Action;

import java.util.HashSet;
import java.util.Set;

public abstract class Gun {

    protected final String name;
    protected final ExItemStack blueItem;
    protected final ExItemStack redItem;

    protected final float shotsPerSecond;

    protected final Set<GraffitiUser> cooldownUsers = new HashSet<>();

    public Gun(String name, ExItemStack item, float shotsPerSecond) {
        this.name = name;
        this.blueItem = item;
        this.redItem = item;
        this.shotsPerSecond = shotsPerSecond;
    }

    public Gun(String name, ExItemStack blueItem, ExItemStack redItem, float shotsPerSecond) {
        this.name = name;
        this.blueItem = blueItem;
        this.redItem = redItem;
        this.shotsPerSecond = shotsPerSecond;
    }

    public String getName() {
        return this.name;
    }

    public ExItemStack getBlueItem() {
        return this.blueItem;
    }

    public ExItemStack getRedItem() {
        return this.redItem;
    }

    public float getShotsPerSecond() {
        return shotsPerSecond;
    }

    public void onInteract(GraffitiUser user, Action action) {
        if (this.cooldownUsers.contains(user)) {
            return;
        }

        this.cooldownUsers.add(user);

        Server.runTaskLaterSynchrony(() -> this.cooldownUsers.remove(user), (int) (20 / shotsPerSecond),
                GameGraffiti.getPlugin());

        this.shoot(user, action);
    }

    public abstract void shoot(GraffitiUser user, Action action);

    public abstract boolean onBlockHit(GraffitiUser user, Block block, Projectile projectile);

    public abstract boolean onUserHit(GraffitiUser user, GraffitiUser hitUser, Projectile projectile);
}
