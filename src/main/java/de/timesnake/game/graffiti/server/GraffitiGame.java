package de.timesnake.game.graffiti.server;

import de.timesnake.basic.game.util.Game;
import de.timesnake.basic.game.util.Team;
import de.timesnake.database.util.game.DbGame;
import de.timesnake.database.util.game.DbMap;

public class GraffitiGame extends Game {

    private static final String BLUE_TEAM_NAME = "blue";
    private static final String RED_TEAM_NAME = "red";

    public GraffitiGame(DbGame game, boolean loadWorlds) {
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
