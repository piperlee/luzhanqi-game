package org.luzhanqi.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.luzhanqi.client.Piece.PieceType;

/**
 * Explores the state graph.
 */
interface StateExplorer {
  /**
   * Returns all the possible moves from the given state.
   * For example, from the initial state we have 16 possible moves
   * for the 8 pawns (each pawn can move either 1 or 2 squares)
   * and 4 possible moves for the two knights.
   * So in total, 
   *   getPossibleMoves(new State()) 
   * should return a list with 20 moves.
   */
  Set<List<Slot>> getPossibleMoves(LuzhanqiState state);

  /**
   * Returns the possible moves from the given state that begin at start.
   * For example, 
   *   getPossibleMovesFromPosition(new State(), new Position(1,0)) 
   * should return a list with 2 moves for the pawn at position 1x0.
   */
  Set<List<Slot>> getPossibleMovesFromPosition(LuzhanqiState state, int start);
  
  /**
   * Returns the list of start positions of all possible moves.
   * For example, 
   *   getPossibleStartPositions(new State()) 
   * should return a list with 10 possible start positions:
   *   8 positions for the pawns (1x0 till 1x7)
   *   2 positions for the knights (0x1 and 0x6).
   */
  Set<Integer> getPossibleStartPositions(LuzhanqiState state);
}

public class StateExplorerImpl implements StateExplorer {
  private LuzhanqiLogic luzhanqiLogic=new LuzhanqiLogic();
  @Override
  public Set<List<Slot>> getPossibleMoves(LuzhanqiState state) {
    Set<Integer> startPosition=getPossibleStartPositions(state);
    Set<List<Slot>> possibleMoves=new HashSet<List<Slot>>();
    if(startPosition.size()!=0){
      for(int currentPosition:startPosition){
        possibleMoves.addAll(getPossibleMovesFromPosition(state,currentPosition));  
      }
    }
    return possibleMoves;
  }

  @Override
  public Set<List<Slot>> getPossibleMovesFromPosition(LuzhanqiState state, int start) {
    Slot from = state.getBoard().get(start);
    Piece currentPiece=from.getPiece();
    Set<List<Slot>> possibleMove=new HashSet<List<Slot>>();
    if(currentPiece==null) return possibleMove;
    List<Slot> move;
    for (int adj: from.getAdjSlots()) {
      Slot to = state.getBoard().get(adj);
      if (to.emptySlot()) {
        move = new ArrayList<Slot>();
        move.add(from);
        move.add(to);
        possibleMove.add(move);
      } else {
        Piece toPiece = to.getPiece();
        if (toPiece.getPlayer() != currentPiece.getPlayer()) {
          move = new ArrayList<Slot>();
          move.add(from);
          move.add(to);
          possibleMove.add(move);
        }
      }
      
      // on rail
      if (from.getOnRail() && to.getOnRail() && to.emptySlot()) {
        //TODO
      }
    }
    return possibleMove;
  }

  @Override
  public Set<Integer> getPossibleStartPositions(LuzhanqiState state) {
    Turn turn=(state.getTurn()==Turn.W)?Turn.W:Turn.B;
    Set<Integer> startPosition=new HashSet<Integer>(); 
    for (Slot from: state.getBoard()) {
      if (!from.emptySlot()) {
        Piece piece = from.getPiece();
        if (piece.getPlayer() == turn) {
          if (piece.getFace()!=PieceType.LANDMINE && piece.getFace()!=PieceType.FLAG) {
            startPosition.add(from.getKey());
          }
        }
      }
    }
    return startPosition;
  }
}

