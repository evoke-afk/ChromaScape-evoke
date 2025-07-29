package com.chromascape.web.logs;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.springframework.stereotype.Service;

/**
 * Service class responsible for managing application log messages in memory.
 *
 * <p>This service uses a thread-safe {@link ConcurrentLinkedDeque} to store logs in FIFO order with
 * a fixed maximum capacity of 200 entries.
 */
@Service
public class LogService {

  // Thread-safe deque to store log messages
  private final Deque<String> logs = new ConcurrentLinkedDeque<>();

  /**
   * Adds a new log message to the end of the log queue. If the queue exceeds 200 entries, the
   * oldest message is removed.
   *
   * @param log the log message to add
   */
  public void addLog(String log) {
    logs.addLast(log);
    if (logs.size() > 200) {
      logs.removeFirst();
    }
  }

  /**
   * Retrieves a snapshot of all current log messages in insertion order.
   *
   * @return a list of log messages
   */
  public List<String> getLogs() {
    return new ArrayList<>(logs);
  }
}
