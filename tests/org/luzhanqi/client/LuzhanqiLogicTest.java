package org.luzhanqi.client;

import java.util.List;
import java.util.Map;

import org.luzhanqi.client.GameApi.Operation;
import org.luzhanqi.client.GameApi.Set;
import org.luzhanqi.client.GameApi.SetTurn;
import org.luzhanqi.client.GameApi.Delete;
import org.luzhanqi.client.GameApi.EndGame;
import org.luzhanqi.client.GameApi.SetVisibility;
import org.luzhanqi.client.GameApi.VerifyMove;
import org.luzhanqi.client.GameApi.VerifyMoveDone;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

@RunWith(JUnit4.class)
public class LuzhanqiLogicTest {
  
  LuzhanqiLogic luzhanqiLogic = new LuzhanqiLogic();
  private void assertMoveOk(VerifyMove verifyMove) {
    luzhanqiLogic.checkMoveIsLegal(verifyMove);
  }
  private void assertHacker(VerifyMove verifyMove) {
    VerifyMoveDone verifyDone = luzhanqiLogic.verify(verifyMove);
    assertEquals(verifyMove.getLastMovePlayerId(), verifyDone.getHackerPlayerId());
  }
  
  private final int wId = 41;
  private final int bId = 42;
  private final int sId = 1; // dummy turn s id
  private final String playerId = "playerId";
  private static final String W = "W"; // White hand
  private static final String B = "B"; // Black hand
  private static final String S = "S"; // Start arranging pieces B and W
  private static final String D = "D"; // Discard pile
  private static final String READY = "ready"; // after arrange pieces set ready
  private static final String MOVE = "move"; // move from SLx to SLy
  private static final String DEPLOY = "deploy"; // beat pieces, maybe both
  private static final String BOARD = "board"; 
  private final Map<String, Object> wInfo = ImmutableMap.<String, Object>of(playerId, wId);
  private final Map<String, Object> bInfo = ImmutableMap.<String, Object>of(playerId, bId);
  private final Map<String, Object> sInfo = ImmutableMap.<String, Object>of(playerId, sId);
  private final List<Map<String, Object>> playersInfo = ImmutableList.of(wInfo, bInfo, sInfo);
  private final Map<String, Object> emptyState = ImmutableMap.<String, Object>of();
  private final Map<String, Object> nonEmptyState = ImmutableMap.<String, Object>of("k", "v");
  
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
  
  private final Map<String, Object> afterDeployState = ImmutableMap.<String, Object>of(
      BOARD,ImmutableList.of(
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
          45,49,48,42,41),
      W, ImmutableList.of(
          0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24),
      B, ImmutableList.of(
          25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49),     
      D, ImmutableList.of());
  
  private final Map<String, Object> oldStateB = ImmutableMap.<String, Object>of(
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
      W, ImmutableList.of(8,21,22,24),
      B, ImmutableList.of(33,41,42,47,48,49),     
      D, ImmutableList.of(
          0,1,2,3,4,5,6,7,9,10,11,12,13,14,15,16,17,18,19,20,23,
          25,26,27,28,29,30,31,32,34,35,36,37,38,39,40,43,44,45,46)); 
  
  private VerifyMove vMove(
      int lastMovePlayerId, Map<String, Object> lastState, List<Operation> lastMove) {
    return new VerifyMove(playersInfo,
        emptyState,
        lastState, lastMove, lastMovePlayerId, ImmutableMap.<Integer, Integer>of());
  }
  
  private List<Operation> getInitialOperations() {
    return luzhanqiLogic.getInitialMove(ImmutableList.of(wId, bId));
  }

  @Test
  public void testGetInitialOperationsSize() {
    assertEquals(5 + 25 + 25, luzhanqiLogic.getInitialMove(ImmutableList.of(wId, bId)).size());
  }

  @Test
  public void testInitialMove() {
    assertMoveOk(vMove(bId, emptyState, getInitialOperations()));
  }

  @Test
  public void testInitialMoveByWrongPlayer() {
    assertHacker(vMove(wId,emptyState,getInitialOperations()));
  }

  @Test
  public void testInitialMoveFromNonEmptyState() {
    assertHacker(vMove(bId,nonEmptyState,getInitialOperations()));
  }
  
