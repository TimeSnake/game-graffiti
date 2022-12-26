/*
 * Copyright (C) 2022 timesnake
 */

package de.timesnake.game.graffiti.main;

import de.timesnake.basic.bukkit.util.ServerManager;
import de.timesnake.game.graffiti.server.GraffitiServerManager;
import org.bukkit.plugin.java.JavaPlugin;

public class GameGraffiti extends JavaPlugin {

    public static GameGraffiti getPlugin() {
        return plugin;
    }

    private static GameGraffiti plugin;

    @Override
    public void onLoad() {
        ServerManager.setInstance(new GraffitiServerManager());
    }

    @Override
    public void onEnable() {
        GameGraffiti.plugin = this;

        GraffitiServerManager.getInstance().onGraffitiEnable();
    }


}
