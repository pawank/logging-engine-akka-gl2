package com.acelrtech.log.backends.gl2

import akka.actor.{Actor,ActorSelection}
import com.acelrtech.log.Logger

import com.acelrtech.log.models._
import com.acelrtech.utils.Utils
import com.typesafe.config._
import com.acelrtech.log.models.app.{AppLog, LogMessage}

/**
 * Logger which takes target logging system `ActorRef` to communicate with
 *
 * @param client ActorRef of the target logging platform
 * @param config Configuration to enable / disable logging
 *
 */
class Graylog2Logger(client:ActorSelection, config:Config) extends com.acelrtech.log.AppLogger{
  private[this] var active:Boolean = true
  override def enableDisable(mode:Boolean):Boolean = {
    active = mode
    true
  }

  /**
   * Check whether to send any log level information to actor system or not
   *
   * {{{app.log.enabled=1}}} enables logging through it
   */
  override def isEnabled = active || config.getInt("app.log.enabled") == 1
  /**
   * Check whether to send only `TRACE` or `INFO` log information to actor system or not
   *
   * {{{app.log.info.enabled=1}}} enables logging through it
   */
  override def isInfoEnabled = isEnabled || config.getInt("app.log.info.enabled") == 1

  /**
   * Check whether to send only `DEBUG` log information to actor system or not
   *
   * {{{app.log.debug.enabled=1}}} enables logging through it
   */
  override def isDebugEnabled = isEnabled || config.getInt("app.log.debug.enabled")  == 1

  /**
   * Check whether to send only `WARN` log information to actor system or not
   *
   * {{{app.log.warning.enabled=1}}} enables logging through it
   */
  override def isWarningEnabled = isEnabled || config.getInt("app.log.warning.enabled") == 1

  /**
   * Logs a message with the `TRACE` level.
   *
   * @param message the message to log
   */
  override def trace(message: => String) {
    if (isInfoEnabled) client ! Trace(message)
  }

  /**
   * Logs a message with the `TRACE` level.
   *
   * @param message the message to log
   * @param error the associated exception
   */
  override def trace(message: => String, error: => Throwable) {
    if (isInfoEnabled) client ! LogMessage("", LOGTYPE.TRACE, message, Some(Utils.stackTrace(error)))
  }

  /**
   * Logs a message with the `DEBUG` level.
   *
   * @param message the message to log
   */
  override def debug(message: => String) {
    if (isDebugEnabled) client ! Debug(message)
  }


  /**
   * Logs a message with the `DEBUG` level.
   *
   * @param message the message to log
   * @param error the associated exception
   */
  override def debug(message: => String, error: => Throwable) {
    if (isDebugEnabled) client ! LogMessage("", LOGTYPE.DEBUG, message, Some(Utils.stackTrace(error)))
  }

  /**
   * Logs a message with the `INFO` level.
   *
   * @param message the message to log
   */
  override def info(message: => String) {
    println(s"INFO message to server as $message")
    if (isInfoEnabled) client ! Info(message)
  }

  /**
   * Logs a message with the `INFO` level.
   *
   * @param message the message to log
   * @param error the associated exception
   */
  override def info(message: => String, error: => Throwable) {
    if (isInfoEnabled) client ! LogMessage("", LOGTYPE.INFO, message, Some(Utils.stackTrace(error)))
  }

  /**
   * Logs a message with the `WARN` level.
   *
   * @param message the message to log
   */
  override def warn(message: => String) {
    if (isWarningEnabled) client ! Warning(message)
  }

  /**
   * Logs a message with the `WARN` level.
   *
   * @param message the message to log
   * @param error the associated exception
   */
  override def warn(message: => String, error: => Throwable) {
    if (isWarningEnabled) client ! LogMessage("", LOGTYPE.WARNING, message, Some(Utils.stackTrace(error)))
  }

  /**
   * Logs a message with the `ERROR` level.
   *
   * @param message the message to log
   */
  override def error(message: => String) = {
    if (isEnabled) client ! Error(message)
  }

  /**
   * Logs a message with the `ERROR` level.
   *
   * @param message the message to log
   * @param error the associated exception
   */
  override def error(message: => String, error: => Throwable)  {
    if (isEnabled) client ! LogMessage("", LOGTYPE.ERROR, message, Some(Utils.stackTrace(error)))
  }

  /**
   * Logs a message with the `UNKNOWN` level.
   *
   * @param message the message to log
   */
  override def unknown(message: => String) = {
    if (isEnabled) client ! Unknown(message)
  }

  /**
   * Logs a message with the `UNKNOWN` level.
   *
   * @param message the message to log
   * @param error the associated exception
   */
  override def unknown(message: => String, error: => Throwable)  {
    if (isEnabled) client ! LogMessage("", LOGTYPE.UNKNOWN, message, Some(Utils.stackTrace(error)))
  }

  /**
   * Enable / disable backend with name {{name}}
   * @param name Name of the backend
   * @param enable True for enable, false for disable
   */
  override def backend(name:String, enable:Boolean):Boolean = {
    //println(s"Setting - $enable for $name")
    LogBackends.withName(name) match {
      case LogBackends.GRAYLOG2 =>
        enableDisable(enable)
      case _ => false
    }
  }

  def log(message: AppLog): Unit = {
    if (isEnabled) client ! message
  }
}

object Graylog2Logger {
  /**
   * Load the configuration object based on `com.typesafe.config.Config`
   *
   */
  def apply(actorRef:ActorSelection, config:Config):com.acelrtech.log.AppLogger = {
    //val appGraylog2IP = config.getString("app.graylog2.host")
    //val appGraylog2Post = config.getInt("app.graylog2.port")
    //println(s"IP:$appGraylog2IP and port:$appGraylog2Post")
    new Graylog2Logger(actorRef,config)
  }

  /**
   * Create an underlying Logger client through which log events will be delegated to target logging system
   *
   */
  def apply(actorRef:ActorSelection):com.acelrtech.log.AppLogger = {
    val config:Config = ConfigFactory.load()
    new Graylog2Logger(actorRef,config)
  }
}