  @Test
  public void testInitialMoveWithWrongOperation() {
    List<Operation> move = ImmutableList.<Operation>of(
        // Black set turn to S, deploy pieces
        new SetTurn(sId),
        new Set(BOARD,ImmutableList.of(
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
            -1,-1,-1,-1,-1)),
        new Set(W, ImmutableList.of(
                0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24)),
        new Set(B, ImmutableList.of(
                25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49)),     
        new Set(D, ImmutableList.of()),
        new Set(S,READY));
    
    assertHacker(vMove(bId,emptyState,move));
  }
  
  @Test
  public void testDeployB() {
    List<Operation> move = ImmutableList.<Operation>of(
        new SetTurn(bId),
        new Set(DEPLOY,DEPLOY),
        new Set(BOARD,ImmutableList.of(
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            25,26,27,28,29,
            30,-1,31,-1,32,
            33,34,-1,35,36,
            37,-1,38,-1,39,
            40,47,46,44,43,
            45,49,48,42,41)),
        new Set(W, ImmutableList.of(
            0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24)),
        new Set(B, ImmutableList.of(
            25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49)),     
        new Set(D, ImmutableList.of()));
        
    assertMoveOk(vMove(bId,initialState,move));
  }    
  
  @Test
  public void testDeployW() {
    List<Operation> move = ImmutableList.<Operation>of(
        new SetTurn(bId),
        new Set(DEPLOY,DEPLOY),
        new Set(BOARD,ImmutableList.of(
            0,24,21,1,2,
            3,4,22,5,23,
            6,-1,8,-1,10,
            11,12,-1,14,20,
            7,-1,13,-1,19,
            16,17,18,9,15,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1)),
        new Set(W, ImmutableList.of(
            0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24)),
        new Set(B, ImmutableList.of(
            25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49)),     
        new Set(D, ImmutableList.of()));
        
    assertMoveOk(vMove(wId,initialState,move));
  }
  
  @Test
  public void testIllDeployFlagB() {
    List<Operation> move = ImmutableList.<Operation>of(
        new SetTurn(bId),
        new Set(DEPLOY,DEPLOY),
        new Set(BOARD,ImmutableList.of(
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            25,26,27,28,29,
            30,-1,31,-1,32,
            33,34,-1,35,36,
            37,-1,38,-1,39,
            40,47,46,44,43,
            45,48,49,42,41)),
        new Set(W, ImmutableList.of(
            0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24)),
        new Set(B, ImmutableList.of(
            25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49)),     
        new Set(D, ImmutableList.of()));
        
    assertHacker(vMove(bId,initialState,move));
  }
  
  @Test
  public void testIllDeployLandMineB() {
    List<Operation> move = ImmutableList.<Operation>of(
        new SetTurn(bId),
        new Set(DEPLOY,DEPLOY),
        new Set(BOARD,ImmutableList.of(
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            25,47,27,28,29,
            30,-1,31,-1,32,
            33,34,-1,35,36,
            37,-1,38,-1,39,
            40,26,46,44,43,
            45,49,48,42,41)),
        new Set(W, ImmutableList.of(
            0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24)),
        new Set(B, ImmutableList.of(
            25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49)),     
        new Set(D, ImmutableList.of()));
        
    assertHacker(vMove(bId,initialState,move));
  }
  
  @Test
  public void testIllDeployCampB() {
    List<Operation> move = ImmutableList.<Operation>of(
        new SetTurn(bId),
        new Set(DEPLOY,DEPLOY),
        new Set(BOARD,ImmutableList.of(
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            25,26,27,28,29,
            30,-1,31,-1,32,
            33,34,-1,35,36,
            -1,37,38,-1,39,
            40,47,46,44,43,
            45,49,48,42,41)),
        new Set(W, ImmutableList.of(
            0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24)),
        new Set(B, ImmutableList.of(
            25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49)),     
        new Set(D, ImmutableList.of()));
        
    assertHacker(vMove(bId,initialState,move));
  }
  
  @Test
  public void testIllDeployBombB() {
    List<Operation> move = ImmutableList.<Operation>of(
        new SetTurn(bId),
        new Set(DEPLOY,DEPLOY),
        new Set(BOARD,ImmutableList.of(
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            45,26,27,28,29,
            30,-1,31,-1,32,
            33,34,-1,35,36,
            37,-1,38,-1,39,
            40,47,46,44,43,
            25,49,48,42,41)),
        new Set(W, ImmutableList.of(
            0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24)),
        new Set(B, ImmutableList.of(
            25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49)),     
        new Set(D, ImmutableList.of()));
        
    assertHacker(vMove(bId,initialState,move));
  }
  
