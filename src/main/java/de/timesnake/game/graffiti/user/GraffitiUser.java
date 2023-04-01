/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.graffiti.user;

import de.timesnake.basic.bukkit.util.user.inventory.ExItemStack;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.loungebridge.util.user.GameUser;
import de.timesnake.game.graffiti.server.GraffitiServer;
import de.timesnake.game.graffiti.server.PaintManager;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

public class GraffitiUser extends GameUser {

    public GraffitiUser(Player player) {
        super(player);
    }

    @Override
    public void onGameJoin() {
        super.onGameJoin();

        this.teleport(this.getTeamSpawn());
        this.lockLocation(true);
        this.setItems();

        GraffitiServer.updateSideboardPlayers();
    }

    public ExLocation getTeamSpawn() {
        if (GraffitiServer.getMap() == null) {
            return null;
        }

        if (this.getTeam().equals(GraffitiServer.getGame().getBlueTeam())) {
            return GraffitiServer.getMap().getBlueSpawn();
        } else if (this.getTeam().equals(GraffitiServer.getGame().getRedTeam())) {
            return GraffitiServer.getMap().getRedSpawn();
        }

        return GraffitiServer.getMap().getSpectatorSpawn();
    }


    private void setItems() {
        if (this.getTeam().equals(GraffitiServer.getGame().getBlueTeam())) {
            this.setItem(PaintManager.BLUE_SPRAY_BOTTLE);
            this.setItem(PaintManager.SNIPER_GUN.getBlueItem());
            this.setItem(PaintManager.AIR_PISTOL.getBlueItem());
            this.setItem(EquipmentSlot.CHEST,
                    ExItemStack.getLeatherArmor(Material.LEATHER_CHESTPLATE, Color.BLUE));
            this.setItem(EquipmentSlot.LEGS,
                    ExItemStack.getLeatherArmor(Material.LEATHER_LEGGINGS, Color.BLUE));
            this.setItem(EquipmentSlot.FEET,
                    ExItemStack.getLeatherArmor(Material.LEATHER_BOOTS, Color.BLUE));
        } else {
            this.setItem(PaintManager.RED_SPRAY_BOTTLE);
            this.setItem(PaintManager.SNIPER_GUN.getRedItem());
            this.setItem(PaintManager.AIR_PISTOL.getRedItem());
            this.setItem(EquipmentSlot.CHEST,
                    ExItemStack.getLeatherArmor(Material.LEATHER_CHESTPLATE, Color.RED));
            this.setItem(EquipmentSlot.LEGS,
                    ExItemStack.getLeatherArmor(Material.LEATHER_LEGGINGS, Color.RED));
            this.setItem(EquipmentSlot.FEET,
                    ExItemStack.getLeatherArmor(Material.LEATHER_BOOTS, Color.RED));
        }
    }
}
