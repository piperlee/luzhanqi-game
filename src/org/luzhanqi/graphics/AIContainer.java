package org.luzhanqi.graphics;

import java.util.List;
import java.util.Map;

import org.game_api.GameApi;
import org.game_api.GameApi.Container;
import org.game_api.GameApi.Game;
import org.game_api.GameApi.GameState;
import org.game_api.GameApi.Operation;
import org.game_api.GameApi.UpdateUI;
import org.game_api.GameApi.VerifyMove;
import org.game_api.GameApi.VerifyMoveDone;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

/**
 * A container for games that use for AI and send them Game API messages.
 */
public class AIContainer implements Container{
  public static final String PLAYER_ID = "playerId";
  private final Game game;
  private final List<Map<String, Object>> playersInfo = Lists.newArrayList();
  private final List<String> playerIds;
  private String updateUiPlayerId;
  private GameState gameState = new GameState();
  private GameState lastGameState = null;
  private List<Operation> lastMove = null;
  private String lastMovePlayerId;

  public AIContainer(Game game, int numberOfPlayers) {
    this.game = game;
    List<String> playerIds = Lists.newArrayList();
        
    for (int i = 0; i < numberOfPlayers; i++) {
      String playerId = String.valueOf(43 + i);
      playerIds.add(playerId);
      playersInfo.add(ImmutableMap.<String, Object>of(PLAYER_ID, playerId));
    }
    
    // AI pId white
    playerIds.add(GameApi.AI_PLAYER_ID);
    playersInfo.add(ImmutableMap.<String, Object>of(PLAYER_ID, GameApi.AI_PLAYER_ID));
    
    this.playerIds = ImmutableList.copyOf(playerIds);
  }

  public List<String> getPlayerIds() {
    return playerIds;
  }

  @Override
  public void sendGameReady() {
  }

  public void updateUi(String yourPlayerId) {
    updateUiPlayerId = yourPlayerId;
    game.sendUpdateUI(new UpdateUI(yourPlayerId, playersInfo,
        gameState.getStateForPlayerId(yourPlayerId),
        lastGameState == null ? null : lastGameState.getStateForPlayerId(yourPlayerId),
        lastMove, lastMovePlayerId, gameState.getPlayerIdToNumberOfTokensInPot()));
  }

  @Override
  public void sendMakeMove(List<Operation> operations) {
    lastMovePlayerId = updateUiPlayerId;
    lastMove = ImmutableList.copyOf(operations);
    lastGameState = gameState.copy();
    gameState.makeMove(operations);
    // Verify the move on all players
    for (String playerId : playerIds) {
      game.sendVerifyMove(new VerifyMove(playersInfo,
          gameState.getStateForPlayerId(playerId),
          lastGameState.getStateForPlayerId(playerId), lastMove, lastMovePlayerId,
          gameState.getPlayerIdToNumberOfTokensInPot()));
    }
    updateUi(updateUiPlayerId);
  }

  @Override
  public void sendVerifyMoveDone(VerifyMoveDone verifyMoveDone) {
    if (verifyMoveDone.getHackerPlayerId() != null) {
      throw new RuntimeException("Found a hacker! verifyMoveDone=" + verifyMoveDone);
    }
  }
}
