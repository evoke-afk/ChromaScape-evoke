package com.chromascape.web.instance;

/**
 * Represents the configuration settings for running a script instance.
 *
 * <p>Contains the duration the script should run, the script identifier, and a flag indicating
 * whether the client UI is fixed or resizable.
 */
public class RunConfig {

  private final int duration;
  private final String script;
  private final Boolean fixed;

  /**
   * Constructs a new RunConfig with the specified duration, script, and fixed flag.
   *
   * @param duration the runtime duration in minutes for the script
   * @param script the identifier or name of the script to run
   * @param fixed true if the UI layout is fixed; false otherwise
   */
  public RunConfig(int duration, String script, boolean fixed) {
    this.duration = duration;
    this.script = script;
    this.fixed = fixed;
  }

  /**
   * Returns the duration for which the script should run.
   *
   * @return the runtime duration in minutes
   */
  public int getDuration() {
    return duration;
  }

  /**
   * Returns the identifier or name of the script to run.
   *
   * @return the script name or ID
   */
  public String getScript() {
    return script;
  }

  /**
   * Indicates whether the UI layout is fixed.
   *
   * @return true if the UI layout is fixed, false otherwise
   */
  public Boolean isFixed() {
    return fixed;
  }
}
