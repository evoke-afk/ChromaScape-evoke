package com.chromascape.web.instance;

import com.chromascape.base.BaseScript;
import com.chromascape.web.logs.LogService;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Manages the lifecycle of a script instance.
 *
 * <p>This class dynamically loads and instantiates a script class based on the provided
 * configuration, runs the script in its own thread, and provides control methods to start and stop
 * the script execution.
 */
public class ScriptInstance {

  private final BaseScript instance;
  private volatile Thread thread;

  /**
   * Constructs a ScriptInstance by dynamically loading the script class specified in the config.
   *
   * <p>The script class must have a constructor with the signature: {@code (boolean fixed, int
   * duration, LogService logService)}.
   *
   * @param config the RunConfig containing script name, duration, and UI layout flag
   * @param logService the logging service to be passed to the script constructor
   * @throws NoSuchMethodException if the expected constructor is not found
   * @throws ClassNotFoundException if the script class cannot be found
   * @throws InvocationTargetException if the constructor throws an exception
   * @throws InstantiationException if the class is abstract or an interface
   * @throws IllegalAccessException if the constructor is not accessible
   */
  public ScriptInstance(RunConfig config, LogService logService)
      throws NoSuchMethodException,
          ClassNotFoundException,
          InvocationTargetException,
          InstantiationException,
          IllegalAccessException {

    String fileName = config.getScript();
    String className = fileName.replace(".java", "");

    Class<?> script = Class.forName("com.chromascape.scripts." + className);
    Constructor<?> constructor =
        script.getDeclaredConstructor(boolean.class, int.class, LogService.class);
    instance =
        (BaseScript) constructor.newInstance(config.isFixed(), config.getDuration(), logService);
  }

  /** Starts the script execution in a new thread. */
  public void start() {
    thread = new Thread(instance::run);
    thread.start();
  }

  /**
   * Stops the script execution by requesting the script to stop, interrupting the running thread,
   * and waiting for it to terminate.
   */
  public void stop() {
    instance.stop();
    if (thread != null) {
      thread.interrupt();
      try {
        thread.join();
      } catch (InterruptedException ignored) {
        // Thread join interrupted, ignore to proceed with shutdown
      }
    }
  }

  /**
   * Checks whether the script thread is currently running.
   *
   * @return true if the script thread exists and is alive; false otherwise
   */
  public boolean isRunning() {
    return thread != null && thread.isAlive();
  }

  /**
   * Returns the underlying BaseScript instance.
   *
   * @return the BaseScript instance managed by this ScriptInstance
   */
  public BaseScript getScriptInstance() {
    return instance;
  }
}
