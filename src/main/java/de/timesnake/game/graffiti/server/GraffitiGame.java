/*
 * workspace.game-graffiti.main
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

package de.timesnake.game.graffiti.server;

import de.timesnake.basic.game.util.game.Team;
import de.timesnake.basic.game.util.game.TmpGame;
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
