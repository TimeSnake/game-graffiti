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

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.ServerManager;
import de.timesnake.basic.bukkit.util.user.scoreboard.Sideboard;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.loungebridge.util.game.ItemSpawner;
import de.timesnake.basic.loungebridge.util.server.LoungeBridgeServerManager;
import de.timesnake.basic.loungebridge.util.user.GameUser;
import de.timesnake.database.util.game.DbGame;
import de.timesnake.database.util.game.DbTmpGame;
import de.timesnake.game.graffiti.main.GameGraffiti;
import de.timesnake.game.graffiti.user.GraffitiUser;
import de.timesnake.game.graffiti.user.UserManager;
import de.timesnake.library.basic.util.Status;
import de.timesnake.library.basic.util.Tuple;
import de.timesnake.library.basic.util.chat.ExTextColor;
import de.timesnake.library.extension.util.chat.Chat;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;

public class GraffitiServerManager extends LoungeBridgeServerManager<GraffitiGame> {

    public static GraffitiServerManager getInstance() {
        return (GraffitiServerManager) ServerManager.getInstance();
    }

    private boolean isRunning = false;
    private boolean stopAfterStart = false;
    private boolean stopped = false;
    private Integer time;
    private BukkitTask timeTask;
    private Sideboard gameSideboard;
    private Sideboard spectatorSideboard;
    private UserManager userManager;
    private PaintManager paintManager;

    public void onGraffitiEnable() {
        super.onLoungeBridgeEnable();

        this.userManager = new UserManager();
        this.paintManager = new PaintManager();

        this.gameSideboard = Server.getScoreboardManager().registerSideboard(GraffitiServer.getGame().getName(),
                "§6§l" + GraffitiServer.getGame().getDisplayName());

        this.gameSideboard.setScore(4, "§c§lTime");
        // time
        this.gameSideboard.setScore(2, "§r§f-----------");
        this.gameSideboard.setScore(1, "§9§lPlayers");
        // players

        this.spectatorSideboard = Server.getScoreboardManager().registerSideboard(GraffitiServer.getGame().getName(),
                "§6§l" + GraffitiServer.getGame().getDisplayName());

        this.spectatorSideboard.setScore(4, "§c§lTime");
        // time
        this.spectatorSideboard.setScore(2, "§r§f-----------");
        this.spectatorSideboard.setScore(1, "§9§lPlayers");
        // players

        for (int i = GraffitiMap.ITEM_SPAWNER_START_INDEX; i < GraffitiMap.ITEM_SPAWNER_END_INDEX; i++) {
            this.getToolManager().add(new ItemSpawner(i, GraffitiServer.ITEM_SPAWNER_DELAY,
                    GraffitiServer.ITEM_SPAWNER_DELAY_RANGE, GraffitiServer.ITEM_SPAWNER_ITEMS));
        }
    }

    @Override
    public GraffitiUser loadUser(Player player) {
        return new GraffitiUser(player);
    }

    @Override
    protected GraffitiGame loadGame(DbGame dbGame, boolean loadWorlds) {
        return new GraffitiGame((DbTmpGame) dbGame, true);
    }

    @Override
    public Plugin getGamePlugin() {
        return Plugin.GRAFFITI;
    }

    @Override
    public boolean isGameRunning() {
        return this.isRunning;
    }

    @Override
    @Deprecated
    public void broadcastGameMessage(String message) {
        Server.broadcastMessage(Plugin.GRAFFITI, message);
    }

    @Override
    public void broadcastGameMessage(Component message) {
        Server.broadcastMessage(Plugin.GRAFFITI, message);
    }

    @Override
    public void onMapLoad() {
        this.gameSideboard = Server.getScoreboardManager().registerSideboard(GraffitiServer.getGame().getName(),
                "§6§l" + GraffitiServer.getGame().getDisplayName());

        this.gameSideboard.setScore(4, "§c§lTime");
        // time
        this.gameSideboard.setScore(2, "§r§f-----------");
        this.gameSideboard.setScore(1, "§9§lPlayers");
        // players

        this.spectatorSideboard = Server.getScoreboardManager().registerSideboard(GraffitiServer.getGame().getName(),
                "§6§l" + GraffitiServer.getGame().getDisplayName());

        this.spectatorSideboard.setScore(4, "§c§lTime");
        // time
        this.spectatorSideboard.setScore(2, "§r§f-----------");
        this.spectatorSideboard.setScore(1, "§9§lPlayers");
        // players

        this.time = this.getMap().getTime();
        this.updateSideboardTime();
    }

