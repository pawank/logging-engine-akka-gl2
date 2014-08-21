package com.acelrtech.log

import akka.actor._
import akka.event.Logging
import com.acelrtech.log.backends.gl2.Graylog2LoggerActor


import com.typesafe.config._

/**
* Logging engine main application creating an actor `LoggingActor` for processing all incoming log events.
*
*/
object LoggingEngine extends App {
  case object Welcome

  val system = ActorSystem("AcelrTechLabsLoggingSystem")
  lazy val loggingActor = system.actorOf(Props[LoggingActor], name = "LoggingActor")
  lazy val graylog2Actor = system.actorOf(Props[Graylog2LoggerActor], name = "Graylog2LoggingActor")
  /**
  * Show a welcome message
  */
  loggingActor ! Welcome
  graylog2Actor ! Welcome
}



class LoggingActor extends Actor {
  /**
  * Import internal messages
  */
  import LoggingEngine.Welcome

  val log = Logging(context.system, this)

  /**
  *
  * Settings for Graylog2 server 
  *
  */
  val config:Config = ConfigFactory.load()

  def receive = {
    case Welcome => log.info("\nLoggingActor has started...")
    case com.acelrtech.log.models.Trace(data) => log.info(data)
    case com.acelrtech.log.models.Info(data) => log.info(data)
    case com.acelrtech.log.models.Debug(data) => log.debug(data)
    case com.acelrtech.log.models.Warning(data) => log.warning(data)
    case com.acelrtech.log.models.Error(data) => log.error(data)
    case com.acelrtech.log.models.Fatal(data) => log.error(data)
  }
}
