package org.luzhanqi.client;

import java.util.ArrayList;

import com.google.common.base.Optional;

public class Slot {
  public enum SlotType {
    POST, CAMPSITE, HEADQUARTER // MOUNTAIN, FRONTLINE,
  }
  
  private int key;
  private SlotType type;
  private boolean isEmpty;
  private boolean onRail;
  private ArrayList<Integer> adjSlots;
  private Optional<Piece> curPiece;
  private boolean visited;
  
  public Slot(int key, int pieceKey){
    this.key = key;
    this.type = calcSlotType(key);
    this.onRail = calcOnRail(key);
    this.adjSlots = calcAdjSlots(key);
    this.curPiece = calcPiece(pieceKey);
    this.isEmpty = (pieceKey == -1)? true : false;
    this.visited = false;
  }
  
  public int getKey(){
    return this.key;
  }
  
  public SlotType getType(){
    return this.type;
  }
  
  public boolean emptySlot(){
    return this.isEmpty;
  }
  
  public boolean getOnRail(){
    return this.onRail;
  }
  
  public ArrayList<Integer> getAdjSlots(){
    return this.adjSlots;
  }
  
  public Piece getPiece(){
    if (this.curPiece.isPresent())
      return this.curPiece.get();
    return null;
  }
  
  public boolean getVisited(){
    return this.visited;
  }
  
  public void setVisited(boolean v){
    this.visited = v;
  }
  
  public boolean inAdj(int key){
    return this.adjSlots.contains(key);
  }
  
  public SlotType calcSlotType(int key){
    switch (key){
      case 1 : case 3: case 56: case 58:
        return SlotType.HEADQUARTER;
      case 11: case 13: case 17: case 21: case 23:
      case 36: case 38: case 42: case 46: case 48:
        return SlotType.CAMPSITE;
      default:
        return SlotType.POST;
    }     
  }
  
  public boolean calcOnRail(int key){
    switch (key){
      case 5 : case 6: case 7: case 8: case 9:
      case 10: case 14: case 15: case 19: case 20: case 24: 
      case 25: case 26: case 27: case 28: case 29: 
      case 30: case 31: case 32: case 33: case 34: 
      case 35: case 39: case 40: case 44: case 45: case 49: 
      case 50 : case 51: case 52: case 53: case 54:
        return true;
      default:
        return false;
    }     
  }
  
  public ArrayList<Integer> calcAdjSlots(int key){
    ArrayList<Integer> adj = new ArrayList<Integer>();
    int i = key/5;
    int j = key%5;
    // up 
    if ( i != 0 && i!= 6) adj.add((i-1)*5+j);
    if ( i == 6 && ( j == 0 || j == 2 || j == 4 )) 
      adj.add((i-1)*5+j);
    // right
    if ( j != 4 ) adj.add(i*5+j+1);
    // down
    if ( i != 11 && i!= 5) adj.add((i+1)*5+j);
    if ( i == 5 && ( j == 0 || j == 2 || j == 4 )) 
      adj.add((i+1)*5+j);
    // left
    if ( j != 0 ) adj.add((i*5)+j-1);
    
    // CAMPSITE with POST 
    switch(key){
      case 11: case 13: case 17: case 21: case 23: 
      case 36: case 38: case 42: case 46: case 48:
        adj.add(key-4); adj.add(key+6); adj.add(key+4); adj.add(key-6);
        break;
      case 5: case 30:
        adj.add(key+6);
        break;
      case 25: case 50:
        adj.add(key-4);
        break;
      case 15: case 40:
        adj.add(key-4); adj.add(key+6);
        break;
      case 9: case 34:
        adj.add(key+4);
        break;
      case 29: case 54:
        adj.add(key-6);
        break;
      case 19: case 44:
        adj.add(key+4); adj.add(key-6);
        break;
      case 7: case 32:
        adj.add(key+4); adj.add(key+6);
        break;
      case 27: case 52:
        adj.add(key-4); adj.add(key-6);
        break;
      default:
         break;
    }
    return adj;
  }

  public Optional<Piece> calcPiece(int pieceKey){
    if (pieceKey == -1) return Optional.<Piece>absent();
    return Optional.<Piece>of(new Piece(pieceKey));
  }
  
  public void setPiece(Piece p){
    this.curPiece = Optional.<Piece>of(p); 
    this.isEmpty = false;
  }
  
  public void setEmpty(){
    this.curPiece = null;
    this.isEmpty = true;
  }
  
  public boolean isAdj(int key2){
    return this.adjSlots.contains(key2);
  }
  
  @Override
  public final boolean equals(Object other) {
    if (!(other instanceof Slot)) {
      return false;
    }
    Slot s = (Slot)other;
    return s.getKey() == this.key;
  }

  @Override
  public final int hashCode() {
    return Integer.valueOf(this.key).hashCode();
  }
}
