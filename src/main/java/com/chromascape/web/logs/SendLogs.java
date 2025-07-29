package com.chromascape.web.logs;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for exposing in-memory application logs over HTTP.
 *
 * <p>Provides an endpoint to retrieve all logs stored by {@link LogService}.
 */
@RestController
@RequestMapping("/api")
public class SendLogs {

  private final LogService logService;

  /**
   * Constructs the controller with an injected {@link LogService}.
   *
   * @param logService the service managing log storage
   */
  public SendLogs(LogService logService) {
    this.logService = logService;
  }

  /**
   * Returns the list of current application logs.
   *
   * @return a list of log messages
   */
  @GetMapping("/logs")
  public List<String> getLogs() {
    return logService.getLogs();
  }
}
