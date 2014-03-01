package org.luzhanqi.client;

/**
 * Pieces class: key, order, face, player
 */

public class Piece {
  
  public enum PieceType{
    FIELDMARSHAL(9), GENERAL(8), MAJORGENERAL(7), BRIGADIERGENERAL(6),
    COLONEL(5), MAJOR(4), CAPTAIN(3), LIEUTENANT(2), ENGINEER(1),
    BOMB(0), LANDMINE(0), FLAG(0);
    
    private final int rank;
    PieceType(int r){
      this.rank = r;
    }
    public int getValue(){
      return rank;
    }
  }
  
  private int key; // 0-24 white, 25-49 black
  private int order;
  private PieceType face;
  private Turn player;
  private int slot;
  
  public Piece(int k, int slotKey){
    this.key = k;    
    this.face = calcFace(k);
    this.order = this.face.getValue();
    this.player = (this.key < 25)?Turn.W:Turn.B;
    this.slot = slotKey;
  }
  
  public Piece(int k){
    this.key = k;    
    this.face = calcFace(k);
    this.order = this.face.getValue();
    this.player = (this.key < 25)?Turn.W:Turn.B;
    this.slot = -1;
  }
  
  public PieceType calcFace(int k){
    switch (k){
      case 0: case 25:
        return PieceType.FIELDMARSHAL;
      case 1: case 26:
        return PieceType.GENERAL;
      case 2: case 3: case 27: case 28:
        return PieceType.MAJORGENERAL;
      case 4: case 5: case 29: case 30:
        return PieceType.BRIGADIERGENERAL;
      case 6: case 7: case 31: case 32:
        return PieceType.COLONEL;
      case 8: case 9: case 33: case 34:
        return PieceType.MAJOR;
      case 10: case 11: case 12: case 35: case 36: case 37:
        return PieceType.CAPTAIN;
      case 13: case 14: case 15: case 38: case 39: case 40:
        return PieceType.LIEUTENANT;
      case 16: case 17: case 18: case 41: case 42: case 43:
        return PieceType.ENGINEER;
      case 19: case 20: case 44: case 45:
        return PieceType.BOMB;
      case 21: case 22: case 23: case 46: case 47: case 48:
        return PieceType.LANDMINE;
      default:
        return PieceType.FLAG;       
    }    
  }
  
  public int getKey(){
    return this.key;
  }
  
  public int getOrder(){
    return this.order;
  }
  
  public PieceType getFace(){
    return this.face;
  }
  
  public Turn getPlayer(){
    return this.player;
  }
  
  public int getSlot(){
    return this.slot;   
  }
  
//  public void setKey(int k){
//    this.key = k;
//  }
  
  public void setSlot(int slotKey){
    this.slot = slotKey;
  }
  
  public void setOrder(int o){
    this.order = o;
  }
  
  public void setFace(PieceType f){
    this.face = f;
  }
  
  @Override
  public final boolean equals(Object other) {
    if (!(other instanceof Piece)) {
      return false;
    }
    Piece p = (Piece)other;
    return p.getKey() == this.key;
  }

  @Override
  public final int hashCode() {
    return Integer.valueOf(this.key).hashCode();
  }
}
