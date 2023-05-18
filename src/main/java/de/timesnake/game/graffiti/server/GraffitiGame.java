/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.graffiti.server;

import de.timesnake.basic.game.util.game.Team;
import de.timesnake.basic.loungebridge.util.game.TmpGame;
import de.timesnake.database.util.game.DbMap;
import de.timesnake.database.util.game.DbTmpGame;

public class GraffitiGame extends TmpGame {

  private static final String BLUE_TEAM_NAME = "blue";
  private static final String RED_TEAM_NAME = "red";

  public GraffitiGame(DbTmpGame game, boolean loadWorlds) {
    super(game, loadWorlds);
  }

  @Override
  public GraffitiMap loadMap(DbMap dbMap, boolean loadWorld) {
    return new GraffitiMap(dbMap, true);
  }

  public Team getBlueTeam() {
    return super.getTeam(BLUE_TEAM_NAME);
  }

  public Team getRedTeam() {
    return super.getTeam(RED_TEAM_NAME);
  }
}
