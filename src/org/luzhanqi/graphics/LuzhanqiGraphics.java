package org.luzhanqi.graphics;

import java.util.List;
import java.util.Map;

import org.luzhanqi.client.LuzhanqiPresenter;
import org.luzhanqi.client.LuzhanqiPresenter.LuzhanqiMessage;
import org.luzhanqi.client.LuzhanqiState;
import org.luzhanqi.client.Piece;
import org.luzhanqi.client.Slot;
import org.luzhanqi.client.Turn;

import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.DragEndEvent;
import com.allen_sauer.gwt.dnd.client.DragHandler;
import com.allen_sauer.gwt.dnd.client.DragHandlerAdapter;
import com.allen_sauer.gwt.dnd.client.DragStartEvent;
import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.allen_sauer.gwt.dnd.client.VetoDragException;
import com.allen_sauer.gwt.dnd.client.drop.SimpleDropController;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.AudioElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.media.client.Audio;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

/**
 * Graphics for the game of luzhanqi.
 */
public class LuzhanqiGraphics extends Composite implements LuzhanqiPresenter.View {
  public interface LuzhanqiGraphicsUiBinder extends UiBinder<Widget, LuzhanqiGraphics> {
  }
  public interface Dropper {
    public abstract void onDrop(int row, int col);
  }

  public final static int GAME_ROW = 12;
  public final static int GAME_COL = 5;
  public final static int DEPLOY_ROW = 5;
  public final static int DEPLOY_COL = 5;
  
  @UiField
  Grid gameGrid;
  @UiField
  Grid deployGrid;
  @UiField
  Button deployBtn;
  @UiField
  Button moveBtn;
  // test button
  @UiField
  Button quickDeploy;
  @UiField
  Label curTurn;
  @UiField
  GameCSS css;
  @UiField
  TextArea note;
  
  //setting SimplePanel
  SimplePanel [][] gamePanels = new SimplePanel[GAME_ROW][GAME_COL];
  HandlerRegistration [][] gpClick = new HandlerRegistration [GAME_ROW][GAME_COL];
  HandlerRegistration [][] gpDBClick = new HandlerRegistration [GAME_ROW][GAME_COL];
  SimplePanel [][] deployPanels = new SimplePanel[DEPLOY_ROW][DEPLOY_COL];
  
  private final PieceImageSupplier pieceImageSupplier;
  private LuzhanqiPresenter presenter;
  
  private boolean deployEnable = false;
  private boolean moveEnable = false;  
  private Piece selectedPiece;
  private Image selectedPieceImage;
  private Slot targetSlot;
  private Image targetImage;
  private Image doubleClickedImage;
  private Slot selectedFromSlot;
  private Image selectedFromImage;
  private Slot selectedToSlot;
  private Image selectedToImage;
  
  // Advanced Graphics
  private static GameSounds gameSounds;
  private PickupDragController dragController;
  private DragHandler dragHandler;
  private Dropper dropHandler;
  private AbsolutePanel abPanel;
  private PieceMovingAnimation animation;
  private Audio pieceDown;
  private Audio pieceCaptured;
  private boolean isDrag = false;
  
