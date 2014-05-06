package org.luzhanqi.client;

//

import java.util.Date;

/**
 * Timer.java 
 * @author Ashish
 * This class checks if a timeout has occurred. 
 */
public class MyTimer {

    private long start;
    private int milliseconds;

    /**
     * Constructor
     * @param milliseconds
     */
    public MyTimer(int milliseconds) {
      this.milliseconds = milliseconds;
      if (milliseconds > 0) {
        start = now();
      }
    }

    /**
     * Returns current time.
     * @return
     */
    public long now() {
      return new Date().getTime();
    }

    /**
     * Checks for timeout.
     * @return
     */
    public boolean didTimeout() {
      return milliseconds <= 0 ? false : now() > start + milliseconds;
    }

}
