package com.acelrtech.log.models.app

import com.acelrtech.log.models.LOGTYPE.LOGTYPE
import com.acelrtech.log.models.Log
import com.acelrtech.log.models.app.LogCategory.LogCategory
import org.joda.time.DateTime

trait LogRequest{
  def ip: String
  def path: String
  def status: String
  def action: String
  def queryString: Option[String]
  def session:Option[String]
}

case class PlayLogRequest(ip: String, path:String, status: String, action: String, queryString : Option[String], session:Option[String]) extends LogRequest

object LogCategory extends Enumeration{
  type LogCategory = Value
  val DEBUG,INFO,TRACE,WARN,ERROR = Value
}

trait LogType{
  def value: String
}

case class AppLogType(value: String = "models.logging.AppLog") extends LogType

case class AppLogWithRequestType(value: String = "models.logging.AppLogWithRequest") extends LogType

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
case class LogMessage(module:String, logType:LOGTYPE, message:String, detail:Option[String]) extends Log {
  override def toString = s"""$logType $module $message $detail.getOrElse("")"""
}

/**
 * Application specific `Log` with tracking identifier denoted by `id`
 */
trait AppLog extends Log {
  def id: Option[String]
  def entity: String
  def logCategory: LogCategory
  def module: String
  def message: String
  def stackTrace: Option[String]
  def input: String
  def output: Option[String]
  def target: String
  def calledFunction: String
  def host:String
  /**
   * To be used for converting into JSON structure to send it to Graylog2 or other logging server who needs JSON input
   * @return
   */
  def _t: LogType
  def createdBy: Option[Long]
  def createdOn: DateTime
}

case class SimpleLog(id:Option[String] = None, entity: String, logCategory: LogCategory = LogCategory.DEBUG,  module: String,
                     message: String, stackTrace: Option[String] = None, input: String, output: Option[String] = None,
                     target: String, calledFunction: String, host:String, _t: LogType = AppLogType(),
                     createdBy: Option[Long] = None, createdOn: DateTime = DateTime.now) extends AppLog


case class FullLog(id:Option[String] = None, entity: String, logCategory: LogCategory = LogCategory.DEBUG,  module: String,
                   message: String, stackTrace: Option[String] = None, input: String, output: Option[String] = None,
                   target: String, calledFunction: String, host:String,_t: LogType = AppLogType(),
                   createdBy: Option[Long] = None, createdOn: DateTime = DateTime.now, request: LogRequest) extends AppLog



case object EnableGraylog2
case object DisableGraylog2
