package org.luzhanqi.client;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Representation of the luzhanqi game state. The game state uses these keys:
 * BOARD: {pieceIdx...}//0-49, empty: -1 B: black pieces on board, W: white
 * pieces on board, D: discarded pieces MOVE: {from, to} //slotIdx DEPLOY:
 * DEPLOY
 **/

public class LuzhanqiState {
  private final Turn turn;
  private final ImmutableList<String> playerIds;

  /**
   * Note that some of the entries will have null, meaning the card is not
   * visible to us.
   */
  private ImmutableList<Slot> board;

  /**
   * Index of the white pieces, each integer is in the range [0-24]. Index of
   * the black pieces, each integer is in the range [25-49]. Index of the
   * discard pieces, each integer is in the range [0-49].
   */
  private final ImmutableList<Integer> white;
  private final ImmutableList<Integer> black;
  private final ImmutableList<Integer> discard;
  private final Optional<List<Integer>> dw;
  private final Optional<List<Integer>> db;
  private Optional<List<Integer>> move;
  private final boolean isDeploy;
  private String gameResult;
  private Turn winner;

  public LuzhanqiState(Turn turn, ImmutableList<String> playerIds,
      ImmutableList<Slot> board, ImmutableList<Integer> white,
      ImmutableList<Integer> black, ImmutableList<Integer> discard,
      Optional<List<Integer>> dw, Optional<List<Integer>> db,
      Optional<List<Integer>> move, boolean isDeploy) {
    super();
    this.turn = checkNotNull(turn);
    this.playerIds = checkNotNull(playerIds);
    this.board = checkNotNull(board);
    this.white = checkNotNull(white);
    this.black = checkNotNull(black);
    this.discard = checkNotNull(discard);
    this.dw = dw;
    this.db = db;
    this.move = move;
    this.isDeploy = isDeploy;
    this.gameResult = null;
    this.winner = null;
  }

  public LuzhanqiState(Turn turn, ImmutableList<String> playerIds,
      ImmutableList<Slot> board, ImmutableList<Integer> white,
      ImmutableList<Integer> black, ImmutableList<Integer> discard,
      Optional<List<Integer>> dw, Optional<List<Integer>> db,
      Optional<List<Integer>> move, boolean isDeploy, String gameResult,
      Turn winner) {
    super();
    this.turn = checkNotNull(turn);
    this.playerIds = checkNotNull(playerIds);
    this.board = checkNotNull(board);
    this.white = checkNotNull(white);
    this.black = checkNotNull(black);
    this.discard = checkNotNull(discard);
    this.dw = dw;
    this.db = db;
    this.move = move;
    this.isDeploy = isDeploy;
    this.gameResult = gameResult;
    this.winner = winner;
  }

  public Turn getTurn() {
    return turn;
  }

  public ImmutableList<Slot> getBoard() {
    return board;
  }

  public void setBoard(ImmutableList<Slot> board) {
    this.board = board;
  }

  public List<Integer> getApiBoard() {
    List<Integer> apiBoard = new ArrayList<Integer>();
    for (Slot s : board) {
      if (s.getPiece() == null) {
        apiBoard.add(-1);
      } else {
        apiBoard.add(s.getPiece().getKey());
      }
    }
    return apiBoard;
  }

  public ImmutableList<String> getPlayerIds() {
    return playerIds;
  }

  public String getPlayerId(Turn color) {
    return playerIds.get(color.ordinal());
  }

  public ImmutableList<Integer> getWhite() {
    return white;
  }

  public ImmutableList<Integer> getBlack() {
    return black;
  }

  public Optional<List<Integer>> getDW() {
    return dw;
  }

  public Optional<List<Integer>> getDB() {
    return db;
  }

  public boolean boardEmpty() {
    for (Slot slot : board) {
      if (!slot.emptySlot())
        return false;
    }
    return true;
  }

  public ImmutableList<Integer> getWhiteOrBlack(Turn turn) {
    return turn.isWhite() ? white : black;
  }

  public ImmutableList<Integer> getDiscard() {
    return discard;
  }

  public Optional<List<Integer>> getMove() {
    return move;
  }

  public void setMove(Optional<List<Integer>> move) {
    this.move = move;
  }

  public boolean getIsDeploy() {
    return isDeploy;
  }

  public String getGameResult() {
    return this.gameResult;
  }

  public void setGameResult(String gameResult) {
    this.gameResult = gameResult;
  }

  public Turn getWinner() {
    return this.winner;
  }

  public void setWinner(Turn turn) {
    this.winner = turn;
  }

  public LuzhanqiState copy() {
    List<Slot> newBoard = new ArrayList<Slot>();
    for (Slot slot : board) {
      Slot newSlot = new Slot(slot.getKey(), slot.getPieceKey());
      newBoard.add(newSlot);
    }
    return new LuzhanqiState(
        turn,
        ImmutableList.copyOf(playerIds),
        ImmutableList.copyOf(newBoard),
        white,
        black,
        discard,
        dw,
        db,
        Optional.fromNullable((List<Integer>) ImmutableList.copyOf(move.get())),
        isDeploy, gameResult, winner);
  }
}