  @Test
  public void testFirstMoveB() {
    List<Operation> move = ImmutableList.<Operation>of(
        new SetTurn(bId),
        new Delete(DEPLOY),
        new Set(BOARD,ImmutableList.of(
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
            45,49,48,42,41)),
        new Set(W, ImmutableList.of(
            0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24)),
        new Set(B, ImmutableList.of(
            25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49)),     
        new Set(D, ImmutableList.of()),
        new SetVisibility(String.valueOf(0)), new SetVisibility(String.valueOf(1)),
        new SetVisibility(String.valueOf(2)), new SetVisibility(String.valueOf(3)),
        new SetVisibility(String.valueOf(4)), new SetVisibility(String.valueOf(5)),
        new SetVisibility(String.valueOf(6)), new SetVisibility(String.valueOf(7)),
        new SetVisibility(String.valueOf(8)), new SetVisibility(String.valueOf(9)),
        new SetVisibility(String.valueOf(10)), new SetVisibility(String.valueOf(11)),
        new SetVisibility(String.valueOf(12)), new SetVisibility(String.valueOf(13)),
        new SetVisibility(String.valueOf(14)), new SetVisibility(String.valueOf(15)),
        new SetVisibility(String.valueOf(16)), new SetVisibility(String.valueOf(17)),
        new SetVisibility(String.valueOf(18)), new SetVisibility(String.valueOf(19)),
        new SetVisibility(String.valueOf(20)), new SetVisibility(String.valueOf(21)),
        new SetVisibility(String.valueOf(22)), new SetVisibility(String.valueOf(23)),
        new SetVisibility(String.valueOf(24)), new SetVisibility(String.valueOf(25)),
        new SetVisibility(String.valueOf(26)), new SetVisibility(String.valueOf(27)),
        new SetVisibility(String.valueOf(28)), new SetVisibility(String.valueOf(29)),
        new SetVisibility(String.valueOf(30)), new SetVisibility(String.valueOf(31)),
        new SetVisibility(String.valueOf(32)), new SetVisibility(String.valueOf(33)),
        new SetVisibility(String.valueOf(34)), new SetVisibility(String.valueOf(35)),
        new SetVisibility(String.valueOf(36)), new SetVisibility(String.valueOf(37)),
        new SetVisibility(String.valueOf(38)), new SetVisibility(String.valueOf(39)),
        new SetVisibility(String.valueOf(40)), new SetVisibility(String.valueOf(41)),
        new SetVisibility(String.valueOf(42)), new SetVisibility(String.valueOf(43)),
        new SetVisibility(String.valueOf(44)), new SetVisibility(String.valueOf(45)),
        new SetVisibility(String.valueOf(46)), new SetVisibility(String.valueOf(47)),
        new SetVisibility(String.valueOf(48)), new SetVisibility(String.valueOf(49)));
    
    assertMoveOk(vMove(bId,afterDeployState,move));
  }
  
  @Test
  public void testIllSetVNoBB() {
    List<Operation> move = ImmutableList.<Operation>of(
        new SetTurn(bId),
        new Delete(DEPLOY),
        new Set(BOARD,ImmutableList.of(
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
            45,49,48,42,41)),
        new Set(W, ImmutableList.of(
            0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24)),
        new Set(B, ImmutableList.of(
            25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49)),     
        new Set(D, ImmutableList.of()),
        new SetVisibility(String.valueOf(0)), new SetVisibility(String.valueOf(1)),
        new SetVisibility(String.valueOf(2)), new SetVisibility(String.valueOf(3)),
        new SetVisibility(String.valueOf(4)), new SetVisibility(String.valueOf(5)),
        new SetVisibility(String.valueOf(6)), new SetVisibility(String.valueOf(7)),
        new SetVisibility(String.valueOf(8)), new SetVisibility(String.valueOf(9)),
        new SetVisibility(String.valueOf(10)), new SetVisibility(String.valueOf(11)),
        new SetVisibility(String.valueOf(12)), new SetVisibility(String.valueOf(13)),
        new SetVisibility(String.valueOf(14)), new SetVisibility(String.valueOf(15)),
        new SetVisibility(String.valueOf(16)), new SetVisibility(String.valueOf(17)),
        new SetVisibility(String.valueOf(18)), new SetVisibility(String.valueOf(19)),
        new SetVisibility(String.valueOf(20)), new SetVisibility(String.valueOf(21)),
        new SetVisibility(String.valueOf(22)), new SetVisibility(String.valueOf(23)),
        new SetVisibility(String.valueOf(24)));
    
    assertHacker(vMove(bId,initialState,move));
  }

