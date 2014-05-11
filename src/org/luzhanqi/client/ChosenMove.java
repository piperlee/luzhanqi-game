package org.luzhanqi.client;

import java.util.ArrayList;
import java.util.List;

/**
 * Credit to: https://code.google.com/p/nyu-gaming-course-2013/source/browse/trunk/eclipse/src/org/ashishmanral/hw9/ChosenMove.java  
 * @author Ashish
 * This class picks one of the moves with highest same scores.
 */

public class ChosenMove {
  
  private List<MoveScore> moveScore=new ArrayList<MoveScore>();
  private int lastScore=-12345;
  
  /**
   * Populates the moveScore arraylist.
   * @param ms
   */
  public void populate(MoveScore ms){
    if(lastScore==-12345) {
      lastScore = ms.getScore();
    }
    if(lastScore==ms.getScore()){
      moveScore.add(ms);
    }
  }
  
  /**
   * Returns a move with highest value.
   * @return
   */
  public MoveScore getChosenMove(){
    int size = moveScore.size();
    int position = (int)(Math.random()*size);
    return moveScore.get(position);
  }
}