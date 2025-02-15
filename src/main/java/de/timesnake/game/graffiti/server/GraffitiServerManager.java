/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.graffiti.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.ServerManager;
import de.timesnake.basic.bukkit.util.server.TimeUnit;
import de.timesnake.basic.bukkit.util.user.scoreboard.KeyedSideboard;
import de.timesnake.basic.bukkit.util.user.scoreboard.KeyedSideboard.LineId;
import de.timesnake.basic.bukkit.util.user.scoreboard.KeyedSideboardBuilder;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.game.util.game.Team;
import de.timesnake.basic.loungebridge.util.server.EndMessage;
import de.timesnake.basic.loungebridge.util.server.LoungeBridgeServerManager;
import de.timesnake.basic.loungebridge.util.tool.advanced.ItemSpawner;
import de.timesnake.basic.loungebridge.util.user.GameUser;
import de.timesnake.database.util.game.DbGame;
import de.timesnake.database.util.game.DbTmpGame;
import de.timesnake.game.graffiti.main.GameGraffiti;
import de.timesnake.game.graffiti.user.GraffitiUser;
import de.timesnake.game.graffiti.user.UserManager;
import de.timesnake.library.basic.util.Status;
import de.timesnake.library.basic.util.Tuple;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class GraffitiServerManager extends LoungeBridgeServerManager<GraffitiGame> {

  public static GraffitiServerManager getInstance() {
    return (GraffitiServerManager) ServerManager.getInstance();
  }

  private boolean stopped = false;
  private Integer time;
  private BukkitTask timeTask;
  private KeyedSideboard gameSideboard;
  private KeyedSideboard spectatorSideboard;
  private UserManager userManager;
  private PaintManager paintManager;

  public void onGraffitiEnable() {
    super.onLoungeBridgeEnable();

    this.userManager = new UserManager();
    this.paintManager = new PaintManager();

    this.gameSideboard = Server.getScoreboardManager()
        .registerExSideboard(new KeyedSideboardBuilder()
            .name("graffiti")
            .title("§6§l" + GraffitiServer.getGame().getDisplayName())
            .lineSpacer()
            .addLine(LineId.TIME)
            .addLine(LineId.PLAYERS));

    this.spectatorSideboard = Server.getScoreboardManager()
        .registerExSideboard(new KeyedSideboardBuilder()
            .name("graffiti_spec")
            .title("§6§l" + GraffitiServer.getGame().getDisplayName())
            .lineSpacer()
            .addLine(LineId.TIME)
            .addLine(LineId.PLAYERS));

    for (int i = GraffitiMap.ITEM_SPAWNER_START_INDEX; i < GraffitiMap.ITEM_SPAWNER_END_INDEX;
        i++) {
      this.getToolManager().add(new ItemSpawner(i, TimeUnit.SECONDS, GraffitiServer.ITEM_SPAWNER_DELAY_SEC,
          GraffitiServer.ITEM_SPAWNER_DELAY_RANGE_SEC, GraffitiServer.ITEM_SPAWNER_ITEMS));
    }

    this.allowTeamMateDamage = false;
    this.getChatManager().setBroadcastDeath(false);
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
  public void onMapLoad() {
    this.time = this.getMap().getTime();
    this.updateSideboardTime();
  }

  @Override
  public void onGameStart() {
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

    EndMessage endMessage = new EndMessage();

    Team blueTeam = this.getGame().getBlueTeam();
    Team redTeam = this.getGame().getRedTeam();

    int kills = blueTeam.getKills() + redTeam.getKills();

    Server.getInGameUsers().forEach(u -> u.addCoins((float) ((GameUser) u).getKills() / kills *
            GraffitiServer.KILL_COINS_POOL, true));

    if (blueBlocks > redBlocks) {
      endMessage.winner(blueTeam);
    } else if (redBlocks > blueBlocks) {
      endMessage.winner(redTeam);
    } else {
      endMessage.winner("Tie!");
      Server.getInGameUsers().forEach(u -> u.addCoins(GraffitiServer.WIN_COINS / 2, true));
    }

    endMessage.subTitle(blueTeam.getTDColor() + blueBlocks + " §p- " + redTeam.getTDColor() + redBlocks);
    endMessage.addExtra("Blocks:");
    endMessage.addExtra(blueTeam.getTDColor() + blueBlocks + " §p- " + redTeam.getTDColor() + redBlocks);
    endMessage.send();
  }

  @Override
  public void onGameReset() {
    if (this.getMap() != null) {
      Server.getWorldManager().reloadWorld(this.getMap().getWorld());
    }

    this.stopped = false;
  }

  @Override
  public void onGameUserQuit(GameUser gameUser) {
    super.onGameUserQuit(gameUser);
    this.updateSideboardPlayers();
  }

  @Override
  public boolean checkGameEnd() {
    return this.getGame().getBlueTeam().getInGameUsers().isEmpty()
        || this.getGame().getRedTeam().getInGameUsers().isEmpty();
  }

  @Override
  public boolean isRejoiningAllowed() {
    return true;
  }

  @Override
  public ExLocation getSpectatorSpawn() {
    return this.getMap().getSpectatorSpawn();
  }

  @Override
  public KeyedSideboard getSpectatorSideboard() {
    return this.spectatorSideboard;
  }

  public void updateSideboardTime() {
    this.gameSideboard.updateScore(LineId.TIME, this.time);
    this.spectatorSideboard.updateScore(LineId.TIME, this.time);
  }

  public void updateSideboardPlayers() {
    int size =
        Server.getUsers(u -> u.getStatus().equals(Status.User.IN_GAME) || u.getStatus().equals(Status.User.PRE_GAME)).size();
    this.gameSideboard.updateScore(LineId.PLAYERS, size);
    this.spectatorSideboard.updateScore(LineId.PLAYERS, size);
  }

  @Override
  public KeyedSideboard getGameSideboard() {
    return this.gameSideboard;
  }

  @Override
  public GraffitiMap getMap() {
    return (GraffitiMap) super.getMap();
  }

}
