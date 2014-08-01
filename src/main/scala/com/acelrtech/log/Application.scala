package com.acelrtech.log

import akka.actor._
import akka.event.Logging


import com.typesafe.config._

/**
* Logging engine main application creating an actor `LoggingActor` for processing all incoming log events.
*
*/
object LoggingEngine extends App {
  case object Welcome
  case class LogGraylog2(data:String)

  val system = ActorSystem("AcelrTechLabsLoggingSystem")
  val loggingActor = system.actorOf(Props[LoggingActor], name = "LoggingActor")
  /**
  * Show a welcome message
  */
  loggingActor ! Welcome
}



class LoggingActor extends Actor {
  /**
  * Import internal messages
  */
  import LoggingEngine._

  val log = Logging(context.system, this)

  /**
  *
  * Settings for Graylog2 server 
  *
  */
  val config:Config = ConfigFactory.load()
  lazy val appGraylog2IP = config.getString("app.graylog2.host")
  lazy val appGraylog2Post = config.getInt("app.graylog2.port")
  private var logInGraylog2 = false

  private lazy val gl2 = new com.acelrtech.log.backends.Graylog2Client(appGraylog2IP,appGraylog2Post)

  def receive = {
    case Welcome => log.info("\nLoggingActor has started...")
    case com.acelrtech.log.models.Trace(data) => log.info(data)
      if (logInGraylog2) 
        self ! LogGraylog2(data)
    case com.acelrtech.log.models.Info(data) => log.info(data)
      if (logInGraylog2) 
        self ! LogGraylog2(data)
    case com.acelrtech.log.models.Debug(data) => log.debug(data)
      if (logInGraylog2) 
        self ! LogGraylog2(data)
    case com.acelrtech.log.models.Warning(data) => log.warning(data)
      if (logInGraylog2) 
        self ! LogGraylog2(data)
    case com.acelrtech.log.models.Error(data) => log.error(data)
      if (logInGraylog2) 
        self ! LogGraylog2(data)
    case com.acelrtech.log.models.Fatal(data) => log.error(data)
      if (logInGraylog2) 
        self ! LogGraylog2(data)
    case a @ com.acelrtech.log.models.AppLog(_,logtype,_,_) => logtype match {
      case com.acelrtech.log.models.LOGTYPE.TRACE => log.info(a.toString)
        if (logInGraylog2) 
          self ! LogGraylog2(a.toString)
      case com.acelrtech.log.models.LOGTYPE.INFO => log.info(a.toString)
        if (logInGraylog2) 
          self ! LogGraylog2(a.toString)
      case com.acelrtech.log.models.LOGTYPE.DEBUG => log.debug(a.toString)
        if (logInGraylog2) 
          self ! LogGraylog2(a.toString)
      case com.acelrtech.log.models.LOGTYPE.WARNING => log.warning(a.toString)
        if (logInGraylog2) 
          self ! LogGraylog2(a.toString)
      case com.acelrtech.log.models.LOGTYPE.ERROR => log.error(a.toString)
        if (logInGraylog2) 
          self ! LogGraylog2(a.toString)
      case _ =>
    }
    /**
    *
    * Messages to activate / deactivate graylog2 logging 
    */
    case com.acelrtech.log.models.EnableGraylog2 => logInGraylog2 = true
    case com.acelrtech.log.models.DisableGraylog2 => logInGraylog2 = false
    /**
    * Send log as GELF message to graylog2 server
    */
    case LogGraylog2(data) => logInGraylog2(data)
  }

  private def logInGraylog2(data:String):Unit = {
    if (logInGraylog2) {
        gl2 send com.acelrtech.log.backends.GELF(version = 1.0, host = "localhost", short_message = "test", full_message = "Sample test message", level = 1)      
    }
  }
}
