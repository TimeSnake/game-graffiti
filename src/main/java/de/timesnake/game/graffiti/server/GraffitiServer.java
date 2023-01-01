/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.graffiti.server;

import de.timesnake.basic.bukkit.util.user.scoreboard.Sideboard;
import de.timesnake.basic.loungebridge.util.server.LoungeBridgeServer;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class GraffitiServer extends LoungeBridgeServer {

    public static final int ITEM_SPAWNER_DELAY = 20;
    public static final int ITEM_SPAWNER_DELAY_RANGE = 20;
    public static final List<ItemStack> ITEM_SPAWNER_ITEMS = List.of(PaintManager.PAINT_BALL,
            PaintManager.SNIPER_GUN.getBlueItem());

    public static final int RESPAWN_TIME = 3;

    public static final double PAINT_DAMAGE = 2;

    public static GraffitiGame getGame() {
        return ((GraffitiGame) server.getGame());
    }

    public static GraffitiMap getMap() {
        return server.getMap();
    }

    public static void updateSideboardPlayers() {
        server.updateSideboardPlayers();
    }

    public static Sideboard getGameSideboard() {
        return server.getGameSideboard();
    }

    private static final GraffitiServerManager server = GraffitiServerManager.getInstance();
}
