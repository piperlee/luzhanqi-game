package org.luzhanqi.client;

// Modified code from:

import java.util.List;

import com.google.gwt.user.client.Window;

public class Heuristic {
  
  private StateExplorerImpl stateExplorer;
  private int moveCount;
  private static final int ENDGAME=75;   // Start Endgame tactics after 75 moves
  private static final int MIDDLEGAME=50;  // Start Middlegame tactics after 50 moves.
  
  public Heuristic(){
    stateExplorer = new StateExplorerImpl();
  }
  
  public Iterable<List<Slot>> getOrderedMoves(LuzhanqiState state){
    
    return stateExplorer.getPossibleMoves(state);
    
  }
  
  public int getValueOfState(LuzhanqiState state){
    if(state.getWinner()!=null) return calculateGameEndedValue(state.getWinner());
    int valueOfState=0;
    for (Slot slot : state.getBoard()) {
      Piece currentPiece = slot.getPiece();
      if(currentPiece!=null) {
        valueOfState += getValueOfPiece(currentPiece);
        if(moveCount>=ENDGAME) {
          //TODO
          valueOfState += 3000;
        }
        else if(moveCount>=MIDDLEGAME){
          //TODO
          valueOfState += 5000;
        }        
      }
    }
    return valueOfState;
  }
  
  public int calculateGameEndedValue(Turn winner){
    if(winner==null) return 0;
    else if (winner.equals(Turn.B)) return 1000000; 
    else return -1000000;
  }
  
  public int getValueOfPiece(Piece piece){
    int multiplier = (piece.getPlayer().equals(Turn.B))?1:-1;
    switch(piece.getFace()){     
      case LANDMINE:return 1200*multiplier;
      case BOMB:return 1400*multiplier;
      case ENGINEER:return 1500*multiplier;      
      case LIEUTENANT:return 1300*multiplier;
      case CAPTAIN:return 1400*multiplier;
      case MAJOR:return 1500*multiplier;
      case COLONEL:return 1600*multiplier;
      case BRIGADIERGENERAL:return 1700*multiplier;
      case MAJORGENERAL:return 1800*multiplier;
      case GENERAL:return 1900*multiplier;
      case FIELDMARSHAL:return 2000*multiplier;
      case FLAG:return 20000*multiplier;
      default:return 0;
    }
  }
  
  public void increaseMoveCount(){
    ++moveCount;
  }
  
}
