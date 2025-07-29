package com.chromascape.utils.core.input;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Utility class for pausing execution for a specified duration. Provides methods to sleep for a
 * fixed or randomized amount of time.
 *
 * <p>This class is final and cannot be instantiated or extended.
 */
public final class Sleeper {

  /**
   * Pauses the current thread for the specified number of milliseconds.
   *
   * <p>If the sleep is interrupted, this method restores the interrupted status of the thread.
   *
   * @param ms the duration to sleep in milliseconds
   */
  public static void waitMillis(long ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Pauses the current thread for a random duration between {@code min} and {@code max}
   * milliseconds (inclusive).
   *
   * <p>This method internally calls {@link #waitMillis(long)} with a randomly generated delay.
   *
   * @param min the minimum number of milliseconds to sleep (inclusive)
   * @param max the maximum number of milliseconds to sleep (inclusive)
   * @throws IllegalArgumentException if {@code min} is greater than {@code max}
   */
  public static void waitRandomMillis(long min, long max) {
    if (min > max) {
      throw new IllegalArgumentException("min must be less than or equal to max");
    }
    waitMillis(ThreadLocalRandom.current().nextLong(min, max + 1));
  }
}
