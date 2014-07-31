package com.acelrtech.log.models

import com.acelrtech.log.models.LOGTYPE.LOGTYPE

/**
* All possible log types to be supported
*
*/
object LOGTYPE extends Enumeration{
  type LOGTYPE = Value
  val TRACE, INFO, DEBUG, WARNING, ERROR, FATAL = Value
}

trait Log
case class Trace(log:String) extends Log
case class Info(log:String) extends Log
case class Debug(log:String) extends Log
case class Error(log:String) extends Log
case class Warning(log:String) extends Log
case class Fatal(log:String) extends Log
/**
*
* Application specific log message to be sent by logging client 
*
* @param module Name of the application module / component
* @param logType Any one out of {{{com.acelrtech.log.models.LOGTYPE.LOGTYPE}}}
* @param message Title of the log entry
* @param detail Optional value with more information about the log entrye
*
*/
case class AppLog(module:String, logType:LOGTYPE, message:String, detail:Option[Any]) extends Log {
  override def toString = s"""$logType $module $message $detail.getOrElse("")"""
}


case object EnableGraylog2
case object DisableGraylog2
