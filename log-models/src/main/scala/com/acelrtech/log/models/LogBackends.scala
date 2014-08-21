package com.acelrtech.log.models

/**
 * All supported logging backends
 */
object LogBackends extends Enumeration{
  type LogBackend = Value
  val AKKA_ACTORS, GRAYLOG2, LOG4J, MONGODB = Value
}