  @Test
  public void testMoveB() {
    List<Operation> move = ImmutableList.<Operation>of(
        new SetTurn(wId),
        new Set(MOVE,ImmutableList.of(45,46)),
        new Set(BOARD,ImmutableList.of(
            -1,24,21,-1,-1,
            -1,-1,22,-1,-1,
            -1,-1,8,41,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,33,-1,-1,-1,
            -1,47,-1,-1,42,
            -1,49,48,-1,-1)),
        new Set(W, ImmutableList.of(8,21,22,24)),
        new Set(B, ImmutableList.of(33,41,42,47,48,49)),     
        new Set(D, ImmutableList.of(
            0,1,2,3,4,5,6,7,9,10,11,12,13,14,15,16,17,18,19,20,23,
            25,26,27,28,29,30,31,32,34,35,36,37,38,39,40,43,44,45,46)));

    assertMoveOk(vMove(bId,oldStateB,move));
  }
  
  @Test
  public void testMoveOnRailB() {
    List<Operation> move = ImmutableList.<Operation>of(
        new SetTurn(wId),
        new Set(MOVE,ImmutableList.of(45,10)),
        new Set(BOARD,ImmutableList.of(
            -1,24,21,-1,-1,
            -1,-1,22,-1,-1,
            33,-1,8,41,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,47,-1,-1,42,
            -1,49,48,-1,-1)),
        new Set(W, ImmutableList.of(8,21,22,24)),
        new Set(B, ImmutableList.of(33,41,42,47,48,49)),     
        new Set(D, ImmutableList.of(
            0,1,2,3,4,5,6,7,9,10,11,12,13,14,15,16,17,18,19,20,23,
            25,26,27,28,29,30,31,32,34,35,36,37,38,39,40,43,44,45,46)));

    assertMoveOk(vMove(bId,oldStateB,move));
  }
  
  @Test
  public void testMoveOnRailEB() {
    List<Operation> move = ImmutableList.<Operation>of(
        new SetTurn(wId),
        new Set(MOVE,ImmutableList.of(54,32)),
        new Set(BOARD,ImmutableList.of(
            -1,24,21,-1,-1,
            -1,-1,22,-1,-1,
            -1,-1,8,41,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,42,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            33,-1,-1,-1,-1,
            -1,47,-1,-1,-1,
            -1,49,48,-1,-1)),
        new Set(W, ImmutableList.of(8,21,22,24)),
        new Set(B, ImmutableList.of(33,41,42,47,48,49)),     
        new Set(D, ImmutableList.of(
            0,1,2,3,4,5,6,7,9,10,11,12,13,14,15,16,17,18,19,20,23,
            25,26,27,28,29,30,31,32,34,35,36,37,38,39,40,43,44,45,46)));
    List<Operation> move2 = ImmutableList.<Operation>of(
        new SetTurn(wId),
        new Set(MOVE,ImmutableList.of(54,40)),
        new Set(BOARD,ImmutableList.of(
            -1,24,21,-1,-1,
            -1,-1,22,-1,-1,
            -1,-1,8,41,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            42,-1,-1,-1,-1,
            33,-1,-1,-1,-1,
            -1,47,-1,-1,-1,
            -1,49,48,-1,-1)),
        new Set(W, ImmutableList.of(8,21,22,24)),
        new Set(B, ImmutableList.of(33,41,42,47,48,49)),     
        new Set(D, ImmutableList.of(
            0,1,2,3,4,5,6,7,9,10,11,12,13,14,15,16,17,18,19,20,23,
            25,26,27,28,29,30,31,32,34,35,36,37,38,39,40,43,44,45,46)));
    List<Operation> moveAndBeat = ImmutableList.<Operation>of(
        new SetTurn(wId),
        new Set(MOVE,ImmutableList.of(54,7)),
        new Set(BOARD,ImmutableList.of(
            -1,24,21,-1,-1,
            -1,-1,42,-1,-1,
            -1,-1,8,41,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            33,-1,-1,-1,-1,
            -1,47,-1,-1,-1,
            -1,49,48,-1,-1)),
        new Set(W, ImmutableList.of(8,21,24)),
        new Set(B, ImmutableList.of(33,41,42,47,48,49)),     
        new Set(D, ImmutableList.of(
            0,1,2,3,4,5,6,7,9,10,11,12,13,14,15,16,17,18,19,20,23,
            25,26,27,28,29,30,31,32,34,35,36,37,38,39,40,43,44,45,46,22)));
    
    assertMoveOk(vMove(bId,oldStateB,move));
    assertMoveOk(vMove(bId,oldStateB,move2));
    assertMoveOk(vMove(bId,oldStateB,moveAndBeat));
  }
  
