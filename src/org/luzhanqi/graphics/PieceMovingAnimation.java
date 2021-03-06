package org.luzhanqi.graphics;

import org.luzhanqi.client.Piece;
import org.luzhanqi.client.Slot;
import org.luzhanqi.client.Turn;

import com.allen_sauer.gwt.voices.client.Sound;
import com.google.gwt.animation.client.Animation;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.media.client.Audio;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * Modified from https://code.google.com/p/nyu-gaming-course-2013/source/-
 * browse/trunk/eclipse/src/org/simongellis/hw5/PieceMovingAnimation.java
 */
public class PieceMovingAnimation extends Animation {

  GameCSS css;
  PieceImages pieceImages = GWT.create(PieceImages.class);
  PieceImageSupplier supplier = new PieceImageSupplier(pieceImages);
  Grid deployGrid;
  Grid gameGrid;
  AbsolutePanel panel;
  Image start, moving;
  SimplePanel startPanel, endPanel;
  ImageResource piece, transform;
  int startX, startY, startWidth, startHeight;
  int endX, endY;
  Audio soundAtEnd;
  Sound mobileSoundAtEnd;
  boolean cancelled;
  
  boolean isDeployed = false;
  boolean isNormalMove = false;

  public PieceMovingAnimation(Grid gameGrid, Grid deployGrid, Piece from, Slot to, 
       SimplePanel startPanel, SimplePanel endPanel, Audio sfx) {
    isDeployed = true;
    this.startPanel = startPanel;
    start = (Image)startPanel.getWidget();
    this.endPanel = endPanel;
    piece = supplier.getPieceImage(from);
    transform = to.emptySlot() ? piece : piece;
    if (from.getPlayer() == Turn.W) {
      startX = deployGrid.getWidget(from.getKey()/5, from.getKey()%5).getAbsoluteLeft();
      startY = deployGrid.getWidget(from.getKey()/5, from.getKey()%5).getAbsoluteTop();
    } else {
      startX = deployGrid.getWidget((from.getKey()-25)/5, from.getKey()%5).getAbsoluteLeft();
      startY = deployGrid.getWidget((from.getKey()-25)/5, from.getKey()%5).getAbsoluteTop();
    }
    startWidth = start.getWidth();
    startHeight = start.getHeight();
    panel = (AbsolutePanel) gameGrid.getParent().getParent().getParent();
    endX = gameGrid.getWidget(to.getKey()/5, to.getKey()%5).getAbsoluteLeft();
    endY = gameGrid.getWidget(to.getKey()/5, to.getKey()%5).getAbsoluteTop();
    soundAtEnd = sfx;
    cancelled = false;

    //start.setResource(supplier.getEmpty());
    startPanel.clear();
    moving = new Image(piece);
    moving.setPixelSize(startWidth, startHeight);

    panel.add(moving, startX, startY);
  }

  public PieceMovingAnimation(Grid gameGrid, Slot from, Slot to,
      SimplePanel startPanel, SimplePanel endPanel, Audio sfx) {
    isNormalMove = true;
    this.startPanel = startPanel;
    start = (Image)startPanel.getWidget();
    this.endPanel = endPanel;
    
    piece = supplier.getPieceImage(from.getPiece());
    transform = to.emptySlot() ? piece : piece;
    
    startX = gameGrid.getWidget(from.getKey()/5, from.getKey()%5).getAbsoluteLeft();
    startY = gameGrid.getWidget(from.getKey()/5, from.getKey()%5).getAbsoluteTop();
    startWidth = start.getWidth();
    startHeight = start.getHeight();
    panel = (AbsolutePanel) gameGrid.getParent().getParent().getParent();
    endX = gameGrid.getWidget(to.getKey()/5, to.getKey()%5).getAbsoluteLeft();
    endY = gameGrid.getWidget(to.getKey()/5, to.getKey()%5).getAbsoluteTop();
    soundAtEnd = sfx;
    cancelled = false;

    //start.setResource(supplier.getEmpty());
    startPanel.clear();
    moving = new Image(piece);
    moving.setPixelSize(startWidth, startHeight);

    panel.add(moving, startX, startY);
  }
  
