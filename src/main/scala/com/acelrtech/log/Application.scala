package com.acelrtech.log

import akka.actor.{Props, ActorSystem, Actor, ActorSelection}
import akka.event.Logging
import com.acelrtech.log.backends.gl2.{Graylog2Constants, GELF}
import com.acelrtech.log.models.app.LogCategory


import com.typesafe.config._
import play.api.libs.json.Json

/**
* Logging engine main application creating an actor `LoggingActor` for processing all incoming log events.
*
*/
object LoggingEngine extends App {
  case object Welcome

  //val system = ActorSystem("AcelrTechLabsLoggingSystem")
  //lazy val loggingActor = system.actorOf(Props[LoggingActor], name = "LoggingActor")
  //lazy val loggingActor = system.actorOf(Props[GL2LoggerActor], name = "LoggingActor")
  val gl2system = ActorSystem("AcelrTechLabsGraylog2LoggingSystem")
  lazy val graylog2Actor = gl2system.actorOf(Props[GL2LoggerActor], name = "Graylog2LoggingActor")
  /**
  * Show a welcome message
  */
  //loggingActor ! Welcome
  graylog2Actor ! Welcome
}



class LoggingActor extends Actor {
  private[this] var isEnabled = true
  /**
  * Import internal messages
  */
  import LoggingEngine.Welcome
  val log = Logging(context.system, this)
  def receive = {
    case Welcome => log.info("\nLoggingActor has started...")

    /**
     *
     * Messages to activate / deactivate graylog2 logging
     */
    case com.acelrtech.log.models.Enable => isEnabled = true
    case com.acelrtech.log.models.Disable => isEnabled = false
    case com.acelrtech.log.models.Trace(data) => if (isEnabled) log.info(data)
    case com.acelrtech.log.models.Info(data) => if (isEnabled) log.info(data)
    case com.acelrtech.log.models.Debug(data) => if (isEnabled) log.debug(data)
    case com.acelrtech.log.models.Warning(data) => if (isEnabled) log.warning(data)
    case com.acelrtech.log.models.Error(data) => if (isEnabled) log.error(data)
    case com.acelrtech.log.models.Fatal(data) => if (isEnabled) log.error(data)
  }
}



class GL2LoggerActor extends Actor {
  case class LogGraylog2(data:String, level:Int)

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

  private val config:Config = ConfigFactory.load()
  private val appGraylog2IP = config.getString("app.graylog2.host")
  private val appGraylog2Post = config.getInt("app.graylog2.port")

  private val gl2 = new com.acelrtech.log.backends.gl2.Graylog2Server(appGraylog2IP,appGraylog2Post)

  def receive = {

   case Welcome =>
     log.info("\nLoggingActor has started...")
     //println(s"\nLoggingActor has started...")

    case com.acelrtech.log.models.Enable => gl2.enableDisable(true)
    case com.acelrtech.log.models.Disable => gl2.enableDisable(false)

    case com.acelrtech.log.models.Trace(data) =>
      self ! LogGraylog2(data,1)
    case com.acelrtech.log.models.Info(data) =>
      self ! LogGraylog2(data,1)
    case com.acelrtech.log.models.Debug(data) =>
      self ! LogGraylog2(data,2)
    case com.acelrtech.log.models.Warning(data) =>
      self ! LogGraylog2(data,3)
    case com.acelrtech.log.models.Error(data) =>
      self ! LogGraylog2(data,4)
    case com.acelrtech.log.models.Fatal(data) =>
      self ! LogGraylog2(data,5)

    case a @ com.acelrtech.log.models.app.LogMessage(host,logtype,msg,detail) => logtype match {
      case com.acelrtech.log.models.LOGTYPE.TRACE =>
        val more:String = detail.getOrElse("")
        gl2.send(GELF(version = Graylog2Constants.VERSION, host = host, short_message = msg, full_message = more, level = 1))
      case com.acelrtech.log.models.LOGTYPE.INFO =>
        val more:String = detail.getOrElse("")
        gl2.send(GELF(version = Graylog2Constants.VERSION, host = host, short_message = msg, full_message = more, level = 1))
      case com.acelrtech.log.models.LOGTYPE.DEBUG =>
        val more:String = detail.getOrElse("")
        gl2.send(GELF(version = Graylog2Constants.VERSION, host = host, short_message = msg, full_message = more, level = 0))
      case com.acelrtech.log.models.LOGTYPE.WARNING =>
        val more:String = detail.getOrElse("")
        gl2.send(GELF(version = Graylog2Constants.VERSION, host = host, short_message = msg, full_message = more, level = 2))
      case com.acelrtech.log.models.LOGTYPE.ERROR =>
        val more:String = detail.getOrElse("")
        gl2.send(GELF(version = Graylog2Constants.VERSION, host = host, short_message = msg, full_message = more, level = 3))
      case com.acelrtech.log.models.LOGTYPE.FATAL =>
        val more:String = detail.getOrElse("")
        gl2.send(GELF(version = Graylog2Constants.VERSION, host = host, short_message = msg, full_message = more, level = 4))
      case com.acelrtech.log.models.LOGTYPE.UNKNOWN =>
        val more:String = detail.getOrElse("")
        gl2.send(GELF(version = Graylog2Constants.VERSION, host = host, short_message = msg, full_message = more, level = 5))
    }

    case a:com.acelrtech.log.models.app.AppLog =>
      val level = a.logCategory match {
        case LogCategory.INFO => 1
        case LogCategory.TRACE => 1
        case LogCategory.DEBUG => 0
        case LogCategory.WARN => 2
        case LogCategory.ERROR => 3
        case LogCategory.FATAL => 4
        case LogCategory.UNKNOWN => 5
      }
      val id:String = a.id.getOrElse("")
      val stack:String = a.stackTrace.getOrElse("")
      val output:String = a.output.getOrElse("")
      val data = Json.stringify(Json.obj("version" -> Graylog2Constants.VERSION, "host" -> a.host, "short_message" -> a.message, "full_message" -> stack,
        "level" -> level, "_id" -> id, "_entity" -> a.entity, "_module" -> a.module, "_input" -> a.input,
        "_output" -> output, "_target" -> a.target, "_function" -> a.calledFunction))
      log.info(s"GELF:$data")
      gl2.send(data)

    case LogGraylog2(data,level) => logInGraylog2(data,level)

  }

  private def logInGraylog2(data:String, level:Int) = {
    log.info(s"DATA - $data for level - $level")
    log.debug("Graylog2 server enabled?: " + gl2.isEnabled)
    if (gl2.isEnabled) {
      gl2.send(com.acelrtech.log.backends.gl2.GELF(version = Graylog2Constants.VERSION, host = "localhost", short_message = data, full_message = "test", level = level))
    }
  }

}