  public LuzhanqiGraphics() {
    PieceImages pieceImages = GWT.create(PieceImages.class);
    this.pieceImageSupplier = new PieceImageSupplier(pieceImages);
    LuzhanqiGraphicsUiBinder uiBinder = GWT.create(LuzhanqiGraphicsUiBinder.class);
    initWidget(uiBinder.createAndBindUi(this));
    gameSounds = GWT.create(GameSounds.class);
    abPanel = (AbsolutePanel)gameGrid.getParent().getParent().getParent();

    initializeDragnDrop();
    dragController = new PickupDragController(abPanel, false);
    dragController.setBehaviorDragStartSensitivity(3);
    dragController.addDragHandler(dragHandler);
    dragController.setBehaviorMultipleSelection(false);
    // setting initial game grid
    gameGrid.resize(GAME_ROW, GAME_COL);
    gameGrid.setCellPadding(0);
    gameGrid.setCellSpacing(9);
    gameGrid.setBorderWidth(0);
    for(int i = 0;i<GAME_ROW;i++){
      for(int j = 0; j<GAME_COL; j++){        
        if(i==6) {
          gameGrid.getCellFormatter().setHeight(i, j, "86px");
          gameGrid.getCellFormatter().setVerticalAlignment(i, j, 
              HasVerticalAlignment.ALIGN_BOTTOM);
        }
        else {
          gameGrid.getCellFormatter().setHeight(i, j, "38px");
        }
        gameGrid.getCellFormatter().setWidth(i, j, "77px");   
        gamePanels[i][j] = new SimplePanel();
        gamePanels[i][j].setSize("77px", "38px");

        final SimplePanel target = gamePanels[i][j];
        final int row = i, col = j;
        SimpleDropController dropController = new SimpleDropController(target) {
          @Override
          public void onDrop(DragContext context) {
            //deploy phase
            if (selectedPiece!=null) {              
              //from deploy board to game board
              if(selectedPiece.getSlot()==-1 && enClick(row*GAME_COL+col,presenter.getTurn())) {                  
                targetSlot = new Slot(row*GAME_COL+col,-1);
                presenter.pieceDeploy(selectedPiece, targetSlot);
                selectedPiece = null;
                selectedPieceImage.removeStyleName(css.highlighted());
                selectedPieceImage = null;
                super.onDrop(context);
              }
              //back to deploy board
              else if (selectedPiece.getSlot()==-1) {
                int offset = selectedPiece.getPlayer() == Turn.B ? 25 : 0;
                int row = (selectedPiece.getKey()-offset)/DEPLOY_COL;
                int col = (selectedPiece.getKey()-offset)%DEPLOY_COL;
                deployPanels[row][col].clear();
                deployPanels[row][col].add(selectedPieceImage);
              }
            } 
            //normal move
            if (selectedFromSlot!=null && selectedToSlot == null) {
              int slotKey = row * GAME_COL + col;
              Slot slot = presenter.getState().getBoard().get(slotKey);
              // target is empty or is taken by opponent's piece
              if (slot.emptySlot() || slot.getPiece().getPlayer()!=presenter.getTurn()) {
                if(selectedFromSlot != null && presenter.toValid(selectedFromSlot,slot)){            
                  presenter.moveSelected(selectedFromSlot,slot);
                  moveBtn.setEnabled(true);
                  selectedFromImage.removeStyleName(css.highlighted());
                  selectedToSlot = slot;
                  super.onDrop(context);
                } 
              }              
            }
            isDrag = false;
          }
          
          @Override
          public void onPreviewDrop(DragContext context) throws VetoDragException {
            int slotKey = row * GAME_COL + col;
            Slot slot = presenter.getState().getBoard().get(slotKey);
            //Image image = (I)context.draggable
            if (target.getWidget() != null) {             
              // can not drop piece on one's own piece
              if (!presenter.isSTurn()) {
                if (presenter.isMyTurn() && selectedFromSlot != null
                    && slot.getPiece().getPlayer() != presenter.getTurn()
                    && presenter.toValid(selectedFromSlot, slot)
                    && presenter.fromValid(selectedFromSlot)) {
                    selectedToImage = (Image)target.getWidget();
                    target.clear();
                } else {
                  isDrag = false;
                  throw new VetoDragException();
                }
              } else { //deploy phase
                isDrag = false;
                throw new VetoDragException();
              }
            } else { // empty target
              if ( presenter.isMyTurn() && selectedFromSlot != null) {
                if (!presenter.toValid(selectedFromSlot, slot) 
                    || !presenter.fromValid(selectedFromSlot)) {
                  isDrag = false;
                  throw new VetoDragException();
                }
              }
              if (presenter.isSTurn() && selectedPiece!=null
                  && !presenter.deployValid(selectedPiece.getKey(), slotKey)) {
                isDrag = false;
                throw new VetoDragException();
              }
            }
            super.onPreviewDrop(context);
          }
        };
        dragController.registerDropController(dropController);
        gameGrid.setWidget(i, j, gamePanels[i][j]);
      }      
    }
    
    // setting initial deploy grid
    deployGrid.setSize("445px", "245px");
    deployGrid.resize(DEPLOY_ROW, DEPLOY_COL);
    deployGrid.setCellPadding(0);
    deployGrid.setCellSpacing(10);
    deployGrid.setBorderWidth(0);
    for(int i = 0;i<DEPLOY_ROW; i++) {
      for(int j = 0; j<DEPLOY_COL; j++) {   
        deployPanels[i][j] = new SimplePanel();
        deployPanels[i][j].setSize("77px", "38px");
        deployGrid.setWidget(i, j, deployPanels[i][j]);
      }
    }
    //add text note
    note.setReadOnly(true);
    note.setSize("445px", "300px");
    note.setText("Welcome to Game Luzhanqi!\n\n"
        + "Rules: http://en.wikipedia.org/wiki/Luzhanqi\n\n"
        + "NEW:\n"
        + "Animation, Sound, Drag and Drop\n\n"
        + "How to play:\n"
        + "1.Deploy Phase: each player deploy their own pieces on their own part of the board"
        + "(above half: white player; below half: black player). Click a piece then click a empty"
        + "slot, double click to undo, need to complete all 25 pieces, 'Finish Deploy' button will"
        + "be able. 'Quick Delpoy' will delploy all, and is for test purpose.\n"
        + "2.Game Phase: after both players finishing their deploy, game starts. Click a piece then to some "
        + "position, which will lead to piece-fight or just move. Double click highlight piece "
        + "will undo move. Click 'Confirm Move' button to finish a move.");
    
    //add Audio
    if (Audio.isSupported()) {
      pieceDown = Audio.createIfSupported();
      pieceDown.addSource(gameSounds.pieceDownMp3().getSafeUri()
                      .asString(), AudioElement.TYPE_MP3);
      pieceDown.addSource(gameSounds.pieceDownWav().getSafeUri()
                      .asString(), AudioElement.TYPE_WAV);
      pieceCaptured = Audio.createIfSupported();
      pieceCaptured.addSource(gameSounds.pieceCapturedMp3().getSafeUri()
                      .asString(), AudioElement.TYPE_MP3);
      pieceCaptured.addSource(gameSounds.pieceCapturedWav().getSafeUri()
                      .asString(), AudioElement.TYPE_WAV);
    }    
  }

