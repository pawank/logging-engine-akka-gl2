package com.acelrtech.log.backends.gl2

import akka.event.Logging
import com.acelrtech.log.models.LogBackends
import com.acelrtech.log.{LoggingEngine, AppLogger, Logger}
import com.acelrtech.log.models.app.{LogCategory, AppLog}
import com.acelrtech.utils.Utils
import com.typesafe.config.{ConfigFactory, Config}
import play.api.libs.json.Json
import play.api.libs.json._
import akka.actor._
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.lang.IllegalArgumentException
import java.io._ 
import java.net._

trait GELFLike {
  def toJson:JsValue
}
case class GELF(version:Double, host:String, short_message:String, full_message:String, level:Int) extends GELFLike {
  override def toJson = Json.obj("version" -> version, "host" -> host, "short_message" -> short_message, "full_message" -> full_message, "level" -> level)
}

trait Graylog2Spec {
  def send(gelf:GELFLike):Either[String, Boolean]
}

class Graylog2Logger(val hostname:String, val port:Int) extends AppLogger[AppLog] with Graylog2Spec{
  private[this] var active:Boolean = true
  val address = resolveAddress(hostname)

  override def enableDisable(mode:Boolean):Boolean = {
    active = mode
    true
  }

  def appLogToGELF(applog:AppLog):GELF = {
    GELF(version = 1.0, host = "localhost", short_message = applog.message, full_message = applog.input, level = 1)
  }

  override def log(message:AppLog):Unit = {
    send(appLogToGELF(message))
  }

  override val isEnabled: Boolean = active

  override def warn(message: => String): Unit = send(GELF(version = 1.0, host = "localhost", short_message = message, full_message = "", level = 1))

  override def warn(message: => String, error: => Throwable): Unit = send(GELF(version = 1.0, host = "localhost", short_message = message, full_message = Utils.stackTrace(error), level = 1))

  override def error(message: => String): Unit = send(GELF(version = 1.0, host = "localhost", short_message = message, full_message = "", level = 1))


  override def error(message: => String, error: => Throwable): Unit = send(GELF(version = 1.0, host = "localhost", short_message = message, full_message = Utils.stackTrace(error), level = 1))

  override def debug(message: => String): Unit = send(GELF(version = 1.0, host = "localhost", short_message = message, full_message = "", level = 1))


  override def debug(message: => String, error: => Throwable): Unit = send(GELF(version = 1.0, host = "localhost", short_message = message, full_message = Utils.stackTrace(error), level = 1))


  override def trace(message: => String): Unit = send(GELF(version = 1.0, host = "localhost", short_message = message, full_message = "", level = 1))


  override def trace(message: => String, error: => Throwable): Unit = send(GELF(version = 1.0, host = "localhost", short_message = message, full_message = Utils.stackTrace(error), level = 1))


  override def info(message: => String): Unit = send(GELF(version = 1.0, host = "localhost", short_message = message, full_message = "", level = 1))


  override def info(message: => String, error: => Throwable): Unit = send(GELF(version = 1.0, host = "localhost", short_message = message, full_message = Utils.stackTrace(error), level = 1))


  override def isInfoEnabled: Boolean = isEnabled
  override def isDebugEnabled: Boolean = isEnabled
  override def isWarningEnabled: Boolean = isEnabled

  override def backend(name:String, enable:Boolean):Boolean = {
    //println(s"Setting - $enable for $name")
    LogBackends.withName(name) match {
      case LogBackends.GRAYLOG2 =>
        enableDisable(enable)
      case _ => false
    }
  }

  def resolveAddress(address: String): Option[InetAddress] = {
    val uncheckedResult = InetAddress.getByName(hostname)
    if (uncheckedResult.isReachable(250)) {
      Some(uncheckedResult)
    } else {
      None
    }
  }
  def send(gelfDataAsString: String):Either[String, Boolean] = {
    if (isEnabled) {
      val checkedAddress = address.get
      val gelfBytes = gelfDataAsString.getBytes("UTF-8")
      val socket = new DatagramSocket()
      val packet = new DatagramPacket(gelfBytes, gelfBytes.length, checkedAddress, port)
      try {
        socket.send(packet)
        socket.close() //first try
        Right(true)
      } catch {
        case _: Throwable => {
          try {
            socket.send(packet)
            socket.close()
            Right(true)
          }
          catch {
            case _: IllegalArgumentException =>
              socket.close()
              Left("Cannot send the log entry to Graylog2 server even in second try")
            case _: Throwable =>
              socket.close()
              Left("Cannot send the log entry to Graylog2 server even in second try")
          }
        }
      }
    } else {
      Left("Graylog2 server is disabled")
    }
  }