  @Test
  public void testBombBeatB() {   
    Map<String, Object> state = ImmutableMap.<String, Object>of(
        BOARD,ImmutableList.of(
            -1,24,21,-1,2,
            -1,-1,22,-1,-1,
            -1,44,8,41,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            33,-1,-1,-1,-1,
            -1,47,-1,-1,42,
            -1,49,48,-1,-1),
        W, ImmutableList.of(2,8,21,22,24),
        B, ImmutableList.of(33,41,42,44,47,48,49),     
        D, ImmutableList.of(
            0,1,3,4,5,6,7,9,10,11,12,13,14,15,16,17,18,19,20,23,
            25,26,27,28,29,30,31,32,34,35,36,37,38,39,40,43,45,46));
    
    List<Operation> moveBombCaptain = ImmutableList.<Operation>of(
        new SetTurn(wId),
        new Set(MOVE,ImmutableList.of(11,12)),
        new Set(BOARD,ImmutableList.of(
            -1,24,21,-1,2,
            -1,-1,22,-1,-1,
            -1,-1,-1,41,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            33,-1,-1,-1,-1,
            -1,47,-1,-1,42,
            -1,49,48,-1,-1)),
        new Set(W, ImmutableList.of(2,21,22,24)),
        new Set(B, ImmutableList.of(33,41,42,47,48,49)),     
        new Set(D, ImmutableList.of(
            0,1,3,4,5,6,7,9,10,11,12,13,14,15,16,17,18,19,20,23,
            25,26,27,28,29,30,31,32,34,35,36,37,38,39,40,43,45,46,44,8)));
    
    List<Operation> moveBombLandmine = ImmutableList.<Operation>of(
        new SetTurn(wId),
        new Set(MOVE,ImmutableList.of(11,7)),
        new Set(BOARD,ImmutableList.of(
            -1,24,21,-1,2,
            -1,-1,-1,-1,-1,
            -1,-1,8,41,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            33,-1,-1,-1,-1,
            -1,47,-1,-1,42,
            -1,49,48,-1,-1)),
        new Set(W, ImmutableList.of(2,8,21,24)),
        new Set(B, ImmutableList.of(33,41,42,47,48,49)),     
        new Set(D, ImmutableList.of(
            0,1,3,4,5,6,7,9,10,11,12,13,14,15,16,17,18,19,20,23,
            25,26,27,28,29,30,31,32,34,35,36,37,38,39,40,43,45,46,44,22)));
    
    assertMoveOk(vMove(bId,state,moveBombCaptain));
    assertMoveOk(vMove(bId,state,moveBombLandmine));
  }
  
  @Test
  public void testBeatB() {   
    List<Operation> move = ImmutableList.<Operation>of(
        new SetTurn(wId),
        new Set(MOVE,ImmutableList.of(13,7)),
        new Set(BOARD,ImmutableList.of(
            -1,24,21,-1,-1,
            -1,-1,41,-1,-1,
            -1,-1,8,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            33,-1,-1,-1,-1,
            -1,47,-1,-1,42,
            -1,49,48,-1,-1)),
        new Set(W, ImmutableList.of(8,21,24)),
        new Set(B, ImmutableList.of(33,41,42,47,48,49)),     
        new Set(D, ImmutableList.of(
            0,1,2,3,4,5,6,7,9,10,11,12,13,14,15,16,17,18,19,20,23,
            25,26,27,28,29,30,31,32,34,35,36,37,38,39,40,43,44,45,46,22)));
    
    assertMoveOk(vMove(bId,oldStateB,move));
  }

