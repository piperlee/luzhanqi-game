package org.luzhanqi.graphics;

import org.luzhanqi.client.LuzhanqiLogic;
import org.luzhanqi.client.LuzhanqiPresenter;
import org.game_api.GameApi;
import org.game_api.GameApi.ContainerConnector;
import org.game_api.GameApi.Game;
import org.game_api.GameApi.IteratingPlayerContainer;
import org.game_api.GameApi.UpdateUI;
import org.game_api.GameApi.VerifyMove;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.googlecode.mgwt.dom.client.event.orientation.OrientationChangeEvent;
import com.googlecode.mgwt.dom.client.event.orientation.OrientationChangeHandler;
import com.googlecode.mgwt.ui.client.MGWT;
import com.googlecode.mgwt.ui.client.MGWTSettings;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class LuzhanqiEntryPoint implements EntryPoint {
  //Game
  //IteratingPlayerContainer container;
  //End
  
  //AI
  //AIContainer container;
  //End
  
  //Emulator
  ContainerConnector container;
  //End
  LuzhanqiPresenter luzhanqiPresenter;
  GameMessages messages = (GameMessages)GWT.create(GameMessages.class);

  @Override
  public void onModuleLoad() {
    Game game = new Game() {
      @Override
      public void sendVerifyMove(VerifyMove verifyMove) {
        container.sendVerifyMoveDone(new LuzhanqiLogic().verify(verifyMove));
      }

      @Override
      public void sendUpdateUI(UpdateUI updateUI) {
        luzhanqiPresenter.updateUI(updateUI);
      }
    };
    
    MGWT.applySettings(MGWTSettings.getAppSetting());
    Window.enableScrolling(false);
    Window.addResizeHandler(new ResizeHandler() {
      @Override
      public void onResize(ResizeEvent event) {
        GameSetting.scaleGame();
        scaleFun(GameSetting.getWidth(),GameSetting.getHeight());
      }
    });
    MGWT.addOrientationChangeHandler(new OrientationChangeHandler() {
      @Override
      public void onOrientationChanged(OrientationChangeEvent event) {
        GameSetting.scaleGame();
        scaleFun(GameSetting.getWidth(),GameSetting.getHeight());
      }
    });
    
    //Emulator
    container = new GameApi.ContainerConnector(game);
    //End
    
    //Game
    //container = new IteratingPlayerContainer(game, 2);
    //End
    
    //AI
    //container = new AIContainer(game,1);
    //End
    
    LuzhanqiGraphics luzhanqiGraphics = new LuzhanqiGraphics();
    luzhanqiPresenter =
        new LuzhanqiPresenter(luzhanqiGraphics, container);
    
    
    //Game
//    final ListBox playerSelect = new ListBox();
//    playerSelect.addItem(messages.blackPlayer());
//    playerSelect.addItem(messages.whitePlayer());  
//    playerSelect.addItem(messages.viewer());
//
//    playerSelect.setSelectedIndex(0);
//    playerSelect.addChangeHandler(new ChangeHandler() {
//      @Override
//      public void onChange(ChangeEvent event) {
//        int selectedIndex = playerSelect.getSelectedIndex();
//        String playerId = selectedIndex == 2 ? GameApi.VIEWER_ID
//            : container.getPlayerIds().get(selectedIndex);
//        container.updateUi(playerId);
//      }
//    });
//    FlowPanel flowPanel = new FlowPanel();
//    flowPanel.add(playerSelect);
//    flowPanel.add(luzhanqiGraphics);    
//    RootPanel.get("mainDiv").add(flowPanel);
    //End
    
    //AI
//    final ListBox playerSelect = new ListBox();
//    playerSelect.addItem(messages.blackPlayer());   
//    playerSelect.addItem(messages.viewer());
//  
//    playerSelect.setSelectedIndex(0);
//    playerSelect.addChangeHandler(new ChangeHandler() {
//      @Override
//      public void onChange(ChangeEvent event) {
//        int selectedIndex = playerSelect.getSelectedIndex();
//        String playerId = selectedIndex == 1 ? GameApi.VIEWER_ID
//            : container.getPlayerIds().get(0);
//        container.updateUi(playerId);
//      }
//    });
//    FlowPanel flowPanel = new FlowPanel();
//    flowPanel.add(playerSelect);
//    flowPanel.add(luzhanqiGraphics);    
//    RootPanel.get("mainDiv").add(flowPanel);
    //End
    
    //Emulator
    RootPanel.get("mainDiv").add(luzhanqiGraphics);
    //End
    
    container.sendGameReady();
    
    //Game
    //black start
    //container.updateUi(container.getPlayerIds().get(0));
    //End
    
    //AI
    //black start
    //container.updateUi(container.getPlayerIds().get(0));
    //End
    GameSetting.scaleGame();
  }
  
  public final native void scaleFun(int width, int height) /*-{
    $wnd.setWidthHeight(width, height);
    $wnd.scaleBody();
  }-*/;
} 