  def send(gelf: GELFLike):Either[String, Boolean] = {
    val data = Json.stringify(gelf.toJson)
    send(data)
  }
}

case class LogGraylog2(data:String, level:Int)

class Graylog2LoggerActor extends Actor {
  /**
   * Import internal messages
   */
  import LoggingEngine.Welcome

  //val log = Logging(context.system, this)

  /**
   *
   * Settings for Graylog2 server
   *
   */
  val config:Config = ConfigFactory.load()
  lazy val appGraylog2IP = config.getString("app.graylog2.host")
  lazy val appGraylog2Post = config.getInt("app.graylog2.port")

  private lazy val gl2 = new com.acelrtech.log.backends.gl2.Graylog2Logger(appGraylog2IP,appGraylog2Post)

  def receive = {
    case Welcome => self ! com.acelrtech.log.models.Info("\nLoggingActor has started...")
    /**
     *
     * Messages to activate / deactivate graylog2 logging
     */
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
    case a @ com.acelrtech.log.models.app.LogMessage(m,logtype,msg,detail) => logtype match {
      case com.acelrtech.log.models.LOGTYPE.TRACE =>
          self ! gl2.send(GELF(version = 1.0, host = "", short_message = msg, full_message = detail.getOrElse(""), level = 1))
      case com.acelrtech.log.models.LOGTYPE.INFO =>
        self ! gl2.send(GELF(version = 1.0, host = "", short_message = msg, full_message = detail.getOrElse(""), level = 1))
      case com.acelrtech.log.models.LOGTYPE.DEBUG =>
        self ! gl2.send(GELF(version = 1.0, host = "", short_message = msg, full_message = detail.getOrElse(""), level = 2))
      case com.acelrtech.log.models.LOGTYPE.WARNING =>
        self ! gl2.send(GELF(version = 1.0, host = "", short_message = msg, full_message = detail.getOrElse(""), level = 3))
      case com.acelrtech.log.models.LOGTYPE.ERROR =>
        self ! gl2.send(GELF(version = 1.0, host = "", short_message = msg, full_message = detail.getOrElse(""), level = 4))
      case a:com.acelrtech.log.models.app.AppLog =>
        val level = a.logCategory match {
          case LogCategory.INFO => 1
          case LogCategory.TRACE => 1
          case LogCategory.DEBUG => 2
          case LogCategory.WARN => 3
          case LogCategory.ERROR => 4
        }
        val id:String = a.id.getOrElse("")
        val stack:String = a.stackTrace.getOrElse("")
        val output:String = a.output.getOrElse("")
        gl2.send(Json.stringify(Json.obj("version" -> 1.0, "host" -> a.host, "short_message" -> a.message, "full_message" -> stack,
          "level" -> level, "_id" -> id, "_entity" -> a.entity, "_module" -> a.module, "_input" -> a.input,
          "_output" -> output, "_target" -> a.target, "_function" -> a.calledFunction)))
      case _ =>
        sender ! "Invalid message received"
    }
    /**
     * Send log as GELF message to graylog2 server
     */
    case LogGraylog2(data,level) => logInGraylog2(data,level)
  }

  private def logInGraylog2(data:String, level:Int):Unit = {
    if (gl2.isEnabled) {
      gl2 send com.acelrtech.log.backends.gl2.GELF(version = 1.0, host = "", short_message = data, full_message = "", level = level)
    }
  }
}

class Graylog2Logging(client:ActorSelection, config:Config, hostname:String, port:Int) extends Graylog2Logger(hostname,port) {

}

object Graylog2Logger {
  /**
   * Load the configuration object based on `com.typesafe.config.Config`
   *
   */
  def apply(actorRef:ActorSelection, config:Config):Logger = {
    val appGraylog2IP = config.getString("app.graylog2.host")
    val appGraylog2Post = config.getInt("app.graylog2.port")
    println(s"IP:$appGraylog2IP and port:$appGraylog2Post")
    new Graylog2Logging(actorRef,config, appGraylog2IP, appGraylog2Post)
  }

  /**
   * Create an underlying Logger client through which log events will be delegated to target logging system
   *
   */
  def apply(actorRef:ActorSelection):Logger = {
    val config:Config = ConfigFactory.load()
    val appGraylog2IP = config.getString("app.graylog2.host")
    val appGraylog2Post = config.getInt("app.graylog2.port")
    println(s"IP:$appGraylog2IP and port:$appGraylog2Post")
    new Graylog2Logging(actorRef,config, appGraylog2IP, appGraylog2Post)
  }
}
