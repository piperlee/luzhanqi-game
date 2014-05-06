package org.luzhanqi.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.game_api.GameApi.Delete;
import org.game_api.GameApi.EndGame;
import org.game_api.GameApi.Operation;
import org.game_api.GameApi.Set;
import org.game_api.GameApi.SetTurn;
import org.game_api.GameApi.SetVisibility;
import org.game_api.GameApi.VerifyMove;
import org.game_api.GameApi.VerifyMoveDone;
import org.luzhanqi.client.Piece.PieceType;
import org.luzhanqi.client.Slot.SlotType;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class LuzhanqiLogic {
  /** The entries used in the luzhanqi game are:
   *   turn, MOVE/DEPLOY, BOARD, W, B, D
   * When we send operations on these keys, it will always be in the above order.
   */
  private static final String sId = "1"; // turn S dummy id
  private static final String W = "W"; // White hand
  private static final String B = "B"; // Black hand
  private static final String D = "D"; // Middle pile
  private static final String BOARD = "board"; // map to a list contains board status
  private static final String DEPLOY = "deploy"; // moves at which players deploy pieces
  private static final String DB = "DB";
  private static final String DW = "DW";
  private static final String MOVE = "move"; // normal moves map to a list (fromSlot, toSlot)


  public VerifyMoveDone verify(VerifyMove verifyMove) {
    try {
      checkMoveIsLegal(verifyMove);
      return new VerifyMoveDone();
    } catch (Exception e) {
      return new VerifyMoveDone(verifyMove.getLastMovePlayerId(), e.getMessage());
    }
  }

  void checkMoveIsLegal(VerifyMove verifyMove) {
    List<Operation> lastMove = verifyMove.getLastMove();
    Map<String, Object> lastState = verifyMove.getLastState();
    // Checking the operations are as expected.
    List<Operation> expectedOperations = getExpectedOperations(
        lastState, lastMove, verifyMove.getPlayerIds(), verifyMove.getLastMovePlayerId());
    check(expectedOperations.equals(lastMove), expectedOperations, lastMove);
    // We use SetTurn, so we don't need to check that the correct player did the move.
    // However, we do need to check the first move is done by the white player (and then in the
    // first MakeMove we'll send SetTurn which will guarantee the correct player send MakeMove).
    if (lastState.isEmpty()) {
      //in luzhanqi black player initial the board
      check(verifyMove.getLastMovePlayerId().equals(verifyMove.getPlayerIds().get(0)));
    }
  }

  /** Returns the operations for deploy pieces. */
  List<Operation> deployPiecesMove(LuzhanqiState state, List<Integer> deployList,
    List<String> playerIds, String lastMovePlayerId) {
    /**
     * deploy pieces
     * 0) new SetTurn(BId)
     * 1) new Set(Deploy)
     * 2) new Set(DB/DW)
     * 3) new Set W 
     * 4) new Set B 
     * 5) new Set D
     */
    List<Operation> operations = Lists.newArrayList();
    // B first
    if(!state.getDB().isPresent() && !state.getDW().isPresent()) {
      operations.add(new SetTurn(sId));      
    } else {
      operations.add(new SetTurn(state.getPlayerId(Turn.B)));
    }
    operations.add(new Set(DEPLOY, DEPLOY));
    //BLACK deploy
    if (state.getPlayerId(Turn.B).equals(lastMovePlayerId)){
      //empty CAMPSITE
      check(deployList.get(6) == -1,"campsite must be empty");
      check(deployList.get(8) == -1,"campsite must be empty");
      check(deployList.get(12) == -1,"campsite must be empty");
      check(deployList.get(16) == -1,"campsite must be empty");
      check(deployList.get(18) == -1,"campsite must be empty");
      //flag position
      check(deployList.get(26) == 49 || deployList.get(28) == 49, "flag must be in one of the headquarters");
      //landmine and bomb position
      for (int i = 0; i < 19 ; i++){
        if (i >= 0 && i<= 4){
          check(deployList.get(i)!=44 && deployList.get(i)!=45, "illegal landmine/bomb position");
        }
        check(deployList.get(i)!=46 && deployList.get(i)!=47 && deployList.get(i)!=48
            , "illegal landmine/bomb position");
      }
      operations.add(new Set(DB,deployList));
      //operations.add(new SetVisibility(DB,ImmutableList.of(state.getPlayerId(Turn.B))));
    }//WHITE deploy
    else{
      //empty CAMPSITE
      check(deployList.get(11) == -1,"campsite must be empty");
      check(deployList.get(13) == -1,"campsite must be empty");
      check(deployList.get(17) == -1,"campsite must be empty");
      check(deployList.get(21) == -1,"campsite must be empty");
      check(deployList.get(23) == -1,"campsite must be empty");
      //flag position
      check(deployList.get(3)==24 || deployList.get(1)==24, "flag must be in one of the headquarters");
    //landmine and bomb position
      for (int i = 10; i < 29 ; i++){
        if (i>=25 && i<=29){
          check(deployList.get(i)!=19 && deployList.get(i)!=20, "illegal landmine/bomb position");
        }
        check(deployList.get(i)!=21 && deployList.get(i)!=22 && deployList.get(i)!=23
            , "illegal landmine/bomb position");
      }
      operations.add(new Set(DW,deployList));
      //operations.add(new SetVisibility(DW,ImmutableList.of(state.getPlayerId(Turn.W))));
    }
    operations.add(new Set(W, state.getWhite()));
    operations.add(new Set(B, state.getBlack()));
    operations.add(new Set(D, state.getDiscard()));
    return operations;
  }

  /** Returns the operations for B first move. */
  List<Operation> firstMove(LuzhanqiState state) {
     /**
      * first move
      * 0) new SetTurn(Id)
      * 1) new Delete(DEPLOY)
      * 2) new Delete(DW)
      * 3) new Delete(DB)
      * 4) new Set BOARD
      * 5) new Set W 
      * 6) new Set B 
      * 7) new Set D
      * 6-55) new SetVisibility(all)
      */
    List<Integer> board = (state.getDW().isPresent() && state.getDB().isPresent()) ?
        concat(state.getDW().get(),state.getDB().get()) : Lists.<Integer>newArrayList(); 
    List<Operation> operations = Lists.newArrayList();
    operations.add(new SetTurn(state.getPlayerId(Turn.B)));
    operations.add(new Delete(DEPLOY));
    operations.add(new Delete(DW));
    operations.add(new Delete(DB));
    operations.add(new Set(BOARD,board));
    operations.add(new Set(W,state.getWhite()));
    operations.add(new Set(B,state.getBlack()));
    operations.add(new Set(D,state.getDiscard()));
//    for(int i = 0; i <= 49; i++){
//      operations.add(new SetVisibility(String.valueOf(i)));
//    }
    return operations;
  }

  /** Returns the operations for making normal Move
   *  Move operation {MOVE,[1,2]} means move pieces from slot1 to slot2
   */
  List<Operation> normalMove(LuzhanqiState state, List<Integer> board, 
      List<Integer> pieceMove, List<String> playerIds) {
    /**
     *  Normal move
     *  0) new SetTurn(oppoId)
     *  1) new Set MOVE
     *  2) new Set BOARD
     *  3) new Set W 
     *  4) new Set B 
     *  5) new Set D
     *  6) optional new EndGame
     */
   
    List<Operation> operations = Lists.newArrayList();
    Turn turn = state.getTurn();
    operations.add(new SetTurn(state.getPlayerId(turn.getOppositeColor())));
    
    List<Integer> wHand = Lists.newArrayList(state.getWhite());
    List<Integer> bHand = Lists.newArrayList(state.getBlack());
    List<Integer> dHand = Lists.newArrayList(state.getDiscard());
    check(wHand.size()+bHand.size()+dHand.size()==50,"wrong pieces number");
    
    /** 
     * pieceMove:{fromSlotIdx,toSlotIdx}
     * Check if a pieceMove is valid, if so add it to operations
     */
    check(pieceMove.get(0)>=0 && pieceMove.get(1)<60, "out of board");
    
    operations.add(new Set(MOVE,pieceMove));
    ArrayList<Integer> apiBoard = Lists.newArrayList(board);
    Slot slotFrom = state.getBoard().get(pieceMove.get(0));
    Slot slotTo = state.getBoard().get(pieceMove.get(1));

    check(fromIsValid(state,slotFrom));
    /**
     *  Move with no beat
     */
    if (slotTo.emptySlot()){
      check(toIsValid(state,slotFrom,slotTo),"position invalid");
      apiBoard.set(slotFrom.getKey(), -1);
      apiBoard.set(slotTo.getKey(), slotFrom.getPiece().getKey());
      operations.add(new Set(BOARD,apiBoard));
      operations.add(new Set(W, state.getWhite()));
      operations.add(new Set(B, state.getBlack()));
      operations.add(new Set(D, state.getDiscard()));
    }
    /** 
     * Move with beat
     */
    else{
      check(toIsValid(state,slotFrom,slotTo),"position invalid");
     // check(beatValid(state,slotFrom,slotTo),"position invalid");
            
      //End Game-I: flag is beaten
      if (slotTo.getPiece().getFace()==PieceType.FLAG){        
        apiBoard.set(slotFrom.getKey(), -1);
        apiBoard.set(slotTo.getKey(), slotFrom.getPiece().getKey());
        operations.add(new Set(BOARD,apiBoard));
        if(turn == Turn.W){
          bHand.remove((Integer)slotTo.getPiece().getKey());
          dHand.add(slotTo.getPiece().getKey());
        }else{
          wHand.remove((Integer)slotTo.getPiece().getKey());
          dHand.add(slotTo.getPiece().getKey());
        }
        operations.add(new Set(W, wHand));
        operations.add(new Set(B, bHand));
        operations.add(new Set(D, dHand));
        operations.add(new EndGame(state.getPlayerId(turn)));
        state.setGameResult("OVER");
        state.setWinner(turn);
      }
      // Bomb meets other pieces, both off the board
      else if (slotFrom.getPiece().getFace()==PieceType.BOMB 
          || slotTo.getPiece().getFace()==PieceType.BOMB){  
        apiBoard.set(slotFrom.getKey(), -1);
        apiBoard.set(slotTo.getKey(), -1);
        operations.add(new Set(BOARD,apiBoard));
        if(turn == Turn.W){
          wHand.remove((Integer)slotFrom.getPiece().getKey());
          bHand.remove((Integer)slotTo.getPiece().getKey());          
        }else{
          bHand.remove((Integer)slotFrom.getPiece().getKey());
          wHand.remove((Integer)slotTo.getPiece().getKey());
        }
        dHand.add(slotFrom.getPiece().getKey());
        dHand.add(slotTo.getPiece().getKey());
        operations.add(new Set(W, wHand));
        operations.add(new Set(B, bHand));
        operations.add(new Set(D, dHand));        
      }
      // Engineer meets landmine, beat it
      else if (slotTo.getPiece().getFace()==PieceType.LANDMINE
          && slotFrom.getPiece().getFace()==PieceType.ENGINEER){
        apiBoard.set(slotFrom.getKey(), -1);
        apiBoard.set(slotTo.getKey(), slotFrom.getPiece().getKey());
        operations.add(new Set(BOARD,apiBoard));
        if(turn == Turn.W){
          bHand.remove((Integer)slotTo.getPiece().getKey());
        }else{
          wHand.remove((Integer)slotTo.getPiece().getKey());
        }
        dHand.add(slotTo.getPiece().getKey());
        operations.add(new Set(W, wHand));
        operations.add(new Set(B, bHand));
        operations.add(new Set(D, dHand));
      }
      // Others meets landmine, both off the board
      else if (slotTo.getPiece().getFace()==PieceType.LANDMINE){
        apiBoard.set(slotFrom.getKey(), -1);
        apiBoard.set(slotTo.getKey(), -1);
        operations.add(new Set(BOARD,apiBoard));
        if(turn == Turn.W){
          wHand.remove((Integer)slotFrom.getPiece().getKey());
          bHand.remove((Integer)slotTo.getPiece().getKey());          
        }else{
          bHand.remove((Integer)slotFrom.getPiece().getKey());
          wHand.remove((Integer)slotTo.getPiece().getKey());
        }
        dHand.add(slotFrom.getPiece().getKey());
        dHand.add(slotTo.getPiece().getKey());
        operations.add(new Set(W, wHand));
        operations.add(new Set(B, bHand));
        operations.add(new Set(D, dHand));
      }
      // General meeting, larger order beats smaller one, if equal both off the board
      else{
        int survived = (slotFrom.getPiece().getOrder()>slotTo.getPiece().getOrder())
            ?slotFrom.getPiece().getKey():slotTo.getPiece().getKey();
        int dead = (slotFrom.getPiece().getOrder()<slotTo.getPiece().getOrder())
            ?slotFrom.getPiece().getKey():slotTo.getPiece().getKey();
        apiBoard.set(slotFrom.getKey(), -1);
        apiBoard.set(slotTo.getKey(), 
            (slotFrom.getPiece().getOrder()==slotTo.getPiece().getOrder())?-1:survived);
        operations.add(new Set(BOARD,apiBoard));
        if(slotFrom.getPiece().getOrder()==slotTo.getPiece().getOrder()){
          if(turn == Turn.W){
            wHand.remove((Integer)slotFrom.getPiece().getKey());
            bHand.remove((Integer)slotTo.getPiece().getKey());          
          }else{
            bHand.remove((Integer)slotFrom.getPiece().getKey());
            wHand.remove((Integer)slotTo.getPiece().getKey());
          }
          dHand.add(slotFrom.getPiece().getKey());
          dHand.add(slotTo.getPiece().getKey());
        }else{
          if(dead>=25){
            bHand.remove((Integer)dead);        
          }else{
            wHand.remove((Integer)dead);
          }
          dHand.add(dead);
        }
        operations.add(new Set(W, wHand));
        operations.add(new Set(B, bHand));
        operations.add(new Set(D, dHand));
      }
      //End Game-II: no more piece to move
      boolean noneToMove = true;
      if(turn == Turn.W){
        for(int i:bHand){
          if(i!=49 && i!=46 && i!=47 && i!= 48){
            noneToMove = false;
            break;
          }
        }      
      }else{
        for(int i:wHand){
          if(i!=24 && i!=21 && i!=22 && i!= 23){
            noneToMove = false;
            break;
          }
        }   
      }
      if (noneToMove){
        operations.add(new EndGame(state.getPlayerId(turn)));        
        state.setGameResult("OVER");
        state.setWinner(turn);
      }
    }    
    return operations;
  }
  
  boolean fromIsValid(LuzhanqiState state, Slot slotFrom){
    // empty slot from OR move other's piece
    if (slotFrom.getPiece() == null 
        || slotFrom.getPiece().getPlayer()!=state.getTurn())
      return false;
    // flag, landmine move
    if (slotFrom.getPiece().getFace() == PieceType.FLAG
        || slotFrom.getPiece().getFace() == PieceType.LANDMINE)
      return false;
    
    return true;
  }
  /**
   * Check if a move from slotFrom to slotTo has a valid path, regardless pieces
   * beat with each other.
   */
  boolean toIsValid(LuzhanqiState state, Slot slotFrom, Slot slotTo){    
    // move on one's own piece
    if (!slotTo.emptySlot()
        && slotTo.getPiece().getPlayer() == slotFrom.getPiece().getPlayer())
      return false;
    // same slot move
    if (slotFrom.getKey() == slotTo.getKey()) return false;
    // attack other's in CAMPSITE piece
    if (!slotTo.emptySlot() && slotTo.getType() == SlotType.CAMPSITE)
      return false;

    if (!slotFrom.isAdj(slotTo.getKey())){
      //slotTo has to be onRail
      if(!slotTo.getOnRail() || !slotFrom.getOnRail()) return false;
      if (slotFrom.getPiece().getFace() == PieceType.ENGINEER){
        if(!engineerOnRail(state,slotFrom,slotTo)) return false;
      }else{
        int iFrom = slotFrom.getKey()/5;
        int jFrom = slotFrom.getKey()%5;
        int iTo = slotTo.getKey()/5;
        int jTo = slotTo.getKey()%5;
        //make no turns
        if(iFrom!=iTo && jFrom!=jTo) return false;
        if((slotFrom.getKey() == 26 && slotTo.getKey() == 31)
            ||(slotFrom.getKey() == 31 && slotTo.getKey() == 26)
            ||(slotFrom.getKey() == 28 && slotTo.getKey() == 33)
            ||(slotFrom.getKey() == 33 && slotTo.getKey() == 28))
          return false;
        //no block
        if(iFrom == iTo){
          for(int j=Math.min(jFrom,jTo)+1; j<Math.max(jFrom,jTo); j++){
            if(state.getBoard().get(iFrom*5+j).getPiece()!=null) return false;
          }
        }else if(jFrom == jTo){
          for(int i=Math.min(iFrom,iTo)+1; i<Math.max(iFrom,iTo); i++){
            if(state.getBoard().get(i*5+jFrom).getPiece()!=null)
              return false;
          }
        }
      }
    }
    return true;
  }
  
  /**
   * When ENGINEER moves on rail, it could move to any other slot on rail as long as
   * it is not blocked by other pieces. Search all possible path from slot "from" to 
   * slot "to", using BFS.
   */
  boolean engineerOnRail(LuzhanqiState state, Slot from, Slot to){
    LinkedList<Slot> Q = new LinkedList<Slot>();
    boolean [] visited = new boolean [60];
    visited[from.getKey()] = true;
    Q.add(from);
    while (!Q.isEmpty()){
      Slot cur = Q.removeFirst();
      for (int i:cur.getAdjSlots()){
        if(state.getBoard().get(i).getKey()==to.getKey()){
          return true;
        }
        if (state.getBoard().get(i).getOnRail()){
          if ((!visited[i]) 
              && state.getBoard().get(i).emptySlot()){
            Q.add(state.getBoard().get(i));
            visited[i] = true;
          }
        }
      }
    }
    return false;
  }

  /**
   * Calculate the expected operations based on lastState, and check
   * 1) if deploy pieces: BOARD object
   * 2) if first move: SetVisibility
   * 3) if normal move: MOVE object from last move.
   */
  @SuppressWarnings("unchecked")
  List<Operation> getExpectedOperations(
      Map<String, Object> lastApiState, List<Operation> lastMove, List<String> playerIds,
      String lastMovePlayerId) {
    if (lastApiState.isEmpty()) {
      return getInitialMove(playerIds);
    }
    // remember to deal with W:0, B:1, S:2
    LuzhanqiState lastState = gameApiStateToLuzhanqiState(lastApiState,
        Turn.values()[playerIds.indexOf(lastMovePlayerId)], playerIds);
    // There are 2 types of moves:
    // 1) deploy pieces
    // 2) first move B
    // 3) normal move
    if (lastMove.contains(new Set(DEPLOY,DEPLOY))) {        
      Set setDWB = (Set)lastMove.get(2);
//      Set setDB = (Set)lastMove.get(3);
      return deployPiecesMove(lastState, 
          (List<Integer>)setDWB.getValue(), 
          playerIds, lastMovePlayerId);    
    }else if (lastMove.contains(new Delete(DW))) {
      return firstMove(lastState);
    }else {
      //Set setBoard = (Set)lastMove.get(2);
      Set setMove = (Set)lastMove.get(1);
      return normalMove(lastState, (List<Integer>)lastApiState.get(BOARD), 
          (List<Integer>)setMove.getValue(), playerIds);
    }
  }

  List<Operation> getInitialMove(List<String> playerIds){
    String whitePlayerId = playerIds.get(0);
    String blackPlayerId = playerIds.get(1);
    List<Operation> operations = Lists.newArrayList();
    /**
     *  Initial move
     *  0) new SetTurn(sId)
     *  1) new Set BOARD
     *  2) new Set W 
     *  3) new Set B 
     *  4) new Set D
     *  5) new SetVisibility(0-49)
     */
    operations.add(new SetTurn(sId));
    // set board
    operations.add(new Set(BOARD,ImmutableList.of(
          -1,-1,-1,-1,-1,
          -1,-1,-1,-1,-1,
          -1,-1,-1,-1,-1,
          -1,-1,-1,-1,-1,
          -1,-1,-1,-1,-1,
          -1,-1,-1,-1,-1,
          -1,-1,-1,-1,-1,
          -1,-1,-1,-1,-1,
          -1,-1,-1,-1,-1,
          -1,-1,-1,-1,-1,
          -1,-1,-1,-1,-1,
          -1,-1,-1,-1,-1)));
    // set W and B hands
    operations.add(new Set(W, ImmutableList.of(
        0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24)));
    operations.add(new Set(B, ImmutableList.of(
        25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49)));
    // discard pile is empty
    operations.add(new Set(D, ImmutableList.of()));
    // sets visibility
    for (int i = 0; i < 25; i++) {
      operations.add(new SetVisibility(String.valueOf(i), ImmutableList.of(whitePlayerId)));
    }
    for (int i = 25; i < 50; i++) {
      operations.add(new SetVisibility(String.valueOf(i), ImmutableList.of(blackPlayerId)));
    }
    return operations;
  }

  @SuppressWarnings("unchecked")
  LuzhanqiState gameApiStateToLuzhanqiState(Map<String, Object> gameApiState,
      Turn turn, List<String> playerIds) {
    if(gameApiState.isEmpty()){
      return null;
    }
    List<Integer> apiBoard = (List<Integer>) gameApiState.get(BOARD);   
    List<Slot> board = getBoardFromApiBoard(apiBoard);
    List<Integer> white = (List<Integer>) gameApiState.get(W);
    List<Integer> black = (List<Integer>) gameApiState.get(B);
    List<Integer> discard = (List<Integer>) gameApiState.get(D);
    return new LuzhanqiState(
        turn,
        ImmutableList.copyOf(playerIds),
        ImmutableList.copyOf(board),
        ImmutableList.copyOf(white), ImmutableList.copyOf(black),
        ImmutableList.copyOf(discard),
        Optional.fromNullable((List<Integer>) gameApiState.get(DW)),
        Optional.fromNullable((List<Integer>) gameApiState.get(DB)),
        Optional.fromNullable((List<Integer>) gameApiState.get(MOVE)),
        gameApiState.containsKey(DEPLOY));
  }

  static List<Slot> getBoardFromApiBoard(List<Integer> apiBoard){
    List<Slot> board = Lists.newArrayList();
    for (int i = 0; i < 60; i++) {
      Slot slot = new Slot(i,apiBoard.get(i));
      board.add(slot);
    }
    return board;
  }
  // copy from CheatLogic.java
  <T> List<T> concat(List<T> a, List<T> b) {
    return Lists.newArrayList(Iterables.concat(a, b));
  }

  <T> List<T> subtract(List<T> removeFrom, List<T> elementsToRemove) {
    check(removeFrom.containsAll(elementsToRemove), removeFrom, elementsToRemove);
    List<T> result = Lists.newArrayList(removeFrom);
    result.removeAll(elementsToRemove);
    check(removeFrom.size() == result.size() + elementsToRemove.size());
    return result;
  }
  
  private void check(boolean val, Object... debugArguments) {
    if (!val) {
      throw new RuntimeException("We have a hacker! debugArguments="
          + Arrays.toString(debugArguments));
    }
  }
}

