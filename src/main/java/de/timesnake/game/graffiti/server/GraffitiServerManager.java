package de.timesnake.game.graffiti.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.ServerManager;
import de.timesnake.basic.bukkit.util.user.scoreboard.Sideboard;
import de.timesnake.basic.game.util.Game;
import de.timesnake.basic.loungebridge.util.game.GameElement;
import de.timesnake.basic.loungebridge.util.game.ItemSpawner;
import de.timesnake.basic.loungebridge.util.server.LoungeBridgeServer;
import de.timesnake.basic.loungebridge.util.server.LoungeBridgeServerManager;
import de.timesnake.basic.loungebridge.util.user.GameUser;
import de.timesnake.database.util.game.DbGame;
import de.timesnake.game.graffiti.main.GameGraffiti;
import de.timesnake.game.graffiti.user.GraffitiUser;
import de.timesnake.game.graffiti.user.UserManager;
import de.timesnake.library.basic.util.Status;
import de.timesnake.library.basic.util.Tuple;
import de.timesnake.library.basic.util.chat.ChatColor;
import de.timesnake.library.extension.util.chat.Chat;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;

public class GraffitiServerManager extends LoungeBridgeServerManager {

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

        this.gameSideboard = Server.getScoreboardManager().registerNewSideboard(GraffitiServer.getGame().getName(),
                "§6§l" + GraffitiServer.getGame().getDisplayName());

        this.gameSideboard.setScore(4, "§c§lTime");
        // time
        this.gameSideboard.setScore(2, "§r§f-----------");
        this.gameSideboard.setScore(1, "§9§lPlayers");
        // players

        this.spectatorSideboard = Server.getScoreboardManager().registerNewSideboard(GraffitiServer.getGame().getName(),
                "§6§l" + GraffitiServer.getGame().getDisplayName());

        this.spectatorSideboard.setScore(4, "§c§lTime");
        // time
        this.spectatorSideboard.setScore(2, "§r§f-----------");
        this.spectatorSideboard.setScore(1, "§9§lPlayers");
        // players

        List<GameElement> gameElements = new LinkedList<>();

        for (int i = GraffitiMap.ITEM_SPAWNER_START_INDEX; i < GraffitiMap.ITEM_SPAWNER_END_INDEX; i++) {
            gameElements.add(new ItemSpawner(i, GraffitiServer.ITEM_SPAWNER_DELAY,
                    GraffitiServer.ITEM_SPAWNER_DELAY_RANGE, GraffitiServer.ITEM_SPAWNER_ITEMS));
        }

        super.getGameElementManager().addGameElement(gameElements);
    }

    @Override
    public GraffitiUser loadUser(Player player) {
        return new GraffitiUser(player);
    }

    @Override
    protected Game loadGame(DbGame dbGame, boolean loadWorlds) {
        return new GraffitiGame(dbGame, true);
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
    public void broadcastGameMessage(String message) {
        Server.broadcastMessage(Chat.getSenderPlugin(Plugin.GRAFFITI) + message);
    }

    @Override
    public void prepareGame() {

    }

    @Override
    public void onMapLoad() {
        this.gameSideboard = Server.getScoreboardManager().registerNewSideboard(GraffitiServer.getGame().getName(),
                "§6§l" + GraffitiServer.getGame().getDisplayName());

        this.gameSideboard.setScore(4, "§c§lTime");
        // time
        this.gameSideboard.setScore(2, "§r§f-----------");
        this.gameSideboard.setScore(1, "§9§lPlayers");
        // players

        this.spectatorSideboard = Server.getScoreboardManager().registerNewSideboard(GraffitiServer.getGame().getName(),
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

    public void stopGame() {
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

        String title;

        if (blueBlocks > redBlocks) {
            title = ChatColor.BLUE + "Blue" + ChatColor.GOLD + " wins";
            this.broadcastGameMessage(ChatColor.BLUE + "Blue" + " §fwins!");
        } else if (redBlocks > blueBlocks) {
            title = ChatColor.RED + "Red" + ChatColor.GOLD + " wins";
            this.broadcastGameMessage(ChatColor.RED + "Red" + " §fwins!");
        } else {
            title = "§fTie";
            this.broadcastGameMessage(ChatColor.WHITE + "Tie!");
        }

        Server.broadcastTitle(title,
                ChatColor.BLUE + blueBlocks + ChatColor.PUBLIC + " - " + ChatColor.RED + redBlocks,
                Duration.ofSeconds(5));

        this.broadcastGameMessage("§fBlocks: " + ChatColor.BLUE + blueBlocks + ChatColor.PUBLIC +
                " - " + ChatColor.RED + redBlocks);
        this.broadcastGameMessage(Chat.getLineSeparator());


        LoungeBridgeServer.closeGame();
    }

    @Override
    public void resetGame() {
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
    public Location getSpectatorSpawn() {
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
