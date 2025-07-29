package com.chromascape.web;

import com.chromascape.web.instance.ScriptInstanceManager;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * REST controller for retrieving script progress updates.
 *
 * <p>This endpoint provides the current progress percentage of the active script managed by the
 * {@link ScriptInstanceManager}.
 */
@Controller
@RequestMapping("/api")
public class ProgressUpdate {

  /**
   * Returns the current progress of the running script as a percentage.
   *
   * <p>If no script is running, returns 0.
   *
   * @return a {@link ResponseEntity} containing the script progress percentage
   */
  @GetMapping("/progress")
  public ResponseEntity<Integer> progressUpdate() {
    int percent =
        ScriptInstanceManager.getInstance().getInstanceRef() != null
            ? ScriptInstanceManager.getInstance()
                .getInstanceRef()
                .getScriptInstance()
                .getProgressPercent()
            : 0;
    return ResponseEntity.ok(percent);
  }
}
