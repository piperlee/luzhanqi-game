package org.luzhanqi.graphics;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.DataResource;

public interface GameSounds extends ClientBundle {
  @Source("sounds/pieceCaptured.mp3")
  DataResource pieceCapturedMp3();

  @Source("sounds/pieceCaptured.wav")
  DataResource pieceCapturedWav();

  @Source("sounds/pieceDown.mp3")
  DataResource pieceDownMp3();

  @Source("sounds/pieceDown.wav")
  DataResource pieceDownWav();
}