  // mobile ver
  public PieceMovingAnimation(Grid gameGrid, Grid deployGrid, Piece from, Slot to, 
      SimplePanel startPanel, SimplePanel endPanel, Sound sfx) {
   isDeployed = true;
   this.startPanel = startPanel;
   start = (Image)startPanel.getWidget();
   this.endPanel = endPanel;
   piece = supplier.getPieceImage(from);
   transform = to.emptySlot() ? piece : piece;
   if (from.getPlayer() == Turn.W) {
     startX = (int) (deployGrid.getWidget(from.getKey()/5, from.getKey()%5).getAbsoluteLeft()
         / GameSetting.scale );
     startY = (int) (deployGrid.getWidget(from.getKey()/5, from.getKey()%5).getAbsoluteTop()
         / GameSetting.scale );
   } else {
     startX = (int) (deployGrid.getWidget((from.getKey()-25)/5, from.getKey()%5).getAbsoluteLeft() 
         / GameSetting.scale );
     startY = (int) (deployGrid.getWidget((from.getKey()-25)/5, from.getKey()%5).getAbsoluteTop()
         / GameSetting.scale );
   }
   startWidth = start.getWidth();
   startHeight = start.getHeight();
   panel = (AbsolutePanel) gameGrid.getParent().getParent().getParent();
   endX = (int) (gameGrid.getWidget(to.getKey()/5, to.getKey()%5).getAbsoluteLeft()
       / GameSetting.scale );
   endY = (int) (gameGrid.getWidget(to.getKey()/5, to.getKey()%5).getAbsoluteTop()
       / GameSetting.scale );
   mobileSoundAtEnd = sfx;
   cancelled = false;

   //start.setResource(supplier.getEmpty());
   startPanel.clear();
   moving = new Image(piece);

   moving.setPixelSize(startWidth, startHeight);
   panel.add(moving, startX, startY);
 }

 public PieceMovingAnimation(Grid gameGrid, Slot from, Slot to,
     SimplePanel startPanel, SimplePanel endPanel, Sound sfx) {
   isNormalMove = true;
   this.startPanel = startPanel;
   start = (Image)startPanel.getWidget();
   this.endPanel = endPanel;
   
   piece = supplier.getPieceImage(from.getPiece());
   transform = to.emptySlot() ? piece : piece;
   
   startX = (int)(gameGrid.getWidget(from.getKey()/5, from.getKey()%5).getAbsoluteLeft()
       / GameSetting.scale );
   startY = (int)(gameGrid.getWidget(from.getKey()/5, from.getKey()%5).getAbsoluteTop()
       / GameSetting.scale );
   startWidth = start.getWidth();
   startHeight = start.getHeight();
   panel = (AbsolutePanel) gameGrid.getParent().getParent().getParent();
   endX = (int)(gameGrid.getWidget(to.getKey()/5, to.getKey()%5).getAbsoluteLeft()
       / GameSetting.scale );
   endY = (int)(gameGrid.getWidget(to.getKey()/5, to.getKey()%5).getAbsoluteTop()
       / GameSetting.scale );
   mobileSoundAtEnd = sfx;
   cancelled = false;

   //start.setResource(supplier.getEmpty());
   startPanel.clear();
   moving = new Image(piece);
   moving.setPixelSize(startWidth, startHeight);

   panel.add(moving, startX, startY);
 }
 
  @Override
  protected void onUpdate(double progress) {
//    System.out.println(progress);
    int x = (int) (startX + (endX - startX) * progress);
    int y = (int) (startY + (endY - startY) * progress);
    double scale = 1 + 0.5 * Math.sin(progress * Math.PI);
    int width = (int) (startWidth * scale);
    int height = (int) (startHeight * scale);
    moving.setPixelSize(width, height);

    x -= (width - startWidth) / 2;
    y -= (height - startHeight) / 2;
    panel.remove(moving);
    moving = new Image(piece.getSafeUri());
    moving.setPixelSize(width, height);
    panel.add(moving, x, y);
  }

  @Override
  protected void onCancel() {
    cancelled = true;
    panel.remove(moving);
  }

  @Override
  protected void onComplete() {
    if (!cancelled) {
      if (soundAtEnd != null) {
        soundAtEnd.play();
      } else if (mobileSoundAtEnd != null) {
        mobileSoundAtEnd.play();
      }
      //endPanel.add(new Image(transform));
      startPanel.clear();
      endPanel.clear();
      endPanel.add(start);
      panel.remove(moving);
      isDeployed = false;
      isNormalMove = false;
    }
  }
    
}