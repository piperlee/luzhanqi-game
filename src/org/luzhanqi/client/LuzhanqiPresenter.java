package org.luzhanqi.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.game_api.GameApi;
import org.game_api.GameApi.Container;
import org.game_api.GameApi.Operation;
import org.game_api.GameApi.SetTurn;
import org.game_api.GameApi.UpdateUI;
import org.luzhanqi.graphics.AIContainer;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gwt.user.client.Timer;

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
    
    // Play the sound effect associated with moving a piece to an empty
    // square
    void playPieceDownSound();

    // Play the sound effect associated with moving a piece to an occupied
    // square
    void playPieceCapturedSound();
  }

  private final LuzhanqiLogic luzhanqiLogic = new LuzhanqiLogic();
  private final View view;
  private final Container container;
  private final String sId = "1";
  /** A viewer doesn't have a color. */
  private Optional<Turn> myTurn;
  private LuzhanqiState luzhanqiState;
  private List<Integer> apiBoard;
  private List<Slot> fromTo;
  public  HashMap<Piece,Slot> deployMap; 
  private HashMap<Piece,Optional<Slot>> lastDeploy;
  private List<String> playerIds;
  private String yourPlayerId;
  private boolean isEndGame = false;
  private AlphaBetaPruning alphaBetaPruning = new AlphaBetaPruning();
    
  public LuzhanqiPresenter(View view, Container container) {
    this.view = view;
    this.container = container;
    view.setPresenter(this);
  }
  
  public void updateUIAI() {
    AIContainer aiContainer = (AIContainer) this.container;
    aiContainer.updateUi(GameApi.AI_PLAYER_ID);
  }
  
  public void updateUIPlayer() {
    AIContainer aiContainer = (AIContainer) this.container;
    for (String pId : playerIds)  {
      if (!pId.equals(GameApi.AI_PLAYER_ID)) {
        aiContainer.updateUi(pId);
      }
    }    
  }
  
  
  /** Get current presenter Turn, i.e who is UI active */
  public Turn getTurn(){
    return myTurn.get();
  }
  
  /** Get current game Turn, i.e who should move */
  public Turn getGameTurn(){
    return luzhanqiState.getTurn();
  }
  
  /** Get current game state */
  public LuzhanqiState getState(){
    return luzhanqiState;
  }
  
  /** Updates the presenter and the view with the state in updateUI. */
  public void updateUI(UpdateUI updateUI) {
    playerIds = updateUI.getPlayerIds();
    yourPlayerId = updateUI.getYourPlayerId();
    int yourPlayerIndex = updateUI.getPlayerIndex(yourPlayerId);
    // turn s has a dummy player id
    myTurn = yourPlayerIndex == 0 ? Optional.of(Turn.B)
        : yourPlayerIndex == 1 ? Optional.of(Turn.W) 
        : yourPlayerIndex == 2 ? Optional.of(Turn.S) : Optional.<Turn>absent();
        
    if (updateUI.getState().isEmpty()) {
      // The B player sends the initial setup move.
      if (myTurn.isPresent() && myTurn.get().isBlack()) {
//        if (myTurn.isPresent() ) {
        sendInitialMove(playerIds);
      }
      return;
    }
    Turn turnOfColor = null;
    for (Operation operation : updateUI.getLastMove()) {
      if (operation instanceof SetTurn) {
        String pId = ((SetTurn) operation).getPlayerId();
        turnOfColor = pId.equals(sId) ? Turn.S : Turn.values()[playerIds.indexOf(pId)];
      }
    }
    deployMap = new HashMap<Piece,Slot>();
    fromTo = new ArrayList<Slot>();
    lastDeploy = new HashMap<Piece,Optional<Slot>>();
    luzhanqiState = 
        luzhanqiLogic.gameApiStateToLuzhanqiState(updateUI.getState(), turnOfColor, playerIds);
    //apiBoard = getApiBoard(luzhanqiState.getBoard());
    isEndGame = endGame();
    
    if (updateUI.isViewer()) {
      view.setViewerState(luzhanqiState.getWhite().size(), luzhanqiState.getBlack().size(),
         luzhanqiState.getDiscard().size() ,luzhanqiState.getBoard(), getLuzhanqiMessage());
      return;
    }
    
    Turn thisT = luzhanqiState.getTurn();
    // Must be a player!
    Turn myT = myTurn.get();
    
    if (updateUI.isAiPlayer()) {
      // TODO: implement AI in a later HW!
      //container.sendMakeMove(..);
      if (thisT == Turn.S && myT == Turn.W) {
        if (isAIGame() && !luzhanqiState.getDW().isPresent()) {
          aiDeploy();
          finishedDeployingPieces();
        }                    
      } else if (getLuzhanqiMessage()==LuzhanqiMessage.NORMAL_MOVE && thisT == Turn.W){
        fromTo = alphaBetaPruning.findBestMove(luzhanqiState, 4, new MyTimer(2000));
        //fromTo = alphaBetaPruning.findBestMove(luzhanqiState, 1);
        //System.out.println("Actual:"+fromTo.get(0).getKey()+" "+fromTo.get(1).getKey());
        nextFromTo();
//        Timer timer = new Timer() {
//          public void run() {  }
//        };
//        timer.schedule(1000);
        waitFun();
        finishedNormalMove();
      }        
      return;
    }
    
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
        if(getLuzhanqiMessage()==LuzhanqiMessage.FIRST_MOVE){
          if (myTurn.isPresent() && myTurn.get().isBlack()) {
            firstMove();            
          }          
        } else if (getLuzhanqiMessage()==LuzhanqiMessage.NORMAL_MOVE){
          // Choose the next card only if the game is not over            
          if (!isEndGame)
            nextFromTo();
        }
      }
    }   
  }
    
  public final native void waitFun() /*-{
    $wnd.setTimeout(function() { }, 1000);
  }-*/; 
    
  public boolean getIsEndGame(){
    return isEndGame;
  }

  // to check if the game state is an end game state
  private boolean endGame(){
    if(luzhanqiState.getDiscard().contains(24) || luzhanqiState.getDiscard().contains(49))
      return true;
    boolean blackNoMove = true;
    for(Integer i: luzhanqiState.getBlack()){
      if(i < 46){ 
        blackNoMove=false;
        break;
      }
    }
    boolean whiteNoMove = true;
    for(Integer i: luzhanqiState.getWhite()){
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
  public boolean isMyTurn() {
    return myTurn.isPresent() && myTurn.get() == luzhanqiState.getTurn();
  }
  
  // check if this turn is deploy turn
  public boolean isSTurn(){
    return luzhanqiState.getTurn() == Turn.S;
  }
  
  // Check if this is an AI game
  public boolean isAIGame() {
    return playerIds.contains(GameApi.AI_PLAYER_ID);
  }
  
  // check if a moving destination is valid
  public boolean toValid(Slot from, Slot to){
    return luzhanqiLogic.toIsValid(luzhanqiState, from, to);
  }
  
  // check if a moving origin is valid
  public boolean fromValid(Slot from){
    return luzhanqiLogic.fromIsValid(luzhanqiState, from);
  }
  
  public void aiDeploy() {
    deployMap.clear();
    List<Integer> list = ImmutableList.of(
        0,24,21,1,2,
        3,4,22,5,23,
        6,-1,8,-1,10,
        11,12,-1,14,20,
        7,-1,13,-1,19,
        16,17,18,9,15,
        25,26,27,28,29,
        30,-1,31,-1,32,
        33,34,-1,35,36,
        37,-1,38,-1,39,
        40,47,46,44,43,
        45,49,48,42,41);
    for(int i = 0; i<6;i++){
      for(int j = 0; j<5; j++){
        Slot slot = new Slot(i*5+j,list.get(i*5+j));
        Piece piece = slot.getPiece(); 
        if(piece!=null){
          deployMap.put(piece, slot);
        }
      }
    }
  }
  
  public void blackDeploy() {
    deployMap.clear();
    List<Integer> list = ImmutableList.of(
        0,24,21,1,2,
        3,4,22,5,23,
        6,-1,8,-1,10,
        11,12,-1,14,20,
        7,-1,13,-1,19,
        16,17,18,9,15,
        25,26,27,28,29,
        30,-1,31,-1,32,
        33,34,-1,35,36,
        37,-1,38,-1,39,
        40,47,46,44,43,
        45,49,48,42,41);    
    for(int i = 6; i<12; i++){
      for(int j = 0; j<5; j++){
        Slot slot = new Slot(i*5+j,list.get(i*5+j));
        Piece piece = slot.getPiece();
        if(piece!=null){
          deployMap.put(piece, slot);
        }
      }
    }
  }
  
  public boolean deployValid (int pieceKey, int slotKey) {
    Piece piece = new Piece (pieceKey,-1);
    Slot slot = new Slot (slotKey,-1);
    try {
      validDeployPosition(piece,slot);
    } catch (IllegalArgumentException e) {
      return false;
    }
    return true;
  }
  // check if a slot is valid deploy slot
  private boolean validDeployPosition(Piece piece, Slot slot) throws IllegalArgumentException{
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

  private void check(boolean val) throws IllegalArgumentException{
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
    try {
      check(isSTurn());
      check(validDeployPosition(piece,slot));
    } catch (Exception e) {
      if (piece != null) {
        return;
      }
    }
    if(deployMap.containsKey(piece)){
      //double click: put back to deploy grid
      if(deployMap.get(piece).equals(slot)){ 
        //change piece slot cross ref
        slot.setEmpty();
        piece.setSlot(-1);
        lastDeploy.clear();
        lastDeploy.put(piece, Optional.<Slot>fromNullable(null));
        deployMap.remove(piece);                
      }else{
        //change a piece on game board to another slot
        if(!deployMap.containsValue(slot)){
          //change piece slot cross ref
          deployMap.get(piece).setEmpty();
          slot.setPiece(piece);
          piece.setSlot(slot.getKey());
          lastDeploy.clear();
          lastDeploy.put(piece, Optional.<Slot>of(slot));
          deployMap.put(piece, slot);
        }
      }
    }else{ 
      //new to game grid
      if(!deployMap.containsValue(slot)){
        //change piece slot cross ref
        slot.setPiece(piece);
        piece.setSlot(slot.getKey());
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
  
//  public void finishedAIDeploy() {
//    check(deployMap.size()==25);
//    container.sendMakeMove(luzhanqiLogic.deployPiecesMove(
//        luzhanqiState, getDeployList(deployMap), playerIds, GameApi.AI_PLAYER_ID));
//  }
  
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
    if(!isMyTurn()) return;
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
    //check(fromTo.size()==2);  
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

  private void sendInitialMove(List<String> playerIds) {
    container.sendMakeMove(luzhanqiLogic.getInitialMove(playerIds));
//    aiDeploy();
//    container.sendMakeMove(luzhanqiLogic.deployPiecesMove(
//        luzhanqiState, getDeployList(deployMap), playerIds, playerIds.get(1)));
//    blackDeploy();
//    container.sendMakeMove(luzhanqiLogic.deployPiecesMove(
//        luzhanqiState, getDeployList(deployMap), playerIds, playerIds.get(0)));
    
    //TODO AI
//    updateUIAI();
//    updateUIPlayer();
  }
}