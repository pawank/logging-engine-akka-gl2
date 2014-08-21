package com.acelrtech.log.backends

import com.acelrtech.log.Logger
import com.acelrtech.log.models.app.AppLog
import com.acelrtech.utils.Utils
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
  def log(message:AppLog):Unit
  def send(gelf:GELFLike):Either[String, Boolean]
}

class Graylog2Logger(val hostname:String, val port:Int) extends Logger with Graylog2Spec{
  val address = resolveAddress(hostname)

  def appLogToGELF(applog:AppLog):GELF = {
    GELF(version = 1.0, host = "localhost", short_message = applog.message, full_message = applog.input, level = 1)
  }

  override def log(message:AppLog):Unit = {
    send(appLogToGELF(message))
  }

  override val isEnabled: Boolean = true

  override def warn(message: => String): Unit = send(GELF(version = 1.0, host = "localhost", short_message = message, full_message = "", level = 1))

  override def warn(message: => String, error: => Throwable): Unit = send(GELF(version = 1.0, host = "localhost", short_message = message, full_message = Utils.stackTrace(error), level = 1))

  override def error(message: => String): Unit = send(GELF(version = 1.0, host = "localhost", short_message = message, full_message = "", level = 1))


  override def error(message: => String, error: => Throwable): Unit = send(GELF(version = 1.0, host = "localhost", short_message = message, full_message = Utils.stackTrace(error), level = 1))


  override def backend(name: String, enable: Boolean): Boolean = true

  override def debug(message: => String): Unit = send(GELF(version = 1.0, host = "localhost", short_message = message, full_message = "", level = 1))


  override def debug(message: => String, error: => Throwable): Unit = send(GELF(version = 1.0, host = "localhost", short_message = message, full_message = Utils.stackTrace(error), level = 1))


  override def trace(message: => String): Unit = send(GELF(version = 1.0, host = "localhost", short_message = message, full_message = "", level = 1))


  override def trace(message: => String, error: => Throwable): Unit = send(GELF(version = 1.0, host = "localhost", short_message = message, full_message = Utils.stackTrace(error), level = 1))


  override def info(message: => String): Unit = send(GELF(version = 1.0, host = "localhost", short_message = message, full_message = "", level = 1))


  override def info(message: => String, error: => Throwable): Unit = send(GELF(version = 1.0, host = "localhost", short_message = message, full_message = Utils.stackTrace(error), level = 1))


  override val isInfoEnabled: Boolean = true
  override val isDebugEnabled: Boolean = true
  override val isWarningEnabled: Boolean = true

  def resolveAddress(address: String): Option[InetAddress] = {
    val uncheckedResult = InetAddress.getByName(hostname)
    if (uncheckedResult.isReachable(250)) {
      Some(uncheckedResult)
    } else {
      None
    }
  }

  def send(gelf: GELFLike):Either[String, Boolean] = {
    val checkedAddress = address.get
    val data = Json.stringify(gelf.toJson)
    val gelfBytes = data.getBytes("UTF-8")
    val socket = new DatagramSocket() 
    val packet = new DatagramPacket(gelfBytes, gelfBytes.length, checkedAddress, port)
    try {
      socket.send(packet)
      socket.close()//first try
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
}