  @Test
  public void testIllTurnB() {    
    List<Operation> move = ImmutableList.<Operation>of(
        new SetTurn(bId),
        new Set(MOVE,ImmutableList.of(45,40)),
        new Set(BOARD,ImmutableList.of(
            -1,24,21,-1,-1,
            -1,-1,22,-1,-1,
            -1,-1,8,41,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            33,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,47,-1,-1,42,
            -1,49,48,-1,-1)),
        new Set(W, ImmutableList.of(8,21,22,24)),
        new Set(B, ImmutableList.of(33,41,42,47,48,49)),     
        new Set(D, ImmutableList.of(
            0,1,2,3,4,5,6,7,9,10,11,12,13,14,15,16,17,18,19,20,23,
            25,26,27,28,29,30,31,32,34,35,36,37,38,39,40,43,44,45,46)));

    assertHacker(vMove(bId,oldStateB,move));
  } 
  
  @Test
  public void testIllVanishFlagB() {   
    List<Operation> move = ImmutableList.<Operation>of(
        new SetTurn(wId),
        new Set(MOVE,ImmutableList.of(45,40)),
        new Set(BOARD,ImmutableList.of(
            -1,24,21,-1,-1,
            -1,-1,22,-1,-1,
            -1,-1,8,41,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            33,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,47,-1,-1,42,
            -1,-1,48,-1,-1)),
        new Set(W, ImmutableList.of(8,21,22,24)),
        new Set(B, ImmutableList.of(33,41,42,47,48,49)),     
        new Set(D, ImmutableList.of(
            0,1,2,3,4,5,6,7,9,10,11,12,13,14,15,16,17,18,19,20,23,
            25,26,27,28,29,30,31,32,34,35,36,37,38,39,40,43,44,45,46)));

    assertHacker(vMove(bId,oldStateB,move));
  }
  
  @Test
  public void testIllDoubleMoveB() {
    List<Operation> move = ImmutableList.<Operation>of(
        new SetTurn(wId),
        new Set(MOVE,ImmutableList.of(45,40,54,49)),
        new Set(BOARD,ImmutableList.of(
            -1,24,21,-1,-1,
            -1,-1,22,-1,-1,
            -1,-1,8,41,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            33,-1,-1,-1,-1,
            -1,-1,-1,-1,42,
            -1,47,-1,-1,-1,
            -1,49,48,-1,-1)),
        new Set(W, ImmutableList.of(8,21,22,24)),
        new Set(B, ImmutableList.of(33,41,42,47,48,49)),     
        new Set(D, ImmutableList.of(
            1,2,3,4,5,6,7,9,10,11,12,13,14,15,16,17,18,19,20,23,
            25,26,27,28,29,30,31,32,34,35,36,37,38,39,40,43,44,45,46)));

    assertHacker(vMove(bId,oldStateB,move));
  }
  
  @Test
  public void testIllNoPathB() {    
    List<Operation> move = ImmutableList.<Operation>of(
        new SetTurn(wId),
        new Set(MOVE,ImmutableList.of(45,41)),
        new Set(BOARD,ImmutableList.of(
            -1,24,21,-1,-1,
            -1,-1,22,-1,-1,
            -1,-1,8,41,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,33,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,47,-1,-1,42,
            -1,49,48,-1,-1)),
        new Set(W, ImmutableList.of(8,21,22,24)),
        new Set(B, ImmutableList.of(33,41,42,47,48,49)),     
        new Set(D, ImmutableList.of(
            0,1,2,3,4,5,6,7,9,10,11,12,13,14,15,16,17,18,19,20,23,
            25,26,27,28,29,30,31,32,34,35,36,37,38,39,40,43,44,45,46)));

    assertHacker(vMove(bId,oldStateB,move));
  }
  
  @Test
  public void testIllRailTurnB() {    
    List<Operation> move = ImmutableList.<Operation>of(
        new SetTurn(wId),
        new Set(MOVE,ImmutableList.of(45,31)),
        new Set(BOARD,ImmutableList.of(
            -1,24,21,-1,-1,
            -1,-1,22,-1,-1,
            -1,-1,8,41,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,33,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,47,-1,-1,42,
            -1,49,48,-1,-1)),
        new Set(W, ImmutableList.of(8,21,22,24)),
        new Set(B, ImmutableList.of(33,41,42,47,48,49)),     
        new Set(D, ImmutableList.of(
            0,1,2,3,4,5,6,7,9,10,11,12,13,14,15,16,17,18,19,20,23,
            25,26,27,28,29,30,31,32,34,35,36,37,38,39,40,43,44,45,46)));

    assertHacker(vMove(bId,oldStateB,move));
  }
  
