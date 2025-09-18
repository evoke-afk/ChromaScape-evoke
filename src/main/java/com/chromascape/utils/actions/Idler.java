package com.chromascape.utils.actions;

import com.chromascape.base.BaseScript;
import com.chromascape.utils.core.screen.colour.ColourInstances;
import com.chromascape.utils.core.screen.colour.ColourObj;
import com.chromascape.utils.domain.ocr.Ocr;
import java.awt.Rectangle;
import java.time.Duration;
import java.time.Instant;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class for handling idle behavior in scripts.
 *
 * <p>This class provides functionality to pause execution for a given amount of time, or until the
 * game client indicates the player has become idle again through a chat message.
 *
 * <p>You can add ChatRed directly to your colours/colours.json file by adding this. { "name" :
 * "ChatRed", "min" : [ 177, 229, 239, 0 ], "max" : [ 179, 240, 240, 0 ] }
 */
public class Idler {

  private static final Logger logger = LogManager.getLogger(Idler.class);
  private static volatile String lastMessage = "";

  /**
   * Waits until either the specified timeout has elapsed or until the client chatbox reports that
   * the player is idle.
   *
   * <p>Specifically, this method monitors the "Latest Message" zone in the chatbox for a red
   * message containing the substring {@code "idle"} or {@code "moving"}, which typically appears
   * when using the Idle Notifier plugin
   *
   * @param base the active {@link BaseScript} instance, usually passed as {@code this}
   * @param timeoutSeconds the maximum number of seconds to remain idle before continuing
   */
  public static void waitUntilIdle(BaseScript base, int timeoutSeconds) {
    BaseScript.checkInterrupted();
    try {
      Instant start = Instant.now();
      Instant deadline = start.plus(Duration.ofSeconds(timeoutSeconds));
      while (Instant.now().isBefore(deadline)) {
        Rectangle latestMessage = base.controller().zones().getChatTabs().get("Latest Message");
        ColourObj red = ColourInstances.getByName("ChatRed");
        ColourObj black = ColourInstances.getByName("Black");
        String idleText = Ocr.extractText(latestMessage, "Plain 12", red, true);
        String timeStamp = Ocr.extractText(latestMessage, "Plain 12", black, true);
        if ((idleText.contains("moving") || idleText.contains("idle"))
            && !timeStamp.equals(lastMessage)) {
          lastMessage = timeStamp;
          return;
        }
      }
    } catch (Exception e) {
      logger.error("Error while waiting for idle", e);
    }
  }

  public static void wait(int seconds) {
    try {
      Thread.sleep(seconds * 1000);
    } catch (InterruptedException e) {
      logger.error("Error while waiting", e);
    }
  }
}
