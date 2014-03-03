package org.luzhanqi.graphics;

import java.util.List;
import java.util.Map;

import org.luzhanqi.client.LuzhanqiPresenter;
import org.luzhanqi.client.LuzhanqiPresenter.LuzhanqiMessage;
import org.luzhanqi.client.LuzhanqiState;
import org.luzhanqi.client.Piece;
import org.luzhanqi.client.Slot;
import org.luzhanqi.client.Turn;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

/**
 * Graphics for the game of luzhanqi.
 */
public class LuzhanqiGraphics extends Composite implements LuzhanqiPresenter.View {
  public interface LuzhanqiGraphicsUiBinder extends UiBinder<Widget, LuzhanqiGraphics> {
  }

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
  
  private boolean deployEnable = false;
  private boolean moveEnable = false;
  private final PieceImageSupplier pieceImageSupplier;
  private LuzhanqiPresenter presenter;
  private Piece selectedPiece;
  private Slot selectedFromSlot;
  private Image selectedFromImage;
  private Slot selectedToSlot;
  
  public LuzhanqiGraphics() {
    PieceImages pieceImages = GWT.create(PieceImages.class);
    this.pieceImageSupplier = new PieceImageSupplier(pieceImages);
    LuzhanqiGraphicsUiBinder uiBinder = GWT.create(LuzhanqiGraphicsUiBinder.class);
    initWidget(uiBinder.createAndBindUi(this));

    // setting initial game grid
    gameGrid.resize(12, 5);
    gameGrid.setCellPadding(0);
    gameGrid.setCellSpacing(9);
    gameGrid.setBorderWidth(0);
    for(int i = 0;i<12;i++){
      for(int j = 0; j<5; j++){
        if(i==6){
          gameGrid.getCellFormatter().setHeight(i, j, "86px");
          gameGrid.getCellFormatter().setVerticalAlignment(i, j, 
              HasVerticalAlignment.ALIGN_BOTTOM);
        }
        else
          gameGrid.getCellFormatter().setHeight(i, j, "38px");
        gameGrid.getCellFormatter().setWidth(i, j, "77px"); 
        
      }
    }   
    // setting initial deploy grid
    deployGrid.setSize("445px", "245px");
    deployGrid.resize(5, 5);
    deployGrid.setCellPadding(0);
    deployGrid.setCellSpacing(10);
    deployGrid.setBorderWidth(0);
    
    //add text note
    note.setReadOnly(true);
    note.setSize("445px", "300px");
    note.setText("Welcome to Game Luzhanqi!\n\n"
        + "Rules: http://en.wikipedia.org/wiki/Luzhanqi\n\n"
        + "How to play:\n"
        + "1.Deploy Phase: each player deploy their own pieces on their own part of the board"
        + "(above half: white player; below half: black player). Click a piece then click a empty"
        + "slot, double click to undo, need to complete all 25 pieces, 'Finish Deploy' button will"
        + "be able. 'Quick Delpoy' will delploy all, and is for test purpose.\n"
        + "2.Game Phase: after both players finishing their deploy, game starts. Click a piece then to some "
        + "position, which will lead to piece-fight or just move. Double click highlight piece "
        + "will undo move. Click 'Confirm Move' button to finish a move.");
  }

  private List<Image> createDeployPieces(Turn turn) {
    List<PieceImage> images = Lists.newArrayList();
    for (int i = 0; i < 25; i++) {
      if(turn == Turn.W)
        images.add(PieceImage.Factory.getPieceImage(new Piece(i), null));
      else if(turn == Turn.B)
        images.add(PieceImage.Factory.getPieceImage(new Piece(i+25), null));
    }
    return createImages(images, true);
  }

  private List<Image> createBoard(List<Slot> slots, boolean withClick) {
    List<PieceImage> images = Lists.newArrayList();
    for (Slot slot: slots) {
      if (slot.getPiece() == null)
         images.add(PieceImage.Factory.getEmpty(slot));
      else
        images.add(PieceImage.Factory.getPieceImage(slot.getPiece(),slot));
    }
    return createImages(images, withClick);
  }
  
