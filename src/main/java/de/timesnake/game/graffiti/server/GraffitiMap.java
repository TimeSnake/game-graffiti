/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.graffiti.server;

import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.basic.bukkit.util.world.ExWorld.Restriction;
import de.timesnake.basic.game.util.game.Map;
import de.timesnake.database.util.game.DbMap;
import de.timesnake.library.basic.util.Loggers;
import java.util.Collection;
import java.util.List;
import org.bukkit.GameRule;

public class GraffitiMap extends Map {

    public static final int ITEM_SPAWNER_START_INDEX = 100;
    public static final int ITEM_SPAWNER_END_INDEX = 120;
    private static final int SPECTATOR_SPAWN_INDEX = 0;
    private static final int BLUE_SPAWN_INDEX = 10;
    private static final int RED_SPAWN_INDEX = 20;
    private static final String MAP_SIZE_TOKEN = "mapSize=";
    private static final String TIME_TOKEN = "time=";

    private static final int DEFAULT_MAP_SIZE = 100;
    private static final int DEFAULT_TIME = 5 * 60;

    private int mapSize = DEFAULT_MAP_SIZE;
    private Integer time = DEFAULT_TIME;

    public GraffitiMap(DbMap map, boolean loadWorld) {
        super(map, loadWorld);

        List<String> infos = map.getInfo();

        for (String info : infos) {
            if (info.startsWith(MAP_SIZE_TOKEN)) {
                try {
                    this.mapSize = Integer.parseInt(info.replace(MAP_SIZE_TOKEN, ""));
                } catch (NumberFormatException e) {
                    Loggers.GAME.warning("Can not load map size of map " +
                            super.name + ", info map size is not an integer");
                }
            } else if (info.startsWith(TIME_TOKEN)) {
                try {
                    this.time = Integer.parseInt(info.replace(TIME_TOKEN, ""));
                } catch (NumberFormatException e) {
                    Loggers.GAME.warning("Can not load time of map " +
                            super.name + ", info time is not an integer");
                }
            }
        }

        this.getWorld().setAutoSave(false);
        this.getWorld().restrict(ExWorld.Restriction.BLOCK_PLACE, true);
        this.getWorld().restrict(ExWorld.Restriction.BLOCK_BREAK, true);
        this.getWorld().restrict(ExWorld.Restriction.FOOD_CHANGE, true);
        this.getWorld().restrict(Restriction.CRAFTING, true);
        this.getWorld().setGameRule(GameRule.NATURAL_REGENERATION, false);
        this.getWorld().setExceptService(true);
    }

    public ExLocation getSpectatorSpawn() {
        return super.getLocation(SPECTATOR_SPAWN_INDEX);
    }

    public ExLocation getBlueSpawn() {
        return super.getLocation(BLUE_SPAWN_INDEX);
    }

    public ExLocation getRedSpawn() {
        return super.getLocation(RED_SPAWN_INDEX);
    }

    public Collection<ExLocation> getItemSpawnerLocations() {
        return super.getLocations(ITEM_SPAWNER_START_INDEX, ITEM_SPAWNER_END_INDEX);
    }

    public int getMapSize() {
        return mapSize;
    }

    public int getTime() {
        return time;
    }
}
