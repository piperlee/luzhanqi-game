package org.luzhanqi.client;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.List;
import java.util.Map;

import org.luzhanqi.client.LuzhanqiPresenter.LuzhanqiMessage;
import org.luzhanqi.client.LuzhanqiPresenter.View;
import org.luzhanqi.client.GameApi.Container;
import org.luzhanqi.client.GameApi.Operation;
import org.luzhanqi.client.GameApi.SetTurn;
import org.luzhanqi.client.GameApi.UpdateUI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/** Tests for {@link LuzhanqiPresenter}.
 * Test plan:
 * There are several interesting states:
 * 1) empty state
 * 2) initial state
 * 3) after deploy state
 * 4) normal move state
 * 5) game end state
 * There are several interesting yourPlayerId:
 * 1) white player
 * 2) black player
 * 3) viewer
 * 4) dummy turn S id
 * For each one of these states and for each yourPlayerId,
 * I will test what methods the presenters calls on the view and container.
 * In addition I will also test the interactions between the presenter and view, i.e.,
 * the view can call one of these methods:
 * 1) pieceDeploy(Piece, Slot)
 * 2) finishedDeployingPieces()
 * 3) firstMove()
 * 4) moveSelected(Slot, Slot)
 * 5) finishedNormalMove()
 */
@RunWith(JUnit4.class)
public class LuzhanqiPresenterTest {
  /** The class under test. */
  private LuzhanqiPresenter luzhanqiPresenter;
  private final LuzhanqiLogic luzhanqiLogic = new LuzhanqiLogic();
  private View mockView;
  private Container mockContainer;

  private static final String PLAYER_ID = "playerId";
  private static final String W = "W"; // White hand
  private static final String B = "B"; // Black hand
  private static final String D = "D"; // Discard pile
  private static final String BOARD = "board"; // game board
  private static final String MOVE = "move"; // move: from to
  private static final String DEPLOY = "deploy";
  private static final String DW = "DW";
  private static final String DB= "DB";
  private final int viewerId = GameApi.VIEWER_ID;
  private final int sId = 1; //dummy
  private final int wId = 41;
  private final int bId = 42;
  private final ImmutableList<Integer> playerIds = ImmutableList.of(wId, bId, sId);
  private final ImmutableMap<String, Object> wInfo =
      ImmutableMap.<String, Object>of(PLAYER_ID, wId);
  private final ImmutableMap<String, Object> bInfo =
      ImmutableMap.<String, Object>of(PLAYER_ID, bId);
  private final ImmutableMap<String, Object> sInfo =
      ImmutableMap.<String, Object>of(PLAYER_ID, sId);
  private final ImmutableList<Map<String, Object>> playersInfo =
      ImmutableList.<Map<String, Object>>of(wInfo, bInfo, sInfo);

