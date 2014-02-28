package org.luzhanqi.client;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Representation of the luzhanqi game state.
 * The game state uses these keys: BOARD: {pieceIdx...}//0-49, empty: -1
 *                                 B: black pieces on board, 
 *                                 W: white pieces on board,
 *                                 D: discarded pieces
 *                                 MOVE: {from, to} //slotIdx
 *                                 DEPLOY: DEPLOY
 **/

public class LuzhanqiState {
  private final Turn turn;
  private final ImmutableList<Integer> playerIds;

  /**
   * Note that some of the entries will have null, meaning the card is not visible to us.
   */
  private final ImmutableList<Slot> board;

  /**
   * Index of the white pieces, each integer is in the range [0-24].
   * Index of the black pieces, each integer is in the range [25-49].
   * Index of the discard pieces, each integer is in the range [0-49].
   */
  private final ImmutableList<Integer> white;
  private final ImmutableList<Integer> black;
  private final ImmutableList<Integer> discard;
  private final Optional<List<Integer>> move;
  private final boolean isDeploy;
 

  public LuzhanqiState(Turn turn, ImmutableList<Integer> playerIds,
      ImmutableList<Slot> board, ImmutableList<Integer> white,
      ImmutableList<Integer> black, ImmutableList<Integer> discard, 
      Optional<List<Integer>> move, boolean isDeploy) {
    super();
    this.turn = checkNotNull(turn);
    this.playerIds = checkNotNull(playerIds);
    this.board = checkNotNull(board);
    this.white = checkNotNull(white);
    this.black = checkNotNull(black);
    this.discard = checkNotNull(discard);
    this.move = move;
    this.isDeploy = isDeploy;
  }

  public Turn getTurn() {
    return turn;
  }
  
  public ImmutableList<Slot> getBoard(){
    return board;
  }
  
  public List<Integer> getApiBoard(){
    List<Integer> apiBoard = new ArrayList<Integer>();
    for(Slot s: board){
      if(s.getPiece() == null){
        apiBoard.add(-1);
      }else{
        apiBoard.add(s.getPiece().getKey());
      }
    }
    return apiBoard;
  }

  public ImmutableList<Integer> getPlayerIds() {
    return playerIds;
  }

  public int getPlayerId(Turn color) {
    return playerIds.get(color.ordinal());
  }

  public ImmutableList<Integer> getWhite() {
    return white;
  }

  public ImmutableList<Integer> getBlack() {
    return black;
  }

  public ImmutableList<Integer> getWhiteOrBlack(Turn turn) {
    return turn.isWhite() ? white : black;
  }

  public ImmutableList<Integer> getDiscard() {
    return discard;
  }
  
  public Optional<List<Integer>> getMove(){
    return move;
  }
  
  public boolean getIsDeploy(){
    return isDeploy;
  }
}