  private void initializeDragnDrop() {
    dragHandler = new DragHandlerAdapter(){
      @Override
      public void onDragStart(DragStartEvent event) {
        Image image = (Image) event.getContext().draggable;
        isDrag = true;
        //deploy phase
        if (presenter.isSTurn()) {
          if (selectedPiece == null || selectedPieceImage == image) {
            int pieceKey = getKeyFromDeployPanels(image,presenter.getTurn());
            selectedPiece = new Piece(pieceKey,-1);
            selectedPieceImage = image;
            image.setStyleName(css.highlighted());
          }
        } 
        //normal move
        else if (presenter.isMyTurn()) {
          if (selectedFromSlot == null || selectedFromImage == image) {
            int slotKey = getSlotKeyFromGamePanels(image);
            selectedFromSlot = presenter.getState().getBoard().get(slotKey);
            selectedFromImage = image;
            image.setStyleName(css.highlighted());
          }
        }
      }
//      @Override
//      public void onPreviewDragStart(DragStartEvent event) throws VetoDragException {
//        if (presenter.isSTurn()) {
//          
//        } else if (presenter.isMyTurn()) {
//          if (selectedFromSlot != null) {
//            throw new VetoDragException();
//          }
//        } else {
//          throw new VetoDragException();
//        }
//        super.onPreviewDragStart(event);
//      }
    };
    
    dropHandler = new Dropper() {
      @Override
      public void onDrop(int row, int col) {
       
      }      
    };
  }
  
