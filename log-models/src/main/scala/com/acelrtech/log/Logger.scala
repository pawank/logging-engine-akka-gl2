package com.acelrtech.log

/**
* Logger contract
*
*/
trait Logger {
    /**
     * Enable / disable overall logger for logging
     */
    val isEnabled:Boolean
    /**
     * Enable / disable `INFO` messages
     */
    val isInfoEnabled:Boolean

    /**
    * Enable / disable `DEBUG` messages
     */
    val isDebugEnabled:Boolean

    /**
     * Enable / disable `WARN` messages
     */
    val isWarningEnabled:Boolean

    /**
     * Logs a message with the `TRACE` level.
     *
     * @param message the message to log
     */
    def trace(message: => String):Unit

  /**
   * Logs a message with the `TRACE` level.
   *
   * @param message the message to log
   * @param error the associated exception
   */
  def trace(message: => String, error: => Throwable):Unit

  /**
   * Logs a message with the `DEBUG` level.
   *
   * @param message the message to log
   */
  def debug(message: => String):Unit


  /**
   * Logs a message with the `DEBUG` level.
   *
   * @param message the message to log
   * @param error the associated exception
   */
  def debug(message: => String, error: => Throwable):Unit

  /**
   * Logs a message with the `INFO` level.
   *
   * @param message the message to log
   */
  def info(message: => String):Unit

  /**
   * Logs a message with the `INFO` level.
   *
   * @param message the message to log
   * @param error the associated exception
   */
  def info(message: => String, error: => Throwable):Unit

  /**
   * Logs a message with the `WARN` level.
   *
   * @param message the message to log
   */
  def warn(message: => String):Unit

  /**
   * Logs a message with the `WARN` level.
   *
   * @param message the message to log
   * @param error the associated exception
   */
  def warn(message: => String, error: => Throwable):Unit

  /**
   * Logs a message with the `ERROR` level.
   *
   * @param message the message to log
   */
  def error(message: => String):Unit

  /**
   * Logs a message with the `ERROR` level.
   *
   * @param message the message to log
   * @param error the associated exception
   */
  def error(message: => String, error: => Throwable):Unit

  /**
   * Enable / disable backend with name {{name}}
   * @param name Name of the backend
   * @param enable True for enable, false for disable
   * @return True if command successful otherwise false
   */
  def backend(name:String, enable:Boolean):Boolean

}
