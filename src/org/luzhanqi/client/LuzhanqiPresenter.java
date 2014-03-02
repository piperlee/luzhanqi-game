package org.luzhanqi.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.luzhanqi.client.GameApi.Container;
import org.luzhanqi.client.GameApi.Delete;
import org.luzhanqi.client.GameApi.Operation;
import org.luzhanqi.client.GameApi.SetTurn;
import org.luzhanqi.client.GameApi.UpdateUI;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * The presenter that controls the luzhanqi graphics.
 * We use the MVP pattern:
 * the model is {@link LuzhanqiState},
 * the view will have the cheat graphics and it will implement {@link LuzhanqiPresenter.View},
 * and the presenter is {@link LuzhanqiPresenter}.
 */
public class LuzhanqiPresenter {
  /**
   * The possible luzhanqi messages.
   * The luzhanqi related messages are:
   * IS_DEPLOY: during the deploy phase of the game.
   * NORMAL_MOVE: during normal move.
   * FIRST_MOVE: player b does the first move.
   */
  public enum LuzhanqiMessage {
    IS_DEPLOY, NORMAL_MOVE, FIRST_MOVE, WAIT
  }

  public interface View {
    /**
     * Sets the presenter. The viewer will call certain methods on the presenter, e.g.,
     * when a piece is selected ({@link #pieceDeploy}),
     * when selection is done ({@link #finishedDeployingPieces}), etc.
     *
     * The process of deploying pieces looks as follows to the viewer:
     * 1) The viewer calls {@link #pieceDeploy} a couple of times to select the piece to drop to
     * certain slot
     * 2) The viewer calls {@link #finishedDeployingPieces} to finalize his selection
     * The process of deploying pieces looks as follows to the presenter:
     * 1) The presenter calls {@link #deployNextPiece(Map)} and passes the current selection.
     * 
     */
    void setPresenter(LuzhanqiPresenter luzhanqiPresenter);

    /** Sets the state for a viewer, i.e., not one of the players. */
    void setViewerState(int numberOfWhitePieces, int numberOfBlackPieces,
        int numberOfDicardPieces, List<Slot> board,
        LuzhanqiMessage luzhanqiMessage);

    /**
     * Sets the state for a player (whether the player has the turn or not).
     * The "Finish Deploy" button should be enabled only for LuzhanqiMessage.IS_DEPLOY.
     * The "OK Move" button should be enabled only for LuzhanqiMessage.NORMAL_MOVE.
     */
    void setPlayerState(int numberOfOpponentPieces,
        int numberOfDiscardPieces, List<Slot> board,
        LuzhanqiMessage luzhanqiMessage);

    /**
     * Asks the player to choose the next piece or finish his selection.
     * We pass a deployMap which maps certain piece to its deployed slot.
     * The user can either select a piece to deploy (by calling {@link #pieceDeploy),
     * or finish selecting
     * (by calling {@link #finishedDeployingPieces}; only allowed if deployMap.size==25).
     * if a user select a piece which is already in the map, it will update the map with 
     * the new slot position(if it is empty), otherwise ignore.
     */
    //void deployNextPiece(Map<Piece,Slot> deployMap);
    void deployNextPiece(Map<Piece,Optional<Slot>> lastDeploy);

    /**
     * Asks the player to choose the normal move or finish his selection.
     * We pass a fromTo List which contains fromSlot and ToSlot.
     * The user can either select a pair of fromTo (by calling {@link #moveSelected})
     * or finish selecting (by calling {@link #finishedNormalMove}; only if fromTo.size==2)
     * 
     */  
    void nextFromTo(List<Slot> fromTo);
  }

  private final LuzhanqiLogic luzhanqiLogic = new LuzhanqiLogic();
  private final View view;
  private final Container container;
  /** A viewer doesn't have a color. */
  private Optional<Turn> myTurn;
  private LuzhanqiState luzhanqiState;
  private List<Integer> apiBoard;
  private List<Slot> fromTo;
  //TODO: for test
  public  HashMap<Piece,Slot> deployMap; 
  private HashMap<Piece,Optional<Slot>> lastDeploy;
  private List<Integer> playerIds;
  private int yourPlayerId;
    