  private int getKeyFromDeployPanels(Image image, Turn turn) {
    for (int i = 0; i<DEPLOY_ROW; i++) {
      for (int j = 0; j<DEPLOY_COL; j++) {
        if (image.equals(deployPanels[i][j].getWidget())) {
          return (turn == Turn.B) ? (i*DEPLOY_COL+j+25):(i*DEPLOY_COL+j);
        }
      }
    }
    return -1;
  }
  
  private int getSlotKeyFromGamePanels(Image image) {
    for (int i = 0; i<GAME_ROW; i++) {
      for (int j = 0; j<GAME_COL; j++) {
        if (image.equals(gamePanels[i][j].getWidget())) {
          return i*GAME_COL+j;
        }
      }
    }
    return -1;
  }  
  
//  private int getPieceKeyFromLePanels(Image image, Turn turn) {
  
  private boolean enClick(int slotKey, Turn turn){
    ImmutableList<Integer> set = ImmutableList.<Integer>of(11,13,17,21,23,36,38,42,46,48);
    if (set.contains(slotKey)) return false;
    if (turn == Turn.B && slotKey < 30) return false;
    if (turn == Turn.W && slotKey > 29) return false;
    return true;
  }
  
  private void disableClicks(){
    moveBtn.setEnabled(false);
    moveEnable = false;
    deployBtn.setEnabled(false);
    deployEnable = false;
  }
  
  private Image createSlotImage(Slot slot, boolean draggable){
    PieceImage pieceImage;
    if(slot == null)
      pieceImage = PieceImage.Factory.getEmpty();
    else{
      if (slot.emptySlot()){
        pieceImage = PieceImage.Factory.getEmpty(slot);           
      }else{
        pieceImage = PieceImage.Factory.getPieceImage(slot.getPiece(),slot);
      }
    }
    Image image = new Image(pieceImageSupplier.getResource(pieceImage)); 
    if (draggable) {
      dragController.makeDraggable(image);
    }
    return image;
  }
   
  private Image createPieceImage(Piece piece, boolean draggable){
    PieceImage pieceImage;
    if(piece == null)
      pieceImage = PieceImage.Factory.getEmpty();
    else{
      pieceImage = PieceImage.Factory.getPieceImage(piece,null);
    }
    Image image = new Image(pieceImageSupplier.getResource(pieceImage)); 
    if (draggable) {
      dragController.makeDraggable(image);
    }
    return image;
  }
  
  private List<Image> createImages(List<PieceImage> images, boolean draggable) {
    List<Image> res = Lists.newArrayList();
    for (PieceImage img : images) {
      Image image = new Image(pieceImageSupplier.getResource(img));
      if (draggable) {
        dragController.makeDraggable(image);
      }
      res.add(image);
    }
    return res;
  }
 
  private void setDeployPanelEmpty(int i, int j, boolean withClick){
    deployPanels[i][j].clear();
  }

  @UiHandler("deployBtn")
  void onClickDelployBtn(ClickEvent e) {
    deployBtn.setEnabled(false);
    deployEnable = false;
    presenter.finishedDeployingPieces();
  }
  
  @UiHandler("moveBtn")
  void onClickMoveBtn(ClickEvent e) {
    moveBtn.setEnabled(false);
    presenter.finishedNormalMove();
    moveBtn.setEnabled(false);
  }
  
