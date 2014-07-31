package com.acelrtech.log

import com.typesafe.config._
import akka.actor._

import com.acelrtech.log.models._
import com.acelrtech.utils.Utils

/**
* Logger which takes target logging system `ActorRef` to communicate with
*
* @param client ActorRef of the target logging platform
* @param config Configuration to enable / disable logging
*
*/
class Logger(val client:ActorSelection, config:Config) {
    /**
     * Check whether to send any log level information to actor system or not
     *
     * {{{app.log.enabled=1}}} enables logging through it
     */
    val isEnabled = config.getInt("app.log.enabled") == 1


    /**
     * Check whether to send only `TRACE` or `INFO` log information to actor system or not
     *
     * {{{app.log.info.enabled=1}}} enables logging through it
     */
    lazy val isInfoEnabled = isEnabled || config.getInt("app.log.info.enabled") == 1

    /**
     * Check whether to send only `DEBUG` log information to actor system or not
     *
     * {{{app.log.debug.enabled=1}}} enables logging through it
     */
    lazy val isDebugEnabled = isEnabled || config.getInt("app.log.debug.enabled")  == 1

    /**
     * Check whether to send only `WARN` log information to actor system or not
     *
     * {{{app.log.warning.enabled=1}}} enables logging through it
     */
    lazy val isWarningEnabled = isEnabled || config.getInt("app.log.warning.enabled") == 1

    /**
     * Logs a message with the `TRACE` level.
     *
     * @param message the message to log
     */
    def trace(message: => String) {
      if (isInfoEnabled) client ! Trace(message)
    }

  /**
   * Logs a message with the `TRACE` level.
   *
   * @param message the message to log
   * @param error the associated exception
   */
  def trace(message: => String, error: => Throwable) {
    if (isInfoEnabled) client ! AppLog("", LOGTYPE.TRACE, message, Some(Utils.stackTrace(error)))
  }

  /**
   * Logs a message with the `DEBUG` level.
   *
   * @param message the message to log
   */
  def debug(message: => String) {
    if (isDebugEnabled) client ! Debug(message)
  }


  /**
   * Logs a message with the `DEBUG` level.
   *
   * @param message the message to log
   * @param error the associated exception
   */
  def debug(message: => String, error: => Throwable) {
    if (isDebugEnabled) client ! AppLog("", LOGTYPE.DEBUG, message, Some(Utils.stackTrace(error)))
  }

  /**
   * Logs a message with the `INFO` level.
   *
   * @param message the message to log
   */
  def info(message: => String) {
    if (isInfoEnabled) client ! Info(message)
  }

  /**
   * Logs a message with the `INFO` level.
   *
   * @param message the message to log
   * @param error the associated exception
   */
  def info(message: => String, error: => Throwable) {
    if (isInfoEnabled) client ! AppLog("", LOGTYPE.INFO, message, Some(Utils.stackTrace(error)))
  }

  /**
   * Logs a message with the `WARN` level.
   *
   * @param message the message to log
   */
  def warn(message: => String) {
    if (isWarningEnabled) client ! Warning(message)
  }

  /**
   * Logs a message with the `WARN` level.
   *
   * @param message the message to log
   * @param error the associated exception
   */
  def warn(message: => String, error: => Throwable) {
    if (isWarningEnabled) client ! AppLog("", LOGTYPE.WARNING, message, Some(Utils.stackTrace(error)))
  }

  /**
   * Logs a message with the `ERROR` level.
   *
   * @param message the message to log
   */
  def error(message: => String) {
    if (isEnabled) client ! Error(message)
  }

  /**
   * Logs a message with the `ERROR` level.
   *
   * @param message the message to log
   * @param error the associated exception
   */
  def error(message: => String, error: => Throwable) {
    if (isEnabled) client ! AppLog("", LOGTYPE.ERROR, message, Some(Utils.stackTrace(error)))
  }


}

/**
 * API for logging operations through the wrapper around messages to be sent to remote logging actor(s).
 *
 * For example, 
 * {{{
 * Logger.info("Test message!")
 * }}}
 *
 * or
 *
 * {{{import com.acelrtech.log.Logger._}}}
 * {{{
 * debug("Test message!")
 * }}}
 *
 */
object Logger {
  /**
   * Load the configuration object based on `com.typesafe.config.Config`
   *
   */
  def apply(actorRef:ActorSelection, config:Config):Logger = new Logger(actorRef,config)

  /**
  * Create an underlying Logger client through which log events will be delegated to target logging system
  *
  */
  def apply(actorRef:ActorSelection):Logger = {
    val config:Config = ConfigFactory.load()
      new Logger(actorRef,config)
  }
}
