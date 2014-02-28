package org.luzhanqi.graphics;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.luzhanqi.client.LuzhanqiPresenter;
import org.luzhanqi.client.LuzhanqiPresenter.LuzhanqiMessage;
import org.luzhanqi.client.Piece;
import org.luzhanqi.client.Slot;
import org.luzhanqi.client.Turn;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

/**
 * Graphics for the game of luzhanqi.
 */
public class LuzhanqiGraphics extends Composite implements LuzhanqiPresenter.View {
  public interface LuzhanqiGraphicsUiBinder extends UiBinder<Widget, LuzhanqiGraphics> {
  }

//  @UiField
//  HorizontalPanel boardArea;
  @UiField
  Grid gameGrid;
  @UiField
  Grid deployGrid;
//  @UiField
//  HorizontalPanel pieceArea;
  @UiField
  Button deployBtn;
  @UiField
  Button moveBtn;
  private boolean deployEnableClicks = false;
  private boolean moveEnableClicks = false;
  private final PieceImageSupplier pieceImageSupplier;
  private LuzhanqiPresenter presenter;
  private Piece selectedPiece;
  private Slot selectedSlot;


  public LuzhanqiGraphics() {
    PieceImages pieceImages = GWT.create(PieceImages.class);
    this.pieceImageSupplier = new PieceImageSupplier(pieceImages);
    LuzhanqiGraphicsUiBinder uiBinder = GWT.create(LuzhanqiGraphicsUiBinder.class);
    initWidget(uiBinder.createAndBindUi(this));
  }

  private List<Image> createDeployPieces(Turn turn) {
    List<PieceImage> images = Lists.newArrayList();
    for (int i = 0; i < 25; i++) {
      if(turn == Turn.W)
        images.add(PieceImage.Factory.getPieceImage(new Piece(i), null));
      else if(turn == Turn.B)
        images.add(PieceImage.Factory.getPieceImage(new Piece(i+25), null));
    }
    return createImages(images, false);
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

  private List<Image> createDeployMap(Map<Piece, Slot> deployMap, boolean withClick) {
    List<PieceImage> images = Lists.newArrayList();
    for (Entry<Piece,Slot> entry: deployMap.entrySet()) {
        images.add(PieceImage.Factory.getPieceImage(entry.getKey(),entry.getValue()));
    }
    return createImages(images, withClick);
  }
  
  private Image createImage(Slot slot, boolean withClick){
    PieceImage pieceImage;
    if (slot.emptySlot()){
      pieceImage = PieceImage.Factory.getEmpty(slot);           
    }else{
      pieceImage = PieceImage.Factory.getPieceImage(slot.getPiece(),slot);
    }
    Image image = new Image(pieceImageSupplier.getResource(pieceImage)); 
    if(withClick) addClick(image,pieceImage);
    return image;
  }
  
  private void addClick(Image image, final PieceImage pieceImage){
    image.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        //deploy
        if (deployEnableClicks) {
          if(pieceImage.piece != null){
            selectedPiece = pieceImage.piece;
          }else{
            if(selectedPiece != null){
              presenter.pieceDeploy(selectedPiece,pieceImage.slot);
              selectedPiece = null;
            }
          }              
        }
        //normal move
        else{
          if(selectedSlot == null){
            if (pieceImage.piece != null)
              selectedSlot = pieceImage.slot;
          }else{
            presenter.moveSelected(selectedSlot,pieceImage.slot);
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

  private void placeImages(Grid grid, List<Image> images) {
    grid.clear();
    //Image last = images.isEmpty() ? null : images.get(images.size() - 1);
    for (Image image : images) {
      FlowPanel imageContainer = new FlowPanel();
      //imageContainer.setStyleName(image != last ? "imgShortContainer" : "imgContainer");
      imageContainer.add(image);
      grid.add(imageContainer);
    }
  }

  @UiHandler("deployBtn")
  void onClickDelployBtn(ClickEvent e) {
    deployBtn.setEnabled(false);
    deployEnableClicks = false;
    presenter.finishedDeployingPieces();
  }
  
  @UiHandler("moveBtn")
  void onClickMoveBtn(ClickEvent e) {
    moveBtn.setEnabled(false);
    moveEnableClicks = false;
    presenter.finishedNormalMove();
  }

  @Override
  public void setPresenter(LuzhanqiPresenter luzhanqiPresenter) {
    this.presenter = luzhanqiPresenter;
  }
 
  @Override
  public void setViewerState(int numberOfWhitePieces, int numberOfBlackPieces,
      int numberOfDicardPieces, List<Slot> board,
      LuzhanqiMessage luzhanqiMessage) {
    // TODO Auto-generated method stub
    placeImages(gameGrid,createBoard(board,false));
    deployBtn.setEnabled(false);
    moveBtn.setEnabled(false);    
  }

  @Override
  public void setPlayerState(int numberOfOpponentPieces,
      int numberOfDiscardPieces, List<Slot> board,
      LuzhanqiMessage luzhanqiMessage) {
    // TODO Auto-generated method stub
    if(luzhanqiMessage == LuzhanqiMessage.IS_DEPLOY){
      placeImages(gameGrid,createBoard(board,true));
      placeImages(deployGrid,createDeployPieces(presenter.getTurn()));
      deployEnableClicks = true;
      deployBtn.setEnabled(true);
    }else if(luzhanqiMessage == LuzhanqiMessage.FIRST_MOVE){
      placeImages(gameGrid,createBoard(board,false));
      deployBtn.setEnabled(false);
      deployEnableClicks = false;
      moveBtn.setEnabled(false);
      moveEnableClicks = false;
    }else if(luzhanqiMessage == LuzhanqiMessage.NORMAL_MOVE){
      placeImages(gameGrid,createBoard(board,false));
      deployBtn.setEnabled(false);
      deployEnableClicks = false;
      moveBtn.setEnabled(true);
      moveEnableClicks = true;
    }    
  }
  
  @Override
  public void deployNextPiece(Map<Piece, Slot> deployMap) {
    // TODO Auto-generated method stub
    //placeImages(gameGrid,createDeployMap(deployMap,true));
    
    deployBtn.setEnabled(!deployMap.isEmpty());
  }

  @Override
  public void nextFromTo(List<Slot> fromTo) {
    // TODO Auto-generated method stub
    Slot from = fromTo.get(0);
    Slot to = fromTo.get(1);
    int fromRow = from.getKey()/5;
    int fromCol = from.getKey()%5;
    int toRow = to.getKey()/5;
    int toCol = to.getKey()%5;
    gameGrid.setWidget(fromRow, fromCol, createImage(from,true));
    gameGrid.setWidget(toRow, toCol, createImage(to,true));
  }
}
