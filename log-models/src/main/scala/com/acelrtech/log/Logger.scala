package com.acelrtech.log

import com.acelrtech.log.models.Log
import com.acelrtech.log.models.app.AppLog

/**
* Logger contract
*
*/
trait Logger {
  /**
   * Enable / disable running logger
   * @param mode True => enable, false => disable
   * @return True for success, false for failure
   */
    def enableDisable(mode:Boolean):Boolean

    /**
     * Enable / disable overall logger for logging
     */
    def isEnabled:Boolean
    /**
     * Enable / disable `INFO` messages
     */
    def isInfoEnabled:Boolean

    /**
    * Enable / disable `DEBUG` messages
     */
    def isDebugEnabled:Boolean

    /**
     * Enable / disable `WARN` messages
     */
    def isWarningEnabled:Boolean

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

/**
 * Logger having application specific details
 * @tparam T AppLog or its subtype
 */
trait AppLogger[T <: AppLog] extends Logger {
  /**
   * Logs a message with the `ERROR` level.
   *
   * @param message the message to log
   */
  def log(message:T):Unit
}