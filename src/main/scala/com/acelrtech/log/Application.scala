package com.acelrtech.log

import akka.actor._
import akka.event.Logging


import com.typesafe.config._

object LoggingEngine extends App {
  val system = ActorSystem("AcelrTechLabsLoggingSystem")
  val loggingActor = system.actorOf(Props[LoggingActor], name = "LoggingActor")
  loggingActor ! Welcome
}

case object Welcome


class LoggingActor extends Actor {
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
    case com.acelrtech.log.models.Info(data) => log.info(data)
    case com.acelrtech.log.models.Debug(data) => log.debug(data)
    case com.acelrtech.log.models.Warning(data) => log.warning(data)
    case com.acelrtech.log.models.Error(data) => log.error(data)
    case com.acelrtech.log.models.Fatal(data) => log.error(data)
    case a @ com.acelrtech.log.models.AppLog(_,logtype,_,_) => logtype match {
      case com.acelrtech.log.models.LOGTYPE.TRACE => log.info(a.toString)
      case com.acelrtech.log.models.LOGTYPE.INFO => log.info(a.toString)
      case com.acelrtech.log.models.LOGTYPE.DEBUG => log.debug(a.toString)
      case com.acelrtech.log.models.LOGTYPE.WARNING => log.warning(a.toString)
      case com.acelrtech.log.models.LOGTYPE.ERROR => log.error(a.toString)
      case _ =>
    }
    case com.acelrtech.log.models.EnableGraylog2 => logInGraylog2 = true
    case com.acelrtech.log.models.DisableGraylog2 => logInGraylog2 = false
  }

  private def logInGraylog2(data:String):Unit = {
    if (logInGraylog2) {
       
    }
  }
}
