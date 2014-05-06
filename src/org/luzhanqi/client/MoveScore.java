package org.luzhanqi.client;

// Modified code from: 

import java.util.List;


/**
 * This is a wrapper class that wraps move and score together.
 */
class MoveScore implements Comparable<MoveScore> {

  private List<Slot> move;
  private int score;

  /**
   * Constructor
   * @param move
   * @param score
   */
  MoveScore(List<Slot> move, int score){
    this.move = move;
    this.score = score;
  }
  
  /**
   * Gets the move.
   * @return
   */
  public List<Slot> getMove(){
    return move;
  }
  
  /**
   * Gets the score.
   * @return
   */
  public int getScore(){
    return score;
  }
  
  /**
   * Sets the move.
   * @param move
   */
  public void setMove(List<Slot> move){
    this.move = move;
  }
  
  /**
   * Sets the score.
   * @param score
   */
  public void setScore(int score){
    this.score = score;
  }
  
  /**
   * Overrrides the toCompare method of Comparable interface.
   */
  @Override
  public int compareTo(MoveScore o) {
    return o.score - score; // sort DESC (best score first)
  }
    
}