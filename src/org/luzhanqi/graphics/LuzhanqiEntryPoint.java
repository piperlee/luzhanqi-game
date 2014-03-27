package org.luzhanqi.graphics;

import org.luzhanqi.client.LuzhanqiLogic;
import org.luzhanqi.client.LuzhanqiPresenter;
import org.game_api.GameApi;
import org.game_api.GameApi.ContainerConnector;
import org.game_api.GameApi.Game;
import org.game_api.GameApi.UpdateUI;
import org.game_api.GameApi.VerifyMove;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class LuzhanqiEntryPoint implements EntryPoint {
  ContainerConnector container;
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
    container = new GameApi.ContainerConnector(game);
    LuzhanqiGraphics luzhanqiGraphics = new LuzhanqiGraphics();
    luzhanqiPresenter =
        new LuzhanqiPresenter(luzhanqiGraphics, container);
    
    RootPanel.get("mainDiv").add(luzhanqiGraphics);
    container.sendGameReady();

  }
}