  private final List<Piece> allPieces = genPiecesList();
  private final List<Slot> allSlots = genSlotsList();
  private final List<Integer> boardDeploy = ImmutableList.of(
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
  
  /* The interesting states that I'll test. */
  private final ImmutableMap<String, Object> emptyState = ImmutableMap.<String, Object>of();
  private final Map<String, Object> initialState = ImmutableMap.<String, Object>of(
      BOARD,ImmutableList.of(
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
          -1,-1,-1,-1,-1),
      W, ImmutableList.of(
          0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24),
      B, ImmutableList.of(
          25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49),     
      D, ImmutableList.of());
  
  private final Map<String, Object> endGameState = ImmutableMap.<String, Object>of(
      BOARD,ImmutableList.of(
          -1,41,21,-1,-1,
          -1,-1,22,-1,-1,
          -1,-1,8,-1,-1,
          -1,-1,-1,-1,-1,
          -1,-1,-1,-1,-1,
          -1,-1,-1,-1,-1,
          -1,-1,-1,-1,-1,
          -1,-1,-1,-1,-1,
          -1,-1,-1,-1,-1,
          33,-1,-1,-1,-1,
          -1,47,-1,-1,42,
          -1,49,48,-1,-1),
      MOVE, ImmutableList.of(6,1),
      W, ImmutableList.of(8,21,22),
      B, ImmutableList.of(33,41,42,47,48,49),     
      D, ImmutableList.of(
          0,1,2,3,4,5,6,7,9,10,11,12,13,14,15,16,17,18,19,20,23,
          25,26,27,28,29,30,31,32,34,35,36,37,38,39,40,43,44,45,46,24));
  
  private final Map<String, Object> afterDeployState = 
      new ImmutableMap.Builder<String, Object>()
        .put(BOARD,ImmutableList.of(
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
            -1,-1,-1,-1,-1))
        .put(DW, ImmutableList.of(
            0,24,21,1,2,
            3,4,22,5,23,
            6,-1,8,-1,10,
            11,12,-1,14,20,
            7,-1,13,-1,19,
            16,17,18,9,15))
        .put(DB, ImmutableList.of(
            25,26,27,28,29,
            30,-1,31,-1,32,
            33,34,-1,35,36,
            37,-1,38,-1,39,
            40,47,46,44,43,
            45,49,48,42,41))
        .put(DEPLOY,DEPLOY)
        .put(W, ImmutableList.of(
            0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24))    
        .put(B, ImmutableList.of(
          25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49))
        .put(D, ImmutableList.of())
        .build();

  private final Map<String, Object> normalMoveState = ImmutableMap.<String, Object>of(
      BOARD,ImmutableList.of(
          -1,24,21,-1,-1,
          -1,-1,22,-1,-1,
          -1,-1,8,41,-1,
          -1,-1,-1,-1,-1,
          -1,-1,-1,-1,-1,
          -1,-1,-1,-1,-1,
          -1,-1,-1,-1,-1,
          -1,-1,-1,-1,-1,
          -1,-1,-1,-1,-1,
          33,-1,-1,-1,-1,
          -1,47,-1,-1,42,
          -1,49,48,-1,-1),
      MOVE, ImmutableList.of(32,54),
      W, ImmutableList.of(8,21,22,24),
      B, ImmutableList.of(33,41,42,47,48,49),     
      D, ImmutableList.of(
          0,1,2,3,4,5,6,7,9,10,11,12,13,14,15,16,17,18,19,20,23,
          25,26,27,28,29,30,31,32,34,35,36,37,38,39,40,43,44,45,46)); 

  @Before
  public void runBefore() {
    mockView = Mockito.mock(View.class);
    mockContainer = Mockito.mock(Container.class);
    luzhanqiPresenter = new LuzhanqiPresenter(mockView, mockContainer);
    verify(mockView).setPresenter(luzhanqiPresenter);
  }

  @After
  public void runAfter() {
    // This will ensure I didn't forget to declare any extra interaction the mocks have.
    verifyNoMoreInteractions(mockContainer);
    verifyNoMoreInteractions(mockView);
  }

  // Initial tests
  @Test
  public void testEmptyStateForB() {
    luzhanqiPresenter.updateUI(createUpdateUI(bId, 0, emptyState));
    verify(mockContainer).sendMakeMove(luzhanqiLogic.getInitialMove(playerIds));
  }

  @Test
  public void testEmptyStateForW() {
    luzhanqiPresenter.updateUI(createUpdateUI(wId, 0, emptyState));
  }

  @Test
  public void testEmptyStateForViewer() {
    luzhanqiPresenter.updateUI(createUpdateUI(viewerId, 0, emptyState));
  }

  //Deploy tests
  @Test
  public void testDeployForWTurnOfS() {
    luzhanqiPresenter.updateUI(createUpdateUI(wId, sId, initialState));
    verify(mockView).setPlayerState(25, 0, 
        luzhanqiLogic.gameApiStateToLuzhanqiState(initialState, Turn.S, playerIds).getBoard(), 
        LuzhanqiMessage.IS_DEPLOY);
    verify(mockView).deployNextPiece(ImmutableMap.<Piece,Optional<Slot>>of());
  }
  
  @Test
  public void testDeployForBTurnOfS() {
    luzhanqiPresenter.updateUI(createUpdateUI(bId, sId, initialState));
    verify(mockView).setPlayerState(25, 0, 
        luzhanqiLogic.gameApiStateToLuzhanqiState(initialState, Turn.S, playerIds).getBoard(), 
        LuzhanqiMessage.IS_DEPLOY);
    verify(mockView).deployNextPiece(ImmutableMap.<Piece,Optional<Slot>>of());
  }

  @Test
  public void testDeployForViewerTurnOfS() {
    luzhanqiPresenter.updateUI(createUpdateUI(viewerId, sId, initialState));
    verify(mockView).setViewerState(25, 25, 0, 
        luzhanqiLogic.gameApiStateToLuzhanqiState(initialState, Turn.S, playerIds).getBoard(), 
        LuzhanqiMessage.IS_DEPLOY);
  }
  
  @Test
  public void testDeployPieceForWTurnOfS() {
    UpdateUI updateUI = createUpdateUI(wId, sId, initialState);
    luzhanqiPresenter.updateUI(updateUI);
    luzhanqiPresenter.pieceDeploy(allPieces.get(24), allSlots.get(1));
    
    verify(mockView).setPlayerState(25, 0, 
        luzhanqiLogic.gameApiStateToLuzhanqiState(initialState, Turn.S, playerIds).getBoard(), 
        LuzhanqiMessage.IS_DEPLOY);
    verify(mockView).deployNextPiece(ImmutableMap.<Piece,Optional<Slot>>of());
    Map<Piece,Optional<Slot>> lastDeploy = Maps.newHashMap();
    lastDeploy.put(allPieces.get(24),  Optional.<Slot>of(allSlots.get(1)));
    verify(mockView).deployNextPiece(lastDeploy);
    
  }
  
  @Test
  public void testDeployPiecesForWTurnOfS() {
    UpdateUI updateUI = createUpdateUI(wId, sId, initialState);
    luzhanqiPresenter.updateUI(updateUI);
    luzhanqiPresenter.pieceDeploy(allPieces.get(24), allSlots.get(1));
    luzhanqiPresenter.pieceDeploy(allPieces.get(24), allSlots.get(3));
    
    verify(mockView).setPlayerState(25, 0, 
        luzhanqiLogic.gameApiStateToLuzhanqiState(initialState, Turn.S, playerIds).getBoard(), 
        LuzhanqiMessage.IS_DEPLOY);
    verify(mockView).deployNextPiece(ImmutableMap.<Piece,Optional<Slot>>of());
    Map<Piece,Optional<Slot>> lastDeploy = Maps.newHashMap();
    lastDeploy.put(allPieces.get(24),  Optional.<Slot>of(allSlots.get(1)));
    verify(mockView).deployNextPiece(lastDeploy);
    lastDeploy.clear();
    lastDeploy.put(allPieces.get(24), Optional.<Slot>of( allSlots.get(3)));
    verify(mockView).deployNextPiece(lastDeploy);
  }
  
  @Test
  public void testDeployPieceForBTurnOfS() {
    UpdateUI updateUI = createUpdateUI(bId, sId, initialState);
    luzhanqiPresenter.updateUI(updateUI);
    luzhanqiPresenter.pieceDeploy(allPieces.get(33), allSlots.get(45));
    
    verify(mockView).setPlayerState(25, 0, 
        luzhanqiLogic.gameApiStateToLuzhanqiState(initialState, Turn.S, playerIds).getBoard(), 
        LuzhanqiMessage.IS_DEPLOY);
    verify(mockView).deployNextPiece(ImmutableMap.<Piece,Optional<Slot>>of());
    Map<Piece,Optional<Slot>> lastDeploy = Maps.newHashMap();
    lastDeploy.put(allPieces.get(33),  Optional.<Slot>of(allSlots.get(45)));
    verify(mockView).deployNextPiece(lastDeploy);
    
  }
  
  @Test
  public void testDeployPiecesForBTurnOfS() {
    UpdateUI updateUI = createUpdateUI(bId, sId, initialState);
    luzhanqiPresenter.updateUI(updateUI);
    luzhanqiPresenter.pieceDeploy(allPieces.get(33), allSlots.get(45));
    luzhanqiPresenter.pieceDeploy(allPieces.get(47), allSlots.get(51));
    
    verify(mockView).setPlayerState(25, 0, 
        luzhanqiLogic.gameApiStateToLuzhanqiState(initialState, Turn.S, playerIds).getBoard(), 
        LuzhanqiMessage.IS_DEPLOY);
    verify(mockView).deployNextPiece(ImmutableMap.<Piece,Optional<Slot>>of());
    Map<Piece,Optional<Slot>> lastDeploy = Maps.newHashMap();
    lastDeploy.put(allPieces.get(33),  Optional.<Slot>of(allSlots.get(45)));
    verify(mockView).deployNextPiece(lastDeploy);
    lastDeploy.clear();
    lastDeploy.put(allPieces.get(47),  Optional.<Slot>of(allSlots.get(51)));
    verify(mockView).deployNextPiece(lastDeploy);
  }
  
  @Test
  public void testDeployPiecesForViewerTurnOfS() {
    luzhanqiPresenter.updateUI(createUpdateUI(viewerId, sId, initialState));
    verify(mockView).setViewerState(25, 25, 0, 
        luzhanqiLogic.gameApiStateToLuzhanqiState(initialState, Turn.S, playerIds).getBoard(), 
        LuzhanqiMessage.IS_DEPLOY);
  }
  
  @Test
  public void testFinishDeployForWTurnOfS() {
    UpdateUI updateUI = createUpdateUI(wId, sId, initialState);
    LuzhanqiState luzhanqiState =
        luzhanqiLogic.gameApiStateToLuzhanqiState(updateUI.getState(), Turn.W, playerIds);
    luzhanqiPresenter.updateUI(updateUI);
    for(int i = 0; i < 30; i++){
      if(boardDeploy.get(i) != -1){
        luzhanqiPresenter.pieceDeploy(allPieces.get(boardDeploy.get(i)), allSlots.get(i));
      }
    }
    luzhanqiPresenter.finishedDeployingPieces();
    
    verify(mockView).setPlayerState(25, 0, 
        luzhanqiLogic.gameApiStateToLuzhanqiState(initialState, Turn.S, playerIds).getBoard(), 
        LuzhanqiMessage.IS_DEPLOY);
    verify(mockView).deployNextPiece(ImmutableMap.<Piece,Optional<Slot>>of());
    Map<Piece,Slot> deployMap = Maps.newHashMap();
    for(int i = 0; i < 30; i++){
      if(boardDeploy.get(i) != -1){
        deployMap.put(allPieces.get(boardDeploy.get(i)),  allSlots.get(i));
        Map<Piece,Optional<Slot>> lastDeploy = Maps.newHashMap();
        lastDeploy.put(allPieces.get(boardDeploy.get(i)),  Optional.<Slot>of(allSlots.get(i)));
        verify(mockView).deployNextPiece(lastDeploy);
      }
    }
    verify(mockContainer).sendMakeMove(luzhanqiLogic.deployPiecesMove(
        luzhanqiState, LuzhanqiPresenter.getDeployList(deployMap), 
        playerIds, luzhanqiState.getPlayerId(Turn.W)));    
  }
  
  @Test
  public void testFinishDeployForBTurnOfS() {
    UpdateUI updateUI = createUpdateUI(bId, sId, initialState);
    LuzhanqiState luzhanqiState =
        luzhanqiLogic.gameApiStateToLuzhanqiState(updateUI.getState(), Turn.B, playerIds);
    luzhanqiPresenter.updateUI(updateUI);
    for(int i = 30; i < 60; i++){
      if(boardDeploy.get(i) != -1){
        luzhanqiPresenter.pieceDeploy(allPieces.get(boardDeploy.get(i)), allSlots.get(i));
      }
    }
    luzhanqiPresenter.finishedDeployingPieces();
    
    verify(mockView).setPlayerState(25, 0, 
        luzhanqiLogic.gameApiStateToLuzhanqiState(initialState, Turn.S, playerIds).getBoard(), 
        LuzhanqiMessage.IS_DEPLOY);
    verify(mockView).deployNextPiece(ImmutableMap.<Piece,Optional<Slot>>of());
    Map<Piece,Slot> deployMap = Maps.newHashMap();
    for(int i = 30; i < 60; i++){
      if(boardDeploy.get(i) != -1){
        deployMap.put(allPieces.get(boardDeploy.get(i)),  allSlots.get(i));
        Map<Piece,Optional<Slot>> lastDeploy = Maps.newHashMap();
        lastDeploy.put(allPieces.get(boardDeploy.get(i)),  Optional.<Slot>of(allSlots.get(i)));
        verify(mockView).deployNextPiece(lastDeploy);
      }
    }
    verify(mockContainer).sendMakeMove(luzhanqiLogic.deployPiecesMove(
        luzhanqiState, LuzhanqiPresenter.getDeployList(deployMap), 
        playerIds, luzhanqiState.getPlayerId(Turn.B)));    
  }
  
  @Test
  public void testFinishDeployForViewerTurnOfS() {
    luzhanqiPresenter.updateUI(createUpdateUI(viewerId, sId, initialState));
    verify(mockView).setViewerState(25, 25, 0, 
        luzhanqiLogic.gameApiStateToLuzhanqiState(initialState, Turn.S, playerIds).getBoard(), 
        LuzhanqiMessage.IS_DEPLOY);
  }
  
  @SuppressWarnings("unchecked")
  @Test
  public void testFirstMoveStateForW() {    
    luzhanqiPresenter.updateUI(createUpdateUI(wId, bId, afterDeployState));
    verify(mockView).setPlayerState(25, 0, 
        LuzhanqiLogic.getBoardFromApiBoard((List<Integer>)afterDeployState.get(BOARD)), 
        LuzhanqiMessage.FIRST_MOVE);
  }

  //First Move Tests
  @Test
  public void testFirstMoveStateForB() {    
    UpdateUI updateUI = createUpdateUI(bId, bId, afterDeployState);
    luzhanqiPresenter.updateUI(updateUI);
    LuzhanqiState state = luzhanqiLogic.gameApiStateToLuzhanqiState(
        updateUI.getState(), Turn.B, playerIds);
    verify(mockView).setPlayerState(25, 0, 
        state.getBoard(), LuzhanqiMessage.FIRST_MOVE);
    verify(mockContainer).sendMakeMove(luzhanqiLogic.firstMove(state));
  }
  
  @SuppressWarnings("unchecked")
  @Test
  public void testFirstMoveStateForViewer() {    
    luzhanqiPresenter.updateUI(createUpdateUI(viewerId, bId, afterDeployState));
    verify(mockView).setViewerState(25, 25, 0, 
        LuzhanqiLogic.getBoardFromApiBoard((List<Integer>)afterDeployState.get(BOARD)), 
        LuzhanqiMessage.FIRST_MOVE);
  }
  
  //Normal move tests
  @SuppressWarnings("unchecked")
  @Test
  public void testNormalMoveStateTurnOfBForViewer() {    
    luzhanqiPresenter.updateUI(createUpdateUI(viewerId, bId, normalMoveState));
    verify(mockView).setViewerState(4, 6, 40, 
        LuzhanqiLogic.getBoardFromApiBoard((List<Integer>)normalMoveState.get(BOARD)), 
        LuzhanqiMessage.NORMAL_MOVE);
  }
  
  @Test
  public void testNormalMoveStateTurnOfBForB() {
    UpdateUI updateUI = createUpdateUI(bId, bId, normalMoveState);
    luzhanqiPresenter.updateUI(updateUI);
    LuzhanqiState luzhanqiState = luzhanqiLogic.gameApiStateToLuzhanqiState(
        updateUI.getState(), Turn.B, playerIds);
    luzhanqiPresenter.moveSelected(
        luzhanqiState.getBoard().get(54), luzhanqiState.getBoard().get(32));
    
    verify(mockView).setPlayerState(4, 40, 
        luzhanqiLogic.gameApiStateToLuzhanqiState(normalMoveState, Turn.B, playerIds).getBoard(), 
        LuzhanqiMessage.NORMAL_MOVE);
    verify(mockView).nextFromTo(ImmutableList.<Slot>of());
    verify(mockView).nextFromTo(ImmutableList.<Slot>of(
        luzhanqiState.getBoard().get(54), luzhanqiState.getBoard().get(32)));
    
  }
  
  @Test
  public void testNormalMoveStateTurnOfBForW() {
    UpdateUI updateUI = createUpdateUI(wId, bId, normalMoveState);
    luzhanqiPresenter.updateUI(updateUI);
    verify(mockView).setPlayerState(6, 40, 
        luzhanqiLogic.gameApiStateToLuzhanqiState(normalMoveState, Turn.B, playerIds).getBoard(), 
        LuzhanqiMessage.NORMAL_MOVE);
  }
  
  @Test
  public void testNormalMoveTwiceStateTurnOfBForB() {
    UpdateUI updateUI = createUpdateUI(bId, bId, normalMoveState);
    luzhanqiPresenter.updateUI(updateUI);
    LuzhanqiState luzhanqiState = luzhanqiLogic.gameApiStateToLuzhanqiState(
        updateUI.getState(), Turn.B, playerIds);
    luzhanqiPresenter.moveSelected(
        luzhanqiState.getBoard().get(54), luzhanqiState.getBoard().get(32));
    luzhanqiPresenter.moveSelected(
        luzhanqiState.getBoard().get(45), luzhanqiState.getBoard().get(30));
    
    verify(mockView).setPlayerState(4, 40, 
        luzhanqiLogic.gameApiStateToLuzhanqiState(normalMoveState, Turn.B, playerIds).getBoard(), 
        LuzhanqiMessage.NORMAL_MOVE);
    verify(mockView).nextFromTo(ImmutableList.<Slot>of());
    verify(mockView).nextFromTo(ImmutableList.<Slot>of(
        luzhanqiState.getBoard().get(54), luzhanqiState.getBoard().get(32)));
    verify(mockView).nextFromTo(ImmutableList.<Slot>of(
        luzhanqiState.getBoard().get(45), luzhanqiState.getBoard().get(30)));   
  }
  
  @Test
  public void testFinishNormalMoveTurnOfBForB() {
    UpdateUI updateUI = createUpdateUI(bId, bId, normalMoveState);
    LuzhanqiState luzhanqiState =
        luzhanqiLogic.gameApiStateToLuzhanqiState(updateUI.getState(), Turn.B, playerIds);
    luzhanqiPresenter.updateUI(updateUI);
    luzhanqiPresenter.moveSelected(
        luzhanqiState.getBoard().get(54), luzhanqiState.getBoard().get(32));
    luzhanqiPresenter.moveSelected(
        luzhanqiState.getBoard().get(45), luzhanqiState.getBoard().get(30));
    luzhanqiPresenter.finishedNormalMove();
    
    verify(mockView).setPlayerState(4, 40, 
        luzhanqiLogic.gameApiStateToLuzhanqiState(normalMoveState, Turn.B, playerIds).getBoard(), 
        LuzhanqiMessage.NORMAL_MOVE);
    verify(mockView).nextFromTo(ImmutableList.<Slot>of());
    verify(mockView).nextFromTo(ImmutableList.<Slot>of(
        luzhanqiState.getBoard().get(54), luzhanqiState.getBoard().get(32)));
    verify(mockView).nextFromTo(ImmutableList.<Slot>of(
        luzhanqiState.getBoard().get(45), luzhanqiState.getBoard().get(30)));
    verify(mockContainer).sendMakeMove(luzhanqiLogic.normalMove(
        luzhanqiState, luzhanqiState.getApiBoard(), 
        ImmutableList.<Integer>of(45,30), playerIds));  
  }
  
  @Test
  public void testFinishNormalMoveTurnOfBForViewer() {
    luzhanqiPresenter.updateUI(createUpdateUI(viewerId, bId, normalMoveState));
    verify(mockView).setViewerState(4, 6, 40, 
        luzhanqiLogic.gameApiStateToLuzhanqiState(normalMoveState, Turn.B, playerIds).getBoard(), 
        LuzhanqiMessage.NORMAL_MOVE);
  }
  
  @Test
  public void testFinishNormalMoveTurnOfBForW() {
    luzhanqiPresenter.updateUI(createUpdateUI(wId, bId, normalMoveState));
    verify(mockView).setPlayerState(6, 40, 
        luzhanqiLogic.gameApiStateToLuzhanqiState(normalMoveState, Turn.B, playerIds).getBoard(), 
        LuzhanqiMessage.NORMAL_MOVE);
  }
    
  //End game tests
  @SuppressWarnings("unchecked")
  @Test
  public void testGameOverStateForW() {    
    luzhanqiPresenter.updateUI(createUpdateUI(wId, bId, endGameState));
    verify(mockView).setPlayerState(6, 41, 
        LuzhanqiLogic.getBoardFromApiBoard((List<Integer>)endGameState.get(BOARD)), 
        LuzhanqiMessage.NORMAL_MOVE);
  }
  
  @SuppressWarnings("unchecked")
  @Test
  public void testGameOverStateForB() {    
    luzhanqiPresenter.updateUI(createUpdateUI(bId, bId, endGameState));
    verify(mockView).setPlayerState(3, 41, 
        LuzhanqiLogic.getBoardFromApiBoard((List<Integer>)endGameState.get(BOARD)), 
        LuzhanqiMessage.NORMAL_MOVE);
  }
  
  @SuppressWarnings("unchecked")
  @Test
  public void testGameOverStateForViewer() {    
    luzhanqiPresenter.updateUI(createUpdateUI(viewerId, bId, endGameState));
    verify(mockView).setViewerState(3, 6, 41,
        LuzhanqiLogic.getBoardFromApiBoard((List<Integer>)endGameState.get(BOARD)), 
        LuzhanqiMessage.NORMAL_MOVE);
  }

  // Helper: generate Piece List contains all pieces
  private ImmutableList<Piece> genPiecesList(){
    List<Piece> pieces = Lists.newArrayList();
    for(int i = 0; i < 50; i++){
      pieces.add(new Piece(i));
    }
    return ImmutableList.copyOf(pieces);
  }

//Helper: generate Slot List contains all slots
  private ImmutableList<Slot> genSlotsList(){
    List<Slot> slots = Lists.newArrayList();
    for(int i = 0; i < 60; i++){
      slots.add(new Slot(i,-1));
    }
    return ImmutableList.copyOf(slots);
  }

  private UpdateUI createUpdateUI(
      int yourPlayerId, int turnOfPlayerId, Map<String, Object> state) {
    // Our UI only looks at the current state
    // (we ignore: lastState, lastMovePlayerId, playerIdToNumberOfTokensInPot)
    return new UpdateUI(yourPlayerId, playersInfo, state,
        emptyState, // we ignore lastState
        ImmutableList.<Operation>of(new SetTurn(turnOfPlayerId)),
        0,
        ImmutableMap.<Integer, Integer>of());
  }
}