package org.luzhanqi.client;

//B: 0, W:1
public enum Turn {
  B, W, S;

  public boolean isWhite() {
    return this == W;
  }

  public boolean isBlack() {
    return this == B;
  }
  
  public boolean isStart(){
    return this == S;
  }

  public Turn getOppositeColor() {
    return this == W ? B : W;
  }

}
