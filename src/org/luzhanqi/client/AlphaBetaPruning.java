package org.luzhanqi.client;

//Copyright 2012 Google Inc.
//
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
////////////////////////////////////////////////////////////////////////////////

import java.util.Collections;
import java.util.List;

import org.luzhanqi.client.Piece.PieceType;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * http://en.wikipedia.org/wiki/Alpha-beta_pruning<br>
 * This algorithm performs both A* and alpha-beta pruning.<br>
 * The set of possible moves is maintained ordered by the current heuristic value of each move. We
 * first use depth=1, and update the heuristic value of each move, then use depth=2, and so on until
 * we get a timeout or reach maximum depth. <br>
 * If a state has {@link TurnBasedState#whoseTurn} null (which happens in backgammon when we should
 * roll the dice), then I treat all the possible moves with equal probabilities. <br>
 * 
 * @author yzibin@google.com (Yoav Zibin)
 */

public class AlphaBetaPruning {
  
  static class TimeoutException extends RuntimeException {
    private static final long serialVersionUID = 1L;
  }

  private Heuristic heuristic;

  /**
   * Constructor
   */
  public AlphaBetaPruning() {
    this.heuristic = new Heuristic();
  }

  /**
   * Finds the best move for AI.
   * @param state
   * @param depth
   * @param timer
   * @return
   */
  public List<Slot> findBestMove(LuzhanqiState state, int depth, MyTimer timer) {
    boolean isAIWhite = state.getTurn().isWhite();
    // Do iterative deepening (A*), and slow get better heuristic values for the states.
    List<MoveScore> scores = Lists.newArrayList();
    {
      Iterable<List<Slot>> possibleMoves = heuristic.getOrderedMoves(state);
      for (List<Slot> move : possibleMoves) {
        MoveScore score = new MoveScore(move, Integer.MIN_VALUE);
        scores.add(score);
      }
    }

    try {
      for (int i = 0; i < depth; i++) {
        for (MoveScore moveScore : scores) {
          LuzhanqiState stateCopy = state.copy();
          stateChange(stateCopy, moveScore.getMove());
          int score = findMoveScore(stateCopy, i, Integer.MIN_VALUE, Integer.MAX_VALUE, timer);
          if (!isAIWhite) {
            // the scores are from the point of view of the white, so for black we need to switch.
            score = -score;
          }
          moveScore.setScore(score);
        }
        Collections.sort(scores); // This will give better pruning on the next iteration.
      }
    } catch (TimeoutException e) {
      Collections.sort(scores);
      ChosenMove chosenMove = new ChosenMove();
      for(MoveScore moveScore : scores){
          chosenMove.populate(moveScore);
        }
      heuristic.increaseMoveCount();
    return chosenMove.getChosenMove().getMove();
    }
    Collections.sort(scores);
    ChosenMove chosenMove = new ChosenMove();
    for(MoveScore moveScore : scores){
      chosenMove.populate(moveScore);
    }
    return chosenMove.getChosenMove().getMove();
  }
  
  public List<Slot> findBestMove(LuzhanqiState state, int depth) {
    boolean isAIWhite = state.getTurn().isWhite();
    // Do iterative deepening (A*), and slow get better heuristic values for the states.
    List<MoveScore> scores = Lists.newArrayList();
    {
      Iterable<List<Slot>> possibleMoves = heuristic.getOrderedMoves(state);
      for (List<Slot> move : possibleMoves) {
        MoveScore score = new MoveScore(move, Integer.MIN_VALUE);
        scores.add(score);
      }
    }

    for (int i = 0; i < depth; i++) {
      for (MoveScore moveScore : scores) {
        LuzhanqiState stateCopy = state.copy();
        stateChange(stateCopy, moveScore.getMove());
        int score = findMoveScore(stateCopy, i, Integer.MIN_VALUE, Integer.MAX_VALUE);
        if (!isAIWhite) {
          // the scores are from the point of view of the white, so for black we need to switch.
          score = -score;
        }
        moveScore.setScore(score);
      }
      Collections.sort(scores); // This will give better pruning on the next iteration.
    }
    Collections.sort(scores);
    ChosenMove chosenMove = new ChosenMove();
    for(MoveScore moveScore : scores){
      chosenMove.populate(moveScore);
    }
    return chosenMove.getChosenMove().getMove();
  }

