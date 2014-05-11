package org.luzhanqi.graphics;

import com.google.gwt.user.client.Window;

public class GameSetting {
  public static double scale = 0.0;
  private final static int width = 460;
  private final static int height = 940;
  private final static int height_2 = 680;
  public static boolean isDeploy = true;
  
  public static void scaleGame() {
    double scaleX = (double) Window.getClientWidth() / (double) GameSetting.width;
    int h = isDeploy ? height : height_2;
    double scaleY = (double) Window.getClientHeight() / (double) h;
    double scale = Math.min(scaleX, scaleY);
//    scale = (scale > 1.0) ? 1.0 : scale;
    GameSetting.scale = scale;
  }
  
  public static int getWidth() {
    return width;
  }
  
  public static int getHeight() {
    return isDeploy ? height : height_2; 
  }
}