  @Test
  public void testIllRailToOutB() {    
    List<Operation> move = ImmutableList.<Operation>of(
        new SetTurn(wId),
        new Set(MOVE,ImmutableList.of(45,36)),
        new Set(BOARD,ImmutableList.of(
            -1,24,21,-1,-1,
            -1,-1,22,-1,-1,
            -1,-1,8,41,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,33,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,47,-1,-1,42,
            -1,49,48,-1,-1)),
        new Set(W, ImmutableList.of(8,21,22,24)),
        new Set(B, ImmutableList.of(33,41,42,47,48,49)),     
        new Set(D, ImmutableList.of(
            0,1,2,3,4,5,6,7,9,10,11,12,13,14,15,16,17,18,19,20,22,23,
            25,26,27,28,29,30,31,32,34,35,36,37,38,39,40,41,43,44,45,46)));

    assertHacker(vMove(bId,oldStateB,move));
  }
  
  @Test
  public void testIllFlagMoveB() {    
    List<Operation> move = ImmutableList.<Operation>of(
        new SetTurn(wId),
        new Set(MOVE,ImmutableList.of(56,55)),
        new Set(BOARD,ImmutableList.of(
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
            49,-1,48,-1,-1)),
        new Set(W, ImmutableList.of(8,21,22,24)),
        new Set(B, ImmutableList.of(33,41,42,47,48,49)),     
        new Set(D, ImmutableList.of(
            0,1,2,3,4,5,6,7,9,10,11,12,13,14,15,16,17,18,19,20,23,
            25,26,27,28,29,30,31,32,34,35,36,37,38,39,40,43,44,45,46)));
    
    assertHacker(vMove(bId,oldStateB,move));
  }
  
  @Test
  public void testIllLandmineMoveB() {  
    List<Operation> move = ImmutableList.<Operation>of(
        new SetTurn(wId),
        new Set(MOVE,ImmutableList.of(51,50)),
        new Set(BOARD,ImmutableList.of(
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
            47,-1,-1,-1,42,
            -1,49,48,-1,-1)),
        new Set(W, ImmutableList.of(8,21,22,24)),
        new Set(B, ImmutableList.of(33,41,42,47,48,49)),     
        new Set(D, ImmutableList.of(
            0,1,2,3,4,5,6,7,9,10,11,12,13,14,15,16,17,18,19,20,23,
            25,26,27,28,29,30,31,32,34,35,36,37,38,39,40,43,44,45,46)));

    assertHacker(vMove(bId,oldStateB,move));
  } 
  
  @Test
  public void testIllMoveThruRailB() {   
    List<Operation> move = ImmutableList.<Operation>of(
        new SetTurn(wId),
        new Set(MOVE,ImmutableList.of(54,50)),
        new Set(BOARD,ImmutableList.of(
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
            42,47,-1,-1,-1,
            -1,49,48,-1,-1)),
        new Set(W, ImmutableList.of(8,21,22,24)),
        new Set(B, ImmutableList.of(33,41,42,47,48,49)),     
        new Set(D, ImmutableList.of(
            0,1,2,3,4,5,6,7,9,10,11,12,13,14,15,16,17,18,19,20,23,
            25,26,27,28,29,30,31,32,34,35,36,37,38,39,40,43,44,45,46)));

    assertHacker(vMove(bId,oldStateB,move));
  }
  
  @Test
  public void testIllReOrderBeatB() { 
    List<Operation> move = ImmutableList.<Operation>of(
        new SetTurn(wId),
        new Set(MOVE,ImmutableList.of(13,12)),
        new Set(BOARD,ImmutableList.of(
            -1,24,21,-1,-1,
            -1,-1,22,-1,-1,
            -1,-1,41,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            33,-1,-1,-1,-1,
            -1,47,-1,-1,42,
            -1,49,48,-1,-1)),
        new Set(W, ImmutableList.of(21,22,24)),
        new Set(B, ImmutableList.of(33,41,42,47,48,49)),     
        new Set(D, ImmutableList.of(
            0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,23,
            25,26,27,28,29,30,31,32,34,35,36,37,38,39,40,43,44,45,46)));

    assertHacker(vMove(bId,oldStateB,move));    
  }