  public LuzhanqiPresenter(View view, Container container) {
    this.view = view;
    this.container = container;
    view.setPresenter(this);
  }

  public Turn getTurn(){
    return myTurn.get();
  }
  
  public LuzhanqiState getState(){
    return luzhanqiState;
  }
  /** Updates the presenter and the view with the state in updateUI. */
    public void updateUI(UpdateUI updateUI) {
      playerIds = updateUI.getPlayerIds();
      yourPlayerId = updateUI.getYourPlayerId();
      int yourPlayerIndex = updateUI.getPlayerIndex(yourPlayerId);
      // turn s has a dummy player id
      myTurn = yourPlayerIndex == 0 ? Optional.of(Turn.W)
          : yourPlayerIndex == 1 ? Optional.of(Turn.B) 
          : yourPlayerIndex == 2 ? Optional.of(Turn.S) : Optional.<Turn>absent();
          
      if (updateUI.getState().isEmpty()) {
        // The W player sends the initial setup move.
        if (myTurn.isPresent() && myTurn.get().isBlack()) {
          sendInitialMove(playerIds);
        }
        return;
      }
      Turn turnOfColor = null;
      for (Operation operation : updateUI.getLastMove()) {
        if (operation instanceof SetTurn) {
          int pId = ((SetTurn) operation).getPlayerId();
          turnOfColor = pId==1 ? Turn.S : Turn.values()[playerIds.indexOf(pId)];
        }
      }
      deployMap = new HashMap<Piece,Slot>();
      fromTo = new ArrayList<Slot>();
      lastDeploy = new HashMap<Piece,Optional<Slot>>();
      luzhanqiState = 
          luzhanqiLogic.gameApiStateToLuzhanqiState(updateUI.getState(), turnOfColor, playerIds);
      //apiBoard = getApiBoard(luzhanqiState.getBoard());
      
      if (updateUI.isViewer()) {
        view.setViewerState(luzhanqiState.getWhite().size(), luzhanqiState.getBlack().size(),
           luzhanqiState.getDiscard().size() ,luzhanqiState.getBoard(), getLuzhanqiMessage());
        return;
      }
      if (updateUI.isAiPlayer()) {
        // TODO: implement AI in a later HW!
        //container.sendMakeMove(..);
        return;
      }
      // Must be a player!
      Turn myT = myTurn.get();
      Turn thisT = luzhanqiState.getTurn();
      // if S turn
      if(thisT == Turn.S){
        view.setPlayerState(25, luzhanqiState.getDiscard().size(), 
            luzhanqiState.getBoard(), getLuzhanqiMessage());
        deployNextPiece();     
      }else{
        Turn opponent = myT.getOppositeColor();
        int numberOfOpponentCards = luzhanqiState.getWhiteOrBlack(opponent).size();
        view.setPlayerState(numberOfOpponentCards, luzhanqiState.getDiscard().size(), 
            luzhanqiState.getBoard(), getLuzhanqiMessage());
        if (isMyTurn() || getLuzhanqiMessage()==LuzhanqiMessage.FIRST_MOVE) {          
          //TODO: what triggers first MOVE
          if(getLuzhanqiMessage()==LuzhanqiMessage.FIRST_MOVE){
            if (myTurn.isPresent() && myTurn.get().isBlack()) {
              firstMove();            
            }          
          } else if (getLuzhanqiMessage()==LuzhanqiMessage.NORMAL_MOVE){
            // Choose the next card only if the game is not over
            if (!endGame(luzhanqiState))
              nextFromTo();
          }
        }
      }   
    }

  // to check if the game state is an end game state
  private boolean endGame(LuzhanqiState state){
    if(state.getDiscard().contains(24) || state.getDiscard().contains(49))
      return true;
    boolean blackNoMove = true;
    for(Integer i: state.getBlack()){
      if(i < 46){ 
        blackNoMove=false;
        break;
      }
    }
    boolean whiteNoMove = true;
    for(Integer i: state.getWhite()){
      if(i < 21){ 
        whiteNoMove=false;
        break;
      }
    }
    return blackNoMove || whiteNoMove;
  }
  
