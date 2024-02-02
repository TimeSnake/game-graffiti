/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.graffiti.server;

import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.basic.bukkit.util.world.ExWorld.Restriction;
import de.timesnake.basic.game.util.game.Map;
import de.timesnake.basic.loungebridge.util.game.ResetableMap;
import de.timesnake.database.util.game.DbMap;
import de.timesnake.library.basic.util.Loggers;
import org.bukkit.GameRule;

import java.util.Collection;

public class GraffitiMap extends Map implements ResetableMap {

  public static final int ITEM_SPAWNER_START_INDEX = 100;
  public static final int ITEM_SPAWNER_END_INDEX = 120;
  private static final int SPECTATOR_SPAWN_INDEX = 0;
  private static final int BLUE_SPAWN_INDEX = 10;
  private static final int RED_SPAWN_INDEX = 20;

  private static final int DEFAULT_MAP_SIZE = 100;
  private static final int DEFAULT_TIME = 5 * 60;

  private final int mapSize;
  private final int time;

  public GraffitiMap(DbMap map, boolean loadWorld) {
    super(map, loadWorld);

    this.mapSize = this.getProperty("size", Integer.class, DEFAULT_MAP_SIZE,
        v -> Loggers.GAME.warning("Can not load map size of map " + super.name + ", info map size is not an integer"));

    this.time = this.getProperty("time", Integer.class, DEFAULT_TIME,
        v -> Loggers.GAME.warning("Can not load time of map " + super.name + ", info time is not an integer"));

    ExWorld world = this.getWorld();

    world.setAutoSave(false);
    world.restrict(ExWorld.Restriction.BLOCK_PLACE, true);
    world.restrict(ExWorld.Restriction.BLOCK_BREAK, true);
    world.restrict(ExWorld.Restriction.FOOD_CHANGE, true);
    world.restrict(Restriction.DROP_PICK_ITEM, true);
    world.restrict(Restriction.CRAFTING, true);
    world.setGameRule(GameRule.KEEP_INVENTORY, true);
    world.setGameRule(GameRule.NATURAL_REGENERATION, false);
    world.setExceptService(true);
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
