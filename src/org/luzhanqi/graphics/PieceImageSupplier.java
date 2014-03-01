// Copyright 2012 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// //////////////////////////////////////////////////////////////////////////////
package org.luzhanqi.graphics;

import org.luzhanqi.client.Piece;
import com.google.gwt.resources.client.ImageResource;

/**
 * A mapping from Piece to its ImageResource.
 * The images are all of size 77x37 (width x height).
 */
public class PieceImageSupplier {
  private final PieceImages pieceImages;

  public PieceImageSupplier(PieceImages pieceImages) {
    this.pieceImages = pieceImages;
  }

  public ImageResource getResource(PieceImage pieceImage) {
    switch (pieceImage.kind) {
      case EMPTY:
        return getEmpty();
      case NORMAL:
        return getPieceImage(pieceImage.piece);
      default:
        throw new RuntimeException("Forgot kind=" + pieceImage.kind);
    }
  }

  public ImageResource getEmpty() {
    return pieceImages.empty();
  }
  
// TODO: add all the piece image
  public ImageResource getPieceImage(Piece piece) {
    if(piece.getKey()<25){
      switch (piece.getFace()) {
        case FIELDMARSHAL: return pieceImages.w9();
        case GENERAL: return pieceImages.w8();
        case MAJORGENERAL: return pieceImages.w7();
        case BRIGADIERGENERAL: return pieceImages.w6();
        case COLONEL: return pieceImages.w5();
        case MAJOR: return pieceImages.w4();
        case CAPTAIN: return pieceImages.w3();
        case LIEUTENANT: return pieceImages.w2();
        case ENGINEER: return pieceImages.w1();
        case BOMB: return pieceImages.wb();
        case LANDMINE: return pieceImages.wl();
        case FLAG: return pieceImages.wf();
        default:
          throw new RuntimeException("Forgot type=" + piece.getFace());
      }
    }else{
      switch (piece.getFace()) {
        case FIELDMARSHAL: return pieceImages.b9();
        case GENERAL: return pieceImages.b8();
        case MAJORGENERAL: return pieceImages.b7();
        case BRIGADIERGENERAL: return pieceImages.b6();
        case COLONEL: return pieceImages.b5();
        case MAJOR: return pieceImages.b4();
        case CAPTAIN: return pieceImages.b3();
        case LIEUTENANT: return pieceImages.b2();
        case ENGINEER: return pieceImages.b1();
        case BOMB: return pieceImages.bb();
        case LANDMINE: return pieceImages.bl();
        case FLAG: return pieceImages.bf();
        default:
          throw new RuntimeException("Forgot type=" + piece.getFace());
      }
    }
  }
}