  // get certain LuzhanqiMessage by current game state
  private LuzhanqiMessage getLuzhanqiMessage() {
    if (luzhanqiState.getTurn() == Turn.S) {
      return LuzhanqiMessage.IS_DEPLOY;
    }
    if (luzhanqiState.getMove().isPresent() || !luzhanqiState.boardEmpty())
      return LuzhanqiMessage.NORMAL_MOVE;
    if ((luzhanqiState.getDB().isPresent() && luzhanqiState.getDW().isPresent())) {
      return LuzhanqiMessage.FIRST_MOVE;
    }    
    return LuzhanqiMessage.WAIT;
  }

  // check if this turn is my turn
  private boolean isMyTurn() {
    return myTurn.isPresent() && myTurn.get() == luzhanqiState.getTurn();
  }
  
  // check if this turn is deploy turn
  private boolean isSTurn(){
    return luzhanqiState.getTurn() == Turn.S;
  }
  
  // check if certain piece is my piece
  private boolean isMyPiece(Piece piece){
    return (myTurn.get()==Turn.W)?(piece.getKey()>=0 && piece.getKey()<25)
        : (piece.getKey()>=25 && piece.getKey()<50);
  }
  
  // check if a slot is valid deploy slot
  private boolean validDeployPosition(Piece piece, Slot slot){
    if (myTurn.get() == Turn.B){
      //deploy their own piece
      check(piece.getKey()>24 && piece.getKey()<50);
      //delpoy on their own half of board
      check(slot.getKey()>29 && slot.getKey()<60);
      //campsite must be empty
      check(slot.getKey()!=36 && slot.getKey()!=38 && slot.getKey()!=42
          && slot.getKey()!=46 && slot.getKey()!=48);
      //flag rule
      if(piece.getKey()==49){
        check(slot.getKey()==56 || slot.getKey()==58);
      }
      //landmine rule
      if(piece.getKey()>=46 && piece.getKey()<=48){
        check(slot.getKey()>=50 && slot.getKey()<=59);
      }
      //bomb rule
      if(piece.getKey()==45 || piece.getKey()==46){
        check(slot.getKey()>=35);
      }
      
    }else if (myTurn.get() == Turn.W){
      //deploy their own piece
      check(piece.getKey()>=0 && piece.getKey()<25);
      //delpoy on their own half of board
      check(slot.getKey()>=0 && slot.getKey()<30);
      //campsite must be empty
      check(slot.getKey()!=11 && slot.getKey()!=13 && slot.getKey()!=17
          && slot.getKey()!=21 && slot.getKey()!=23);
      //flag rule
      if(piece.getKey()==24){
        check(slot.getKey()==1 || slot.getKey()==3);
      }
      //landmine rule
      if(piece.getKey()>=21 && piece.getKey()<=23){
        check(slot.getKey()>=0 && slot.getKey()<=9);
      }
      //bomb rule
      if(piece.getKey()==19 || piece.getKey()==20){
        check(slot.getKey()<=24);
      }
    }   
    return true;
  }

  private void check(boolean val) {
    if (!val) {
      throw new IllegalArgumentException();
    }
  }

  /**
   * Adds/remove the piece:slot pair from the {@link #deployMap}.
   * The view can only call this method if the presenter called 
   * {@link View#deployNextPiece(Map)}.
   */  
  public void pieceDeploy(Piece piece, Slot slot){
    check(isSTurn());
    check(validDeployPosition(piece,slot));
    if(deployMap.containsKey(piece)){
      //double click: put back to deploy grid
      if(deployMap.get(piece).equals(slot)){
        slot.setEmpty();
        lastDeploy.clear();
        lastDeploy.put(piece, Optional.<Slot>fromNullable(null));
        deployMap.remove(piece);
      }else{
        if(!deployMap.containsValue(slot)){
          deployMap.get(piece).setEmpty();
          slot.setPiece(piece);
          lastDeploy.clear();
          lastDeploy.put(piece, Optional.<Slot>of(slot));
          deployMap.put(piece, slot);
        }
      }
    }else{ //new to game grid
      if(!deployMap.containsValue(slot)){
        slot.setPiece(piece);
        lastDeploy.clear();
        lastDeploy.put(piece, Optional.<Slot>of(slot));
        deployMap.put(piece, slot);
      }
    }
    deployNextPiece();
  }