  // For test purpose
  @UiHandler("quickDeploy")
  void onClickQuickDeploy(ClickEvent e){
    deployBtn.setEnabled(true);
    deployEnable = true;
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
    if(presenter.getTurn() == Turn.W){
      for(int i = 0; i<6;i++){
        for(int j = 0; j<5; j++){
          Slot slot = new Slot(i*5+j,list.get(i*5+j));
          Piece piece = slot.getPiece(); 
          if(piece!=null){
            gamePanels[i][j].clear();
            gamePanels[i][j].add(createSlotImage(slot,true));
            presenter.deployMap.put(piece, slot);
          }
        }
      }
    }else{
      for(int i = 6; i<12; i++){
        for(int j = 0; j<5; j++){
          Slot slot = new Slot(i*5+j,list.get(i*5+j));
          Piece piece = slot.getPiece();
          if(piece!=null){
            gamePanels[i][j].clear();
            gamePanels[i][j].add(createSlotImage(slot,true));
            presenter.deployMap.put(piece, slot);
          }
        }
      }
    }
    clearDeployPanels();
  }

  @Override
  public void setPresenter(LuzhanqiPresenter luzhanqiPresenter) {
    this.presenter = luzhanqiPresenter;
  }
 
  @Override
  public void setViewerState(int numberOfWhitePieces, int numberOfBlackPieces,
      int numberOfDicardPieces, List<Slot> board,
      LuzhanqiMessage luzhanqiMessage) {
    if (presenter.getIsEndGame()){
      curTurn.setText("GAME END");
    }else{
      curTurn.setText("Current Turn: " + presenter.getGameTurn().toString());
    }
    setViewerGamePanelsByBoard(board);
    clearDeployPanels();
    disableClicks(); 
    quickDeploy.setEnabled(false);
  }

  @Override
  public void setPlayerState(int numberOfOpponentPieces,
      int numberOfDiscardPieces, List<Slot> board,
      LuzhanqiMessage luzhanqiMessage) {
    disableClicks();
    LuzhanqiState state = presenter.getState();
    if (presenter.getIsEndGame()){
      curTurn.setText("GAME END");
    }else{
      curTurn.setText("Current Turn: " + presenter.getGameTurn().toString());
    }
    
    if(luzhanqiMessage == LuzhanqiMessage.IS_DEPLOY){
      quickDeploy.setEnabled(true);
      deployEnable = true;
      // after B "Finish Deploy"
      if(state.getDB().isPresent() && presenter.getTurn() == Turn.B){
        quickDeploy.setEnabled(false);
        setGamePanelsByDBorW(state.getDB().get(),Turn.B);
        clearDeployPanels();
      }
      // after W "Finish Deploy"
      else if(state.getDW().isPresent() && presenter.getTurn() == Turn.W){
        quickDeploy.setEnabled(false);
        setGamePanelsByDBorW(state.getDW().get(),Turn.W);
        clearDeployPanels();
      }
      // during deploy
      else{
        setDeployPhaseGamePanels(presenter.getTurn());
        initializeDeployPanels(presenter.getTurn());
      }                 
    }else if(luzhanqiMessage == LuzhanqiMessage.FIRST_MOVE){
      quickDeploy.setEnabled(false);
      setGamePanelsByBoard(board,presenter.getTurn());
      
    }else if(luzhanqiMessage == LuzhanqiMessage.NORMAL_MOVE){
      moveEnable = true;
      quickDeploy.setEnabled(false);
      selectedFromSlot = null;
      selectedToSlot = null;
      selectedFromImage = null;
      selectedToImage =null;
      if(presenter.isMyTurn() && !presenter.getIsEndGame()) {
        setGamePanelsByBoard(board,presenter.getTurn());
      } else {
        setViewerGamePanelsByBoard(board);
      }
    }else{
      System.out.println("EHEHEHE");
    }
  }
  
