package org.luzhanqi.client;

//W: 0, B:1
public enum Turn {
  W, B, S;

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
