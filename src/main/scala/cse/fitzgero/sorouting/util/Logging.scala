package cse.fitzgero.sorouting.util

import com.typesafe.scalalogging.Logger

/**
  * Trait based on org.apache.spark.internal.Logging that
  * creates a logger for the class that uses this mixin
  * is NOT lazy because that would be undesirable for measuring experiment performance
  */
trait Logging {
  // logging can be serialized
  @transient protected val logger: Logger = Logger(this.getClass)
}