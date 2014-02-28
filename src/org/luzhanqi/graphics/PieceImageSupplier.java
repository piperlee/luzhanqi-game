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
        case FIELDMARSHAL: return pieceImages.w8();
        case GENERAL: return pieceImages.w8();
        case MAJORGENERAL: return pieceImages.w8();
        case BRIGADIERGENERAL: return pieceImages.w8();
        case COLONEL: return pieceImages.w8();
        case MAJOR: return pieceImages.w8();
        case CAPTAIN: return pieceImages.w8();
        case LIEUTENANT: return pieceImages.w8();
        case ENGINEER: return pieceImages.w8();
        case BOMB: return pieceImages.w8();
        case LANDMINE: return pieceImages.w8();
        case FLAG: return pieceImages.w8();
        default:
          throw new RuntimeException("Forgot type=" + piece.getFace());
      }
    }else{
      switch (piece.getFace()) {
        case FIELDMARSHAL: return pieceImages.b8();
        case GENERAL: return pieceImages.b8();
        case MAJORGENERAL: return pieceImages.b8();
        case BRIGADIERGENERAL: return pieceImages.b8();
        case COLONEL: return pieceImages.b8();
        case MAJOR: return pieceImages.b8();
        case CAPTAIN: return pieceImages.b8();
        case LIEUTENANT: return pieceImages.b8();
        case ENGINEER: return pieceImages.b8();
        case BOMB: return pieceImages.b8();
        case LANDMINE: return pieceImages.b8();
        case FLAG: return pieceImages.b8();
        default:
          throw new RuntimeException("Forgot type=" + piece.getFace());
      }
    }
  }
}