  private void setDeployPhaseGamePanels(Turn turn) {
    for (int i = 0; i < GAME_ROW; i++) {
      for (int j = 0; j < GAME_COL; j++) {
        //deploy phase, game grid is all empty
        gamePanels[i][j].clear();
        if (enClick(i*GAME_COL+j,turn)) {   
          final Slot slot = new Slot(i*GAME_COL+j,-1);
          gamePanels[i][j].sinkEvents(Event.ONCLICK);
          gamePanels[i][j].addHandler(new ClickHandler(){
            @Override
            public void onClick(ClickEvent event) {
              if(selectedPiece != null) {
                if (slot.emptySlot()) {
                  if(selectedPiece.getSlot()==-1) {                  
                    presenter.pieceDeploy(selectedPiece,slot);
                    selectedPiece = null;
                    selectedPieceImage.removeStyleName(css.highlighted());
                    selectedPieceImage = null;
                  }
                }
              }
            }            
          }, ClickEvent.getType());
          gamePanels[i][j].sinkEvents(Event.ONDBLCLICK);
          gamePanels[i][j].addDomHandler(new DoubleClickHandler(){           
            @Override
            public void onDoubleClick(DoubleClickEvent event) {
              if (!slot.emptySlot()) {
                Piece piece = slot.getPiece();
                //keep image info, use in deployNextPiece
                doubleClickedImage = (Image)
                    gamePanels[slot.getKey()/GAME_COL][slot.getKey()%GAME_COL].getWidget();
                presenter.pieceDeploy(piece, slot);
                gamePanels[slot.getKey()/GAME_COL][slot.getKey()%GAME_COL].clear();
              } else {
                if ( targetSlot!=null && !targetSlot.emptySlot()) {
                  Piece piece = targetSlot.getPiece();
                  //keep image info, use in deployNextPiece
                  doubleClickedImage = (Image)
                    gamePanels[targetSlot.getKey()/GAME_COL][targetSlot.getKey()%GAME_COL].getWidget();
                  presenter.pieceDeploy(piece, targetSlot);
                  gamePanels[targetSlot.getKey()/GAME_COL][targetSlot.getKey()%GAME_COL].clear();
                  targetSlot = null;
                }
              }
            }            
          }, DoubleClickEvent.getType());
        }
      }
    }    
  }

  private void initializeDeployPanels(Turn turn) {
    List<PieceImage> pieceImages = Lists.newArrayList();
    for (int i = 0; i < (DEPLOY_ROW * DEPLOY_COL); i++) {
      if(turn == Turn.W)
        pieceImages.add(PieceImage.Factory.getPieceImage(new Piece(i), null));
      else if(turn == Turn.B)
        pieceImages.add(PieceImage.Factory.getPieceImage(new Piece(i+25), null));
    }
    List<Image> images = createImages(pieceImages,true);
    for(int i = 0; i< DEPLOY_ROW; i++){
      for(int j = 0; j< DEPLOY_COL; j++){
        deployPanels[i][j].clear();
        final Image image = images.get(i * DEPLOY_COL + j);
        final Piece piece = pieceImages.get(i * DEPLOY_COL + j).piece;
        deployPanels[i][j].add(image);
        deployPanels[i][j].sinkEvents(Event.ONCLICK);
        deployPanels[i][j].addHandler(new ClickHandler(){
          @Override
          public void onClick(ClickEvent event) {
            //click a piece
            if(piece != null) {
              if (selectedPiece != null) {
                selectedPieceImage.removeStyleName(css.highlighted());
              }
              image.setStyleName(css.highlighted());
              selectedPiece = piece;
              selectedPieceImage = image;
            } 
          }
        }, ClickEvent.getType());
      }
    }
  }

  private void clearDeployPanels() {
    for(int i = 0; i< DEPLOY_ROW; i++){
      for(int j = 0; j< DEPLOY_COL; j++){
        setDeployPanelEmpty(i, j, false);
      }
    }
  }
  
