package org.luzhanqi.graphics;

import org.luzhanqi.client.LuzhanqiLogic;
import org.luzhanqi.client.LuzhanqiPresenter;
import org.game_api.GameApi;
import org.game_api.GameApi.Game;
import org.game_api.GameApi.IteratingPlayerContainer;
import org.game_api.GameApi.UpdateUI;
import org.game_api.GameApi.VerifyMove;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class LuzhanqiEntryPoint implements EntryPoint {
  IteratingPlayerContainer container;
  LuzhanqiPresenter luzhanqiPresenter;

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
    container = new IteratingPlayerContainer(game, 2);
    LuzhanqiGraphics luzhanqiGraphics = new LuzhanqiGraphics();
    luzhanqiPresenter =
        new LuzhanqiPresenter(luzhanqiGraphics, container);
    final ListBox playerSelect = new ListBox();
    playerSelect.addItem("WhitePlayer");
    playerSelect.addItem("BlackPlayer");   
    playerSelect.addItem("Viewer");
    playerSelect.setSelectedIndex(1);
   // playerSelect.addItem("WhitePlayerDeploy");
   // playerSelect.addItem("BlackPlayerDeploy");
    playerSelect.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        int selectedIndex = playerSelect.getSelectedIndex();
        String playerId = selectedIndex == 2 ? GameApi.VIEWER_ID
            : container.getPlayerIds().get(selectedIndex);
        container.updateUi(playerId);
      }
    });
    FlowPanel flowPanel = new FlowPanel();
    flowPanel.add(playerSelect);
    flowPanel.add(luzhanqiGraphics);    
    RootPanel.get("mainDiv").add(flowPanel);
    container.sendGameReady();
    //black start
    container.updateUi(container.getPlayerIds().get(1));
  }
}