    @Override
    public void onGameStart() {
        if (this.stopAfterStart) {
            this.stopGame();
        }

        Server.getInGameUsers().forEach(u -> u.lockLocation(false));
        this.userManager.run();

        this.isRunning = true;

        this.timeTask = Server.runTaskTimerSynchrony(() -> {
            this.updateSideboardTime();

            if (this.time <= 0) {
                this.stopGame();
                return;
            }

            this.time--;
        }, 0, 20, GameGraffiti.getPlugin());
    }

    @Override
    public void onGameStop() {
        if (this.stopped) {
            return;
        }

        this.stopped = true;
        this.isRunning = false;

        if (this.timeTask != null) {
            this.timeTask.cancel();
        }

        this.userManager.cancel();

        Tuple<Integer, Integer> blueRedBlocks = this.paintManager.calcWinner();
        int blueBlocks = blueRedBlocks.getA();
        int redBlocks = blueRedBlocks.getB();

        this.broadcastGameMessage(Chat.getLineSeparator());

        Component title;

        if (blueBlocks > redBlocks) {
            title = Component.text("Blue", ExTextColor.BLUE).append(Component.text(" wins", ExTextColor.GOLD));
            this.broadcastGameMessage(Component.text("Blue", ExTextColor.BLUE)
                    .append(Component.text("wins!", ExTextColor.WHITE)));
        } else if (redBlocks > blueBlocks) {
            title = Component.text("Red", ExTextColor.RED).append(Component.text(" wins", ExTextColor.GOLD));
            this.broadcastGameMessage(Component.text("Red", ExTextColor.RED)
                    .append(Component.text("wins!", ExTextColor.WHITE)));
        } else {
            title = Component.text("Tie", ExTextColor.WHITE);
            this.broadcastGameMessage(Component.text("Tie!", ExTextColor.WHITE));
        }

        Server.broadcastTitle(title, Component.text(blueBlocks, ExTextColor.BLUE)
                        .append(Component.text(" - ", ExTextColor.PUBLIC))
                        .append(Component.text(redBlocks, ExTextColor.RED)),
                Duration.ofSeconds(5));

        this.broadcastGameMessage(Component.text("Blocks: ", ExTextColor.PUBLIC)
                .append(Component.text(blueBlocks, ExTextColor.BLUE))
                .append(Component.text(" - ", ExTextColor.PUBLIC))
                .append(Component.text(redBlocks, ExTextColor.RED)));
        this.broadcastGameMessage(Chat.getLineSeparator());
    }

    @Override
    public void onGameReset() {
        if (this.getMap() != null) {
            Server.getWorldManager().reloadWorld(this.getMap().getWorld());
        }

        this.stopped = false;
    }

    @Override
    public void onGameUserQuitBeforeStart(GameUser user) {

    }

    @Override
    public void onGameUserQuit(GameUser gameUser) {
        this.updateSideboardPlayers();

        if (GraffitiServer.getGame().getBlueTeam().getInGameUsers().size() < 1
                || GraffitiServer.getGame().getRedTeam().getInGameUsers().size() < 1) {
            this.stopGame();
        }
    }

    @Override
    public boolean isRejoiningAllowed() {
        return true;
    }

    @Override
    public void onGameUserRejoin(GameUser user) {

    }

    @Override
    public ExLocation getSpectatorSpawn() {
        return GraffitiServer.getSpectatorSpawn();
    }

    @Override
    public Sideboard getSpectatorSideboard() {
        return this.spectatorSideboard;
    }

    public void updateSideboardTime() {
        String timeStr = Chat.getTimeString(this.time);
        this.gameSideboard.setScore(3, timeStr);
        this.spectatorSideboard.setScore(3, timeStr);
    }

    public void updateSideboardPlayers() {
        String size = String.valueOf(Server.getUsers(u ->
                u.getStatus().equals(Status.User.IN_GAME) || u.getStatus().equals(Status.User.PRE_GAME)).size());
        this.gameSideboard.setScore(0, size);
        this.spectatorSideboard.setScore(0, String.valueOf(Server.getUsers(u ->
                u.getStatus().equals(Status.User.IN_GAME) || u.getStatus().equals(Status.User.PRE_GAME)).size()));
    }

    public Sideboard getGameSideboard() {
        return this.gameSideboard;
    }

    @Override
    public GraffitiMap getMap() {
        return (GraffitiMap) super.getMap();
    }

}
