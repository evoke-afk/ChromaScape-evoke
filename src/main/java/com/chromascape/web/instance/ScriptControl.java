package com.chromascape.web.instance;

import com.chromascape.web.logs.LogService;
import java.lang.reflect.InvocationTargetException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller that handles starting and stopping of scripts, and querying their running state.
 *
 * <p>Provides endpoints to submit a run configuration, start a script instance, stop the currently
 * running script, and check if a script is running. All responses include appropriate HTTP status
 * codes and messages.
 */
@RestController
@RequestMapping("/api")
public class ScriptControl {

  private final LogService logService;

  /**
   * Constructs the controller with the given LogService for logging script events.
   *
   * @param logService the logging service instance
   */
  public ScriptControl(LogService logService) {
    this.logService = logService;
  }

  /**
   * Starts a script based on the provided run configuration.
   *
   * <p>Validates the input configuration fields: script name, duration, and window style. If valid,
   * it attempts to instantiate and start the script. Logs relevant information and returns HTTP
   * status codes accordingly.
   *
   * @param config the RunConfig object containing script parameters (JSON in request body)
   * @return ResponseEntity with status and message indicating success or error details
   */
  @PostMapping(path = "/runConfig", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> getRunConfig(@RequestBody RunConfig config) {
    try {
      // Validation checks
      if (config.getScript() == null || config.getScript().isEmpty()) {
        logService.addLog("No script is selected");
        return ResponseEntity.badRequest().body("Script must be specified.");
      }

      if (config.getDuration() <= 0) {
        logService.addLog("Duration incorrectly specified");
        return ResponseEntity.badRequest().body("Duration must be greater than 0.");
      }

      if (config.isFixed() == null) {
        logService.addLog("No window style selected");
        return ResponseEntity.badRequest().body("Window style (Fixed?) must be specified.");
      }

      logService.addLog("Config valid: attempting to run script");

      // Instantiate and start the script instance
      ScriptInstance instance = new ScriptInstance(config, logService);
      ScriptInstanceManager.getInstance().setInstance(instance);
      instance.start();

      return ResponseEntity.ok("Script started successfully.");

    } catch (ClassNotFoundException e) {
      logService.addLog("Script class not found: " + e.getMessage());
      return ResponseEntity.badRequest().body("Script class not found.");
    } catch (NoSuchMethodException e) {
      logService.addLog("Script constructor not found: " + e.getMessage());
      return ResponseEntity.badRequest().body("Script constructor not valid.");
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
      logService.addLog("Failed to instantiate script: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to start script.");
    } catch (Exception e) {
      logService.addLog("Unexpected error: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Unexpected error: " + e.getMessage());
    }
  }

  /**
   * Stops the currently running script instance.
   *
   * <p>Logs the stop request and interrupts the running script thread.
   *
   * @return ResponseEntity with HTTP 200 OK status after attempting to stop the script
   */
  @PostMapping(path = "/stop", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> stopScript() {
    logService.addLog("Received stop request");
    ScriptInstanceManager.getInstance().getInstanceRef().stop();
    return ResponseEntity.ok().build();
  }

  /**
   * Checks whether a script instance is currently running.
   *
   * @return true if a script is running, false otherwise
   */
  @GetMapping(path = "/isRunning", produces = MediaType.APPLICATION_JSON_VALUE)
  public boolean getIsRunning() {
    ScriptInstance instance = ScriptInstanceManager.getInstance().getInstanceRef();
    return instance != null && instance.isRunning();
  }
}