  private List<Image> createDeployBoard(List<Slot> slots, Turn turn) {
    List<Image> res = Lists.newArrayList();
    for (Slot slot: slots) {
      final PieceImage pieceImage;
      if (slot.getPiece() == null){
        pieceImage = PieceImage.Factory.getEmpty(slot);
      }
      else{
        pieceImage = PieceImage.Factory.getPieceImage(slot.getPiece(),slot);
      }
      Image image = new Image(pieceImageSupplier.getResource(pieceImage));
      if (enClick(slot.getKey(),turn)) {
        addClick(image,pieceImage);
      }
      res.add(image);
    }
    return res;
  }

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
  
  private Image createSlotImage(Slot slot, boolean withClick){
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
    if(withClick) addClick(image,pieceImage);
    return image;
  }
  
  private Image createPieceImage(Piece piece, boolean withClick){
    PieceImage pieceImage;
    if(piece == null)
      pieceImage = PieceImage.Factory.getEmpty();
    else{
      pieceImage = PieceImage.Factory.getPieceImage(piece,null);
    }
    Image image = new Image(pieceImageSupplier.getResource(pieceImage)); 
    if(withClick) addClick(image,pieceImage);
    return image;
  }
  
  private void addClick(final Image image, final PieceImage pieceImage){
    image.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        //deploy
        if (deployEnable) {
          if(pieceImage.piece != null){
            selectedPiece = pieceImage.piece;
          }else{
            if(selectedPiece != null){
              if(selectedPiece.getSlot()==-1){
                presenter.pieceDeploy(selectedPiece,pieceImage.slot);
                selectedPiece = null;
              }
            }
          }              
        }
        //normal move
        if (moveEnable){
          if(selectedFromSlot!=null && selectedToSlot!=null) return;
          if(selectedFromSlot==null){
            if (presenter.fromValid(pieceImage.slot)){
              image.setStyleName(css.highlighted());
              selectedFromSlot = pieceImage.slot;
              selectedFromImage = image;
            }
          }else{
           if (selectedToSlot==null && 
               presenter.toValid(selectedFromSlot, pieceImage.slot)){
             selectedToSlot = pieceImage.slot;
             moveBtn.setEnabled(true);
             presenter.moveSelected(selectedFromSlot,pieceImage.slot);
           }else if(presenter.fromValid(pieceImage.slot)){
             selectedFromImage.removeStyleName(css.highlighted());
             image.setStyleName(css.highlighted());
             selectedFromSlot = pieceImage.slot;
             selectedFromImage = image;
           }
          }              
        }
      }
    });
    
    image.addDoubleClickHandler(new DoubleClickHandler() {
      @Override
      public void onDoubleClick(DoubleClickEvent event) {
      //deploy
        if (deployEnable) {
          if(pieceImage.slot!=null){
            presenter.pieceDeploy(pieceImage.piece,pieceImage.slot);
          }
        }
        //normal move
        if (moveEnable){
          //TODO:
          if( selectedFromSlot!=null && selectedToSlot!=null
              &&pieceImage.slot.equals(selectedToSlot)){
            setGameGridImage(selectedFromSlot.getKey()/5, selectedFromSlot.getKey()%5,
                createSlotImage(selectedFromSlot,true));
            setGameGridImage(selectedToSlot.getKey()/5, selectedToSlot.getKey()%5,
                createSlotImage(selectedToSlot,true));
            selectedFromSlot = null;
            selectedToSlot = null;
            moveBtn.setEnabled(false);
          }
        }
      }     
    });
  }
  
  private List<Image> createImages(List<PieceImage> images, boolean withClick) {
    List<Image> res = Lists.newArrayList();
    for (PieceImage img : images) {
      final PieceImage imgFinal = img;
      Image image = new Image(pieceImageSupplier.getResource(img));
      if (withClick) {
        addClick(image,imgFinal);
      }
      res.add(image);
    }
    return res;
  }

  private void placeDeployImages(Grid grid, List<Image> images) {
    grid.clear();
    for(int i = 0; i<5; i++){
      for(int j = 0; j<5; j++){
        grid.setWidget(i, j, images.get(i*5+j));
      }
    }
  }
  
  private void placeImages(Grid grid, List<Image> images){
    grid.clear();
    for(int i = 0; i<12; i++){
      for(int j = 0; j<5; j++){
        grid.setWidget(i, j, images.get(i*5+j));
      }
    }
  }
  
  private void setGameGridEmpty(int i, int j, boolean withClick){
    gameGrid.clearCell(i, j);
    gameGrid.setWidget(i, j, createSlotImage(new Slot(i*5+j,-1),withClick));
  }
  
  private void setGameGridImage(int i, int j, Image image){
    gameGrid.clearCell(i, j);
    gameGrid.setWidget(i, j, image);
  }
  
  private void setGameGridByDBW(List<Integer> dbw,Turn turn , boolean withClick){
    if(turn == Turn.W){
      for(int i = 0; i<gameGrid.getRowCount();i++){
        for(int j =0; j<gameGrid.getColumnCount();j++){
          if(i>5)
            setGameGridEmpty(i,j,false);
          else
            setGameGridImage(i,j,createSlotImage(new Slot(i*5+j,dbw.get(i*5+j)),false));
        }
      }
    }else{
      for(int i = 0; i<gameGrid.getRowCount();i++){
        for(int j =0; j<gameGrid.getColumnCount();j++){
          if(i<6)
            setGameGridEmpty(i,j,false);
          else
            setGameGridImage(i,j,createSlotImage(new Slot(i*5+j,dbw.get(i*5+j-30)),false));
        }
      }
    }
  }
  
  private void setDeployGridEmpty(int i, int j, boolean withClick){
    deployGrid.clearCell(i, j);
    deployGrid.setWidget(i, j, createPieceImage(null,withClick));
  }

  /**
  private void setDeployGridImage(int i, int j, Image image){
    deployGrid.clearCell(i, j);
    deployGrid.setWidget(i, j, image);
  }
  */
  
  private void setDeployGridAllEmpty(boolean withClick){
    for(int i = 0; i< deployGrid.getRowCount(); i++){
      for(int j = 0; j< deployGrid.getColumnCount(); j++){
        setDeployGridEmpty(i, j, withClick);
      }
    }
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
//    selectedFromSlot = null;
//    selectedFromImage = null;
//    selectedToSlot = null;
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
          setGameGridImage(i,j,
              createSlotImage(slot,true));
          if(piece!=null){
            presenter.deployMap.put(piece, slot);
          }
        }
      }
    }else{
      for(int i = 6; i<12; i++){
        for(int j = 0; j<5; j++){
          Slot slot = new Slot(i*5+j,list.get(i*5+j));
          Piece piece = slot.getPiece();
          setGameGridImage(i,j,
              createSlotImage(slot,true));
          if(piece!=null){
            presenter.deployMap.put(piece, slot);
          }
        }
      }
    }
    for(int i = 0; i<5; i++){
      for(int j = 0; j<5; j++){
        setDeployGridEmpty(i,j,true);
      }
    }
  }

  @Override
  public void setPresenter(LuzhanqiPresenter luzhanqiPresenter) {
    this.presenter = luzhanqiPresenter;
  }
 
  @Override
  public void setViewerState(int numberOfWhitePieces, int numberOfBlackPieces,
      int numberOfDicardPieces, List<Slot> board,
      LuzhanqiMessage luzhanqiMessage) {
    curTurn.setText("Current Turn: " + presenter.getGameTurn().toString());
    placeImages(gameGrid,createBoard(board,false));
    deployGrid.clear();
    disableClicks(); 
    quickDeploy.setEnabled(false);
  }

  @Override
  public void setPlayerState(int numberOfOpponentPieces,
      int numberOfDiscardPieces, List<Slot> board,
      LuzhanqiMessage luzhanqiMessage) {
    disableClicks();
    LuzhanqiState state = presenter.getState();
    
    curTurn.setText("Current Turn: " + presenter.getGameTurn().toString());
    
    if(luzhanqiMessage == LuzhanqiMessage.IS_DEPLOY){
      quickDeploy.setEnabled(true);
      deployEnable = true;          
      if(state.getDB().isPresent() && presenter.getTurn() == Turn.B){
        setGameGridByDBW(state.getDB().get(),Turn.B,false);
        setDeployGridAllEmpty(false);
      }else if(state.getDW().isPresent() && presenter.getTurn() == Turn.W){
        setGameGridByDBW(state.getDW().get(),Turn.W,false);
        setDeployGridAllEmpty(false);
      }else{
        placeImages(gameGrid,createDeployBoard(board,presenter.getTurn()));
        placeDeployImages(deployGrid,createDeployPieces(presenter.getTurn()));
      }           
      
    }else if(luzhanqiMessage == LuzhanqiMessage.FIRST_MOVE){
      quickDeploy.setEnabled(false);
      placeImages(gameGrid,createBoard(board,false));
      
    }else if(luzhanqiMessage == LuzhanqiMessage.NORMAL_MOVE){
      moveEnable = true;
      quickDeploy.setEnabled(false);
      selectedFromSlot = null;
      selectedToSlot = null;
      selectedFromImage = null;
      if(presenter.isMyTurn())
        placeImages(gameGrid,createBoard(board,true));
      else
        placeImages(gameGrid,createBoard(board,false));
    }else{
      
    }
  }
  
  @Override
  public void deployNextPiece(Map<Piece, Optional<Slot>> lastDeploy) {
    if(!lastDeploy.isEmpty()){
      Piece piece = lastDeploy.keySet().iterator().next();
      Slot slot = lastDeploy.get(piece).isPresent() ?
          lastDeploy.get(piece).get() : null;          
      int pieceRow = (piece.getPlayer()==Turn.B) ?
            piece.getKey()/5-5 : piece.getKey()/5;
      int pieceCol = piece.getKey()%5;
      int slotRow = piece.getSlot()/5;
      int slotCol = piece.getSlot()%5;      
      if (slot == null){        
        setGameGridEmpty(slotRow,slotCol,true);
        deployGrid.clearCell(pieceRow, pieceCol);
        deployGrid.setWidget(pieceRow, pieceCol, 
            createPieceImage(new Piece(piece.getKey(),-1),true));
      }else{        
        setDeployGridEmpty(pieceRow,pieceCol,true);
        gameGrid.clearCell(slotRow, slotCol);
        gameGrid.setWidget(slotRow, slotCol, createSlotImage(slot,true));
      }
    }
    // All 25 pieces are deployed
    deployBtn.setEnabled(!lastDeploy.isEmpty() && presenter.deployMap.size() == 25);
  }

  @Override
  public void nextFromTo(List<Slot> fromTo) {
    if(!fromTo.isEmpty()){
      Slot from = fromTo.get(0);
      Slot to = fromTo.get(1);
      int fromRow = from.getKey()/5;
      int fromCol = from.getKey()%5;
      int toRow = to.getKey()/5;
      int toCol = to.getKey()%5;
      gameGrid.setWidget(fromRow, fromCol, 
          createSlotImage(new Slot(from.getKey(),-1),true));
      Image image = createSlotImage(new Slot(to.getKey(),from.getPiece().getKey()),true);   
      gameGrid.setWidget(toRow, toCol, image);
      image.setStyleName(css.highlighted());
    }
    moveEnable = true;
    moveBtn.setEnabled(!fromTo.isEmpty());
  }
}
