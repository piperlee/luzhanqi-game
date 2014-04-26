package org.luzhanqi.graphics;

import com.google.gwt.i18n.client.LocalizableResource.DefaultLocale;
import com.google.gwt.i18n.client.Messages;

@DefaultLocale("en")
public interface GameMessages extends Messages {
  @DefaultMessage("Current Turn:")
  String currentTurn();
  
  @DefaultMessage("B")
  String b();
  
  @DefaultMessage("W")
  String w();
  
  @DefaultMessage("S")
  String s();
  
  @DefaultMessage("Game End")
  String gameEnd();
  
  @DefaultMessage("Confirm Move")
  String confirmMove();
  
  @DefaultMessage("Finish Deploy")
  String finishDeploy();
  
  @DefaultMessage("Quick Deploy")
  String quickDeploy();
  
  @DefaultMessage("BlackPlayer")
  String blackPlayer();
  
  @DefaultMessage("WhitePlayer")
  String whitePlayer();
  
  @DefaultMessage("Viewer")
  String viewer();
  
}