  private void setGamePanelsByDBorW(List<Integer> list, Turn turn) {
    if(turn == Turn.W){
      for(int i = 0; i<GAME_ROW; i++){
        for(int j =0; j<GAME_COL; j++){
          if (i < 6) {
            int slotKey = i * GAME_COL + j;
            int pieceKey = list.get(slotKey);
            Slot slot = new Slot(slotKey,pieceKey);
            if (!slot.emptySlot()) {
              gamePanels[i][j].clear();
              gamePanels[i][j].add(createSlotImage(slot,false));
            }
          }
        }
      }
    }else if (turn == Turn.B){
      for(int i = 0; i<GAME_ROW; i++){
        for(int j =0; j<GAME_COL; j++){
          if (i > 5) {
            int slotKey = i * GAME_COL + j;
            int pieceKey = list.get(slotKey - 30);
            Slot slot = new Slot(slotKey,pieceKey);
            if (!slot.emptySlot()) {
              gamePanels[i][j].clear();
              gamePanels[i][j].add(createSlotImage(slot,false));
            }
          }
        }
      }
    }    
  }
  
  private void setGamePanelsByBoard(List<Slot> board, Turn turn) {
    
    for (int i = 0 ; i < GAME_ROW; i++) {
      for (int j = 0; j < GAME_COL; j++) {
        final Slot slot = board.get(i*GAME_COL+j);
        final Turn t = turn;
        if (!slot.emptySlot()) {
          if (slot.getPiece().getPlayer() == turn) {
            Image image = createSlotImage(slot,true);
            gamePanels[i][j].clear();
            gamePanels[i][j].add(image);
          } else {
            Image image = createSlotImage(slot,false);
            gamePanels[i][j].clear();
            gamePanels[i][j].add(image);
          }         
        }          
        final SimplePanel panel = gamePanels[i][j];
        gamePanels[i][j].sinkEvents(Event.ONCLICK);
        if (gpClick[i][j] != null ) {
          gpClick[i][j].removeHandler();
        }
        gpClick[i][j] = gamePanels[i][j].addHandler(new ClickHandler(){
          @Override
          public void onClick(ClickEvent event) {
            // avoid double click
            if (selectedToSlot == null) {
              // place piece onto, on empty or opponent's piece
              if (slot.emptySlot() || slot.getPiece().getPlayer() != t) {
                if(selectedFromSlot != null 
                    &&  presenter.fromValid(selectedFromSlot)
                    && presenter.toValid(selectedFromSlot,slot)){            
                  presenter.moveSelected(selectedFromSlot,slot);
                  moveBtn.setEnabled(true);
                  selectedFromImage.removeStyleName(css.highlighted());
                  selectedToSlot = slot;
                  if (!slot.emptySlot()) {
                    selectedToImage = (Image)panel.getWidget();
                  }
                } 
              } else { // non-empty with one's own piece
                // pick a piece
                if (selectedFromSlot!=null) { //change selection
                  selectedFromImage.removeStyleName(css.highlighted());
                }
                selectedFromSlot = slot;
                selectedFromImage = (Image)panel.getWidget();
                selectedFromImage.setStyleName(css.highlighted());
              }
            }   
          }
        }, ClickEvent.getType());
        gamePanels[i][j].sinkEvents(Event.ONDBLCLICK);
        if (gpDBClick[i][j] != null ) {
          gpDBClick[i][j].removeHandler();
        }
        gpDBClick[i][j] = gamePanels[i][j].addDomHandler(new DoubleClickHandler(){           
          @Override
          public void onDoubleClick(DoubleClickEvent event) {
            if ( selectedFromSlot != null && selectedToSlot != null 
                && slot.equals(selectedToSlot)) {
              int fromI = selectedFromSlot.getKey()/GAME_COL;
              int fromJ = selectedFromSlot.getKey()%GAME_COL;
              int toI = selectedToSlot.getKey()/GAME_COL;
              int toJ = selectedToSlot.getKey()%GAME_COL;
              gamePanels[fromI][fromJ].clear();
              gamePanels[fromI][fromJ].setWidget(selectedFromImage);
              gamePanels[toI][toJ].clear();
              if (selectedToImage != null) {
                gamePanels[toI][toJ].setWidget(selectedToImage);
              }
              selectedFromSlot = null;
              selectedFromImage = null;
              selectedToSlot = null;
              selectedToImage = null;
              moveBtn.setEnabled(false);
              isDrag = false;
            }         
          }            
        }, DoubleClickEvent.getType());
     
      }
    }
  }
  