  /**
   * Finishes the deploy process and wait for opponent to finish.
   * The view can only call this method if the presenter called 
   * {@link View#deployNextPiece(Map)} 
   * and 25 pieces were all deployed by calling {@link #pieceDeploy(Piece, Slot)}.
   */
  public void finishedDeployingPieces() {
    check(deployMap.size()==25);
    container.sendMakeMove(luzhanqiLogic.deployPiecesMove(
        luzhanqiState, getDeployList(deployMap), playerIds, yourPlayerId));
  }
  
  /**
   * After deploy phase, player B do the first move.
   * This method can be called if the presenter passed
   * LuzhanqiMessage.FIRST_MOVE in {@link View#setPlayerState}.
   */
  public void firstMove() {
    apiBoard = getApiBoard(luzhanqiState.getBoard());
    container.sendMakeMove(luzhanqiLogic.firstMove(luzhanqiState));
  }
  
  /**
   * Selects fromSlot and toSlot in a normal move.
   * The view can only call this method if the presenter called {@link View#nextFromTo(List)}.
   */
  public void moveSelected(Slot from, Slot to) {
    check(isMyTurn() && !from.emptySlot() && isMyPiece(from.getPiece()) 
        && (to.emptySlot() || !isMyPiece(to.getPiece())));
    if (fromTo.isEmpty()){
      fromTo.add(from);
      fromTo.add(to);
    }else{
      fromTo.remove(0);
      fromTo.remove(0);
      fromTo.add(from);
      fromTo.add(to);
    }
    nextFromTo();
  }
  
  /**
   * Finished a normal move process.
   * The view can only call this method if the presenter called 
   * {@link View#nextFromTo(List)} 
   * and there is one valid from-to pair existing by calling {@link #moveSelected(Slot, Slot)}
   */
  public void finishedNormalMove() {
    check(isMyTurn() && fromTo.size()==2);        
    apiBoard = getApiBoard(luzhanqiState.getBoard());
    container.sendMakeMove(luzhanqiLogic.normalMove(
        luzhanqiState, apiBoard, 
        ImmutableList.<Integer>of(fromTo.get(0).getKey(),fromTo.get(1).getKey()), playerIds));
  }

  private void deployNextPiece(){
    //view.deployNextPiece(ImmutableMap.copyOf(deployMap));
    view.deployNextPiece(ImmutableMap.copyOf(lastDeploy));
  }

  private void nextFromTo(){
    view.nextFromTo(ImmutableList.copyOf(fromTo));
  }
  
  // Helper: get an apiBoard from a deployMap
  public static List<Integer> getDeployList(Map<Piece,Slot> deployMap){
    List<Integer> apiBoard = new ArrayList<Integer>();
    for(int i = 0; i < 30; i++){
      apiBoard.add(-1);
    }
    for(Map.Entry<Piece, Slot> entry: deployMap.entrySet()){
      Piece piece = entry.getKey();
      Slot slot = entry.getValue();
      apiBoard.set(slot.getKey()%30, piece.getKey());
    }
    return apiBoard;
  }
  
  // Helper: get an apiBoard from a luzhanqi state board
  private List<Integer> getApiBoard(List<Slot> board){
    List<Integer> apiBoard = new ArrayList<Integer>();
    for(Slot s: board){
      if(s.getPiece() != null){
        apiBoard.add(s.getPiece().getKey());
      }else{
        apiBoard.add(-1);
      }
    }
    return apiBoard;
  }

  private void sendInitialMove(List<Integer> playerIds) {
    container.sendMakeMove(luzhanqiLogic.getInitialMove(playerIds));
  }
}