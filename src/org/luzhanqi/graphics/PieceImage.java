package org.luzhanqi.graphics;

import java.util.Arrays;

import org.luzhanqi.client.Equality;
import org.luzhanqi.client.Piece;
import org.luzhanqi.client.Piece.PieceType;
import org.luzhanqi.client.Slot;

/**
 * A representation of a piece image.
 */
public final class PieceImage extends Equality {

  enum PieceImageKind {
    EMPTY,
    NORMAL
  }

  public static class Factory {
    public static PieceImage getEmpty(Slot slot) {
      return new PieceImage(PieceImageKind.EMPTY, null, slot);
    }

    public static PieceImage getPieceImage(Piece piece, Slot slot) {
      return new PieceImage(PieceImageKind.NORMAL, piece, slot);
    }
  }

  public final PieceImageKind kind;
  public final Piece piece;
  public final Slot slot;

  private PieceImage(PieceImageKind kind, Piece piece, Slot slot) {
    this.kind = kind;
    this.piece = piece;
    this.slot = slot;
  }

  private String piece2str(Piece piece) {
    if(piece == null) return "ERR";
    String name = (piece.getKey()<25)?"w":"b";
    name += (piece.getFace()==PieceType.FLAG) ? "f"
        :(piece.getFace()==PieceType.BOMB) ? "b"
        :(piece.getFace()==PieceType.LANDMINE) ? "b"
        :piece.getOrder();
    return name;
  }

  @Override
  public String toString() {
    switch (kind) {
      case EMPTY:
        return "pieces/empty.gif";
      case NORMAL:
        return "pieces/" + piece2str(piece) + ".png";
      default:
        return "Forgot kind=" + kind;
    }
  }

  @Override
  public Object getId() {
    return Arrays.asList(piece);
  }
}
