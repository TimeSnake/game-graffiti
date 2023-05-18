/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.graffiti.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.ServerManager;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.scoreboard.ExSideboard;
import de.timesnake.basic.bukkit.util.user.scoreboard.ExSideboard.LineId;
import de.timesnake.basic.bukkit.util.user.scoreboard.ExSideboardBuilder;
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
import de.timesnake.library.chat.ExTextColor;
import de.timesnake.library.extension.util.chat.Chat;
import java.time.Duration;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class GraffitiServerManager extends LoungeBridgeServerManager<GraffitiGame> {

  public static GraffitiServerManager getInstance() {
    return (GraffitiServerManager) ServerManager.getInstance();
  }

  private boolean stopAfterStart = false;
  private boolean stopped = false;
  private Integer time;
  private BukkitTask timeTask;
  private ExSideboard gameSideboard;
  private ExSideboard spectatorSideboard;
  private UserManager userManager;
  private PaintManager paintManager;

  public void onGraffitiEnable() {
    super.onLoungeBridgeEnable();

    this.userManager = new UserManager();
    this.paintManager = new PaintManager();

    this.gameSideboard = Server.getScoreboardManager()
        .registerExSideboard(new ExSideboardBuilder()
            .name(GraffitiServer.getGame().getName())
            .title("§6§l" + GraffitiServer.getGame().getDisplayName())
            .lineSpacer()
            .addLine(LineId.TIME)
            .addLine(LineId.PLAYERS));

    this.spectatorSideboard = Server.getScoreboardManager()
        .registerExSideboard(new ExSideboardBuilder()
            .name(GraffitiServer.getGame().getName())
            .title("§6§l" + GraffitiServer.getGame().getDisplayName())
            .lineSpacer()
            .addLine(LineId.TIME)
            .addLine(LineId.PLAYERS));

    for (int i = GraffitiMap.ITEM_SPAWNER_START_INDEX; i < GraffitiMap.ITEM_SPAWNER_END_INDEX;
        i++) {
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
  public void onMapLoad() {
    this.time = this.getMap().getTime();
    this.updateSideboardTime();
  }

  @Override
  public void onGameStart() {
    if (this.stopAfterStart) {
      this.stopGame();
    }

    Server.getInGameUsers().forEach(User::unlockLocation);
    this.userManager.run();

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

    if (this.timeTask != null) {
      this.timeTask.cancel();
    }

    this.userManager.cancel();

    Tuple<Integer, Integer> blueRedBlocks = this.paintManager.calcWinner();
    int blueBlocks = blueRedBlocks.getA();
    int redBlocks = blueRedBlocks.getB();

    this.broadcastGameMessage(Chat.getLineSeparator());

    Component title;

    int kills = this.getGame().getBlueTeam().getKills()
        + this.getGame().getRedTeam().getKills();

    Server.getInGameUsers().forEach(u ->
        u.addCoins((float) ((GameUser) u).getKills() / kills *
            GraffitiServer.KILL_COINS_POOL, true));

    if (blueBlocks > redBlocks) {
      title = Component.text("Blue", ExTextColor.BLUE)
          .append(Component.text(" wins", ExTextColor.GOLD));
      this.broadcastGameMessage(Component.text("Blue", ExTextColor.BLUE)
          .append(Component.text(" wins!", ExTextColor.WHITE)));

      this.getGame().getBlueTeam().getUsers()
          .forEach(u -> u.addCoins(GraffitiServer.WIN_COINS, true));
    } else if (redBlocks > blueBlocks) {
      title = Component.text("Red", ExTextColor.RED)
          .append(Component.text(" wins", ExTextColor.GOLD));
      this.broadcastGameMessage(Component.text("Red", ExTextColor.RED)
          .append(Component.text(" wins!", ExTextColor.WHITE)));
      this.getGame().getRedTeam().getUsers()
          .forEach(u -> u.addCoins(GraffitiServer.WIN_COINS, true));
    } else {
      title = Component.text("Tie", ExTextColor.WHITE);
      this.broadcastGameMessage(Component.text("Tie!", ExTextColor.WHITE));
      Server.getInGameUsers().forEach(u -> u.addCoins(GraffitiServer.WIN_COINS / 2, true));
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
  public ExSideboard getSpectatorSideboard() {
    return this.spectatorSideboard;
  }

  public void updateSideboardTime() {
    this.gameSideboard.updateScore(LineId.TIME, this.time);
    this.spectatorSideboard.updateScore(LineId.TIME, this.time);
  }

  public void updateSideboardPlayers() {
    int size = Server.getUsers(u ->
        u.getStatus().equals(Status.User.IN_GAME)
            || u.getStatus().equals(Status.User.PRE_GAME)).size();
    this.gameSideboard.updateScore(LineId.PLAYERS, size);
    this.spectatorSideboard.updateScore(LineId.PLAYERS, size);
  }

  @Override
  public ExSideboard getGameSideboard() {
    return this.gameSideboard;
  }

  @Override
  public GraffitiMap getMap() {
    return (GraffitiMap) super.getMap();
  }

}
