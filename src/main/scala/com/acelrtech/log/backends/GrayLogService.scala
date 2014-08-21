package com.acelrtech.log.backends.gl2

import akka.event.Logging
import com.acelrtech.log.models._
import com.acelrtech.log.{LoggingEngine, AppLogger, Logger}
import com.acelrtech.log.models.app.{LogMessage, LogCategory, AppLog}
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

import scala.Error

trait GELFLike {
  def toJson:JsValue
}
case class GELF(version:Double, host:String, short_message:String, full_message:String, level:Int) extends GELFLike {
  override def toJson = Json.obj("version" -> version, "host" -> host, "short_message" -> short_message, "full_message" -> full_message, "level" -> level)
}

trait Graylog2Spec {
  def send(gelf:GELFLike):Either[String, Boolean]
}

class Graylog2Server(val hostname:String, val port:Int) extends Graylog2Spec{
  private[this] var active:Boolean = true
  val address = resolveAddress(hostname)
  def appLogToGELF(applog:AppLog):GELF = {
    GELF(version = 1.0, host = "localhost", short_message = applog.message, full_message = applog.input, level = 1)
  }

  def enableDisable(mode:Boolean) {
    active = mode
  }

  var isEnabled: Boolean = active

  /*
  override def enableDisable(mode:Boolean):Boolean = {
    active = mode
    true
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
  */
  def resolveAddress(address: String): Option[InetAddress] = {
    val uncheckedResult = InetAddress.getByName(hostname)
    if (uncheckedResult.isReachable(250)) {
      Some(uncheckedResult)
    } else {
      None
    }
  }

  def send(gelfDataAsString: String):Either[String, Boolean] = {
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
  }

  def send(gelf: GELFLike):Either[String, Boolean] = {
    val data = Json.stringify(gelf.toJson)
    send(data)
  }
}




