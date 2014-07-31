package com.acelrtech.log.backends

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


class Graylog2Client(val hostname:String, val port:Int) extends Graylog2Spec{
  val address = resolveAddress(hostname)
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