  /**
   * Finds out the score for a move.
   * @param state
   * @param depth
   * @param alpha
   * @param beta
   * @param timer
   * @return
   * @throws TimeoutException
   */
  private int findMoveScore(LuzhanqiState state, int depth, int alpha, int beta, MyTimer timer)
      throws TimeoutException {
  if (timer.didTimeout()) {
      throw new TimeoutException();
    }
    if (depth == 0 || state.getGameResult() != null) {
      return heuristic.getValueOfState(state);
    }
    Turn playerColor = state.getTurn();
    int scoreSum = 0;
    int count = 0;
    Iterable<List<Slot>> possibleMoves = heuristic.getOrderedMoves(state);
    for (List<Slot> move : possibleMoves) {
      count++;
      LuzhanqiState stateCopy = state.copy();
      stateChange(stateCopy, move);
      int childScore = findMoveScore(stateCopy, depth - 1, alpha, beta, timer);
      if (playerColor == null) {
        scoreSum += childScore;
      } else if (playerColor.isWhite()) {
        alpha = Math.max(alpha, childScore);
        if (beta <= alpha) {
          break;
        }
      } else {
        beta = Math.min(beta, childScore);
        if (beta <= alpha) {
          break;
        }
      }
    }
    return playerColor == null ? scoreSum / count : playerColor.isWhite() ? alpha : beta;
  }
  
  private int findMoveScore(LuzhanqiState state, int depth, int alpha, int beta) {  
    if (depth == 0 || state.getGameResult() != null) {
      return heuristic.getValueOfState(state);
    }
    Turn playerColor = state.getTurn();
    int scoreSum = 0;
    int count = 0;
    Iterable<List<Slot>> possibleMoves = heuristic.getOrderedMoves(state);
    for (List<Slot> move : possibleMoves) {
      count++;
      LuzhanqiState stateCopy = state.copy();
      stateChange(stateCopy, move);
      int childScore = findMoveScore(stateCopy, depth - 1, alpha, beta);
      if (playerColor == null) {
        scoreSum += childScore;
      } else if (playerColor.isWhite()) {
        alpha = Math.max(alpha, childScore);
        if (beta <= alpha) {
          break;
        }
      } else {
        beta = Math.min(beta, childScore);
        if (beta <= alpha) {
          break;
        }
      }
    }
    return playerColor == null ? scoreSum / count : playerColor.isWhite() ? alpha : beta;
  }
  
  private void stateChange(LuzhanqiState state, List<Slot> move) {
    if (move.size() == 2) {
      Slot from = move.get(0);
      Slot to = move.get(1);
      List<Integer> m = ImmutableList.<Integer>of(from.getKey(),to.getKey());
      state.setMove(Optional.fromNullable((List<Integer>)m));
      List<Slot> board = state.getBoard();
      if (to.emptySlot()) {
        //System.out.println("from:"+from.getKey()+";to:"+to.getKey());
        board.get(to.getKey()).setPiece(board.get(from.getKey()).getPiece());       
      } else {
        if (from.getPiece().getFace() == to.getPiece().getFace()
            || from.getPiece().getFace() == PieceType.BOMB
            || to.getPiece().getFace() == PieceType.BOMB
            || to.getPiece().getFace() == PieceType.LANDMINE) {
          board.get(to.getKey()).setEmpty();
        } else {
          board.get(to.getKey()).setPiece(board.get(from.getKey()).getPiece());
        }
      }
      board.get(from.getKey()).setEmpty();
    }
  }
}