  @Test
  public void testIllLandmineEnBeatB() {
    List<Operation> move = ImmutableList.<Operation>of(
        new SetTurn(wId),
        new Set(MOVE,ImmutableList.of(13,7)),
        new Set(BOARD,ImmutableList.of(
            -1,24,21,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,8,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            33,-1,-1,-1,-1,
            -1,47,-1,-1,42,
            -1,49,48,-1,-1)),
        new Set(W, ImmutableList.of(8,21,24)),
        new Set(B, ImmutableList.of(33,42,47,48,49)),     
        new Set(D, ImmutableList.of(
            0,1,2,3,4,5,6,7,9,10,11,12,13,14,15,16,17,18,19,20,22,23,
            25,26,27,28,29,30,31,32,34,35,36,37,38,39,40,41,43,44,45,46)));

    assertHacker(vMove(bId,oldStateB,move));
  }
  
  @Test
  public void testIllMoveWPieceB() {
    List<Operation> move = ImmutableList.<Operation>of(
        new SetTurn(wId),
        new Set(MOVE, ImmutableList.of(12,11)),
        new Set(BOARD,ImmutableList.of(
            -1,24,21,-1,-1,
            -1,-1,22,-1,-1,
            -1,8,-1,41,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            33,-1,-1,-1,-1,
            -1,47,-1,-1,42,
            -1,49,48,-1,-1)),
        new Set(W, ImmutableList.of(8,21,22,24)),
        new Set(B, ImmutableList.of(33,41,42,47,48,49)),     
        new Set(D, ImmutableList.of(
            0,1,2,3,4,5,6,7,9,10,11,12,13,14,15,16,17,18,19,20,23,
            25,26,27,28,29,30,31,32,34,35,36,37,38,39,40,43,44,45,46)));

    assertHacker(vMove(bId,oldStateB,move));
  }
  
  @Test
  public void testEndGame(){
    Map<String, Object> state = ImmutableMap.<String, Object>of(
        BOARD,ImmutableList.of(
            -1,24,21,-1,-1,
            -1,41,22,-1,-1,
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
        MOVE, ImmutableList.of(31,12),
        W, ImmutableList.of(8,21,22,24),
        B, ImmutableList.of(33,41,42,47,48,49),     
        D, ImmutableList.of(
            0,1,2,3,4,5,6,7,9,10,11,12,13,14,15,16,17,18,19,20,23,
            25,26,27,28,29,30,31,32,34,35,36,37,38,39,40,43,44,45,46));
    
    List<Operation> move = ImmutableList.<Operation>of(
        new SetTurn(wId),
        new Set(MOVE, ImmutableList.of(6,1)),
        new Set(BOARD,ImmutableList.of(
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
            -1,49,48,-1,-1)),
        new Set(W, ImmutableList.of(8,21,22)),
        new Set(B, ImmutableList.of(33,41,42,47,48,49)),     
        new Set(D, ImmutableList.of(
          0,1,2,3,4,5,6,7,9,10,11,12,13,14,15,16,17,18,19,20,23,
          25,26,27,28,29,30,31,32,34,35,36,37,38,39,40,43,44,45,46,24)),
        new EndGame(bId));    
    assertMoveOk(vMove(bId,state,move));
  }  
  
  @Test
  public void testEndGameNoMove(){
    Map<String, Object> state = ImmutableMap.<String, Object>of(
        BOARD,ImmutableList.of(
            -1,24,21,-1,-1,
            16,41,22,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            33,-1,-1,-1,-1,
            -1,47,-1,-1,42,
            -1,49,48,-1,-1),
        MOVE, ImmutableList.of(31,12),
        W, ImmutableList.of(16,21,22,24),
        B, ImmutableList.of(33,41,42,47,48,49),     
        D, ImmutableList.of(
            0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,17,18,19,20,23,
            25,26,27,28,29,30,31,32,34,35,36,37,38,39,40,43,44,45,46));
    
    List<Operation> move = ImmutableList.<Operation>of(
        new SetTurn(wId),
        new Set(MOVE, ImmutableList.of(6,5)),
        new Set(BOARD,ImmutableList.of(
            -1,24,21,-1,-1,
            -1,-1,22,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            -1,-1,-1,-1,-1,
            33,-1,-1,-1,-1,
            -1,47,-1,-1,42,
            -1,49,48,-1,-1)),
        new Set(W, ImmutableList.of(21,22,24)),
        new Set(B, ImmutableList.of(33,42,47,48,49)),     
        new Set(D, ImmutableList.of(
          0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,17,18,19,20,23,
          25,26,27,28,29,30,31,32,34,35,36,37,38,39,40,43,44,45,46,41,16)),
        new EndGame(bId));    
    assertMoveOk(vMove(bId,state,move));
  }  
}