  private void setViewerGamePanelsByBoard(List<Slot> board) {
    for (int i = 0 ; i < GAME_ROW; i++) {
      for (int j = 0; j < GAME_COL; j++) {
        Slot slot = board.get(i*GAME_COL+j);
        gamePanels[i][j].clear();
        if (!slot.emptySlot()) {
          Image image = createSlotImage(slot,false);
          gamePanels[i][j].add(image);        
        }
        if (gpClick[i][j] != null ) {
          gpClick[i][j].removeHandler();
        }
        if (gpDBClick[i][j] != null ) {
          gpDBClick[i][j].removeHandler();
        }
      }
    }
  }  

  @Override
  public void deployNextPiece(Map<Piece, Optional<Slot>> lastDeploy) {
    if(!lastDeploy.isEmpty()){
      Piece piece = lastDeploy.keySet().iterator().next();
      Slot slot = lastDeploy.get(piece).isPresent() ?
          lastDeploy.get(piece).get() : null;          
      int pieceRow = (piece.getPlayer()==Turn.B) ?
            piece.getKey()/DEPLOY_COL-5 : piece.getKey()/DEPLOY_COL;
      int pieceCol = piece.getKey()%DEPLOY_COL;      
      //double click
      if (slot == null){        
        deployPanels[pieceRow][pieceCol].add(doubleClickedImage);
      }else{
        int slotRow = piece.getSlot()/GAME_COL;
        int slotCol = piece.getSlot()%GAME_COL;
        if (isDrag) {
          gamePanels[slotRow][slotCol].clear();
          gamePanels[slotRow][slotCol].add(selectedPieceImage);
          deployPanels[pieceRow][pieceCol].clear();
        } else {
          animation = new PieceMovingAnimation(gameGrid, deployGrid,piece,slot,
              deployPanels[pieceRow][pieceCol],gamePanels[slotRow][slotCol],pieceDown);
          animation.run(1000);
        }
      }
    }
    isDrag = false;
    // All 25 pieces are deployed
    deployBtn.setEnabled(!lastDeploy.isEmpty() && presenter.deployMap.size() == 25);
  }

  @Override
  public void nextFromTo(List<Slot> fromTo) {
    if(!fromTo.isEmpty()){
      Slot from = fromTo.get(0);
      Slot to = fromTo.get(1);
      int fromRow = from.getKey()/GAME_COL;
      int fromCol = from.getKey()%GAME_COL;
      int toRow = to.getKey()/GAME_COL;
      int toCol = to.getKey()%GAME_COL;
      if (isDrag) {
        gamePanels[fromRow][fromCol].clear();
        gamePanels[toRow][toCol].clear();
        gamePanels[toRow][toCol].add(selectedFromImage);
      } else {
        Audio ad = to.emptySlot()? pieceDown : pieceCaptured;
        animation = new PieceMovingAnimation(gameGrid,from,to,
            gamePanels[fromRow][fromCol],gamePanels[toRow][toCol],ad);
        animation.run(1000);
      }
    }
    isDrag = false;
    moveEnable = true;
    moveBtn.setEnabled(!fromTo.isEmpty());
  }
  
  @Override
  public void playPieceDownSound() {
    if (pieceDown != null) {
      pieceDown.play();
    }
  }

  @Override
  public void playPieceCapturedSound() {
    if (pieceCaptured != null) {
      pieceCaptured.play();
    }
  }
}
