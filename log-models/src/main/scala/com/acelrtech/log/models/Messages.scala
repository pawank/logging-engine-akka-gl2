package com.acelrtech.log.models

import com.acelrtech.log.models.LOGTYPE.LOGTYPE

/**
* All possible log types to be supported
*
*/
object LOGTYPE extends Enumeration{
  type LOGTYPE = Value
  val TRACE, INFO, DEBUG, WARNING, ERROR, FATAL, UNKNOWN = Value
}

trait Log
case class Trace(log:String) extends Log
case class Info(log:String) extends Log
case class Debug(log:String) extends Log
case class Error(log:String) extends Log
case class Warning(log:String) extends Log
case class Fatal(log:String) extends Log
case class Unknown(log:String) extends Log

case object Enable
case object Disable
