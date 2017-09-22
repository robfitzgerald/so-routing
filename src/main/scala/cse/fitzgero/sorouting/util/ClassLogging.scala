package cse.fitzgero.sorouting.util

import com.typesafe.scalalogging._

/**
  * Trait based on org.apache.spark.internal.Logging that creates a logger for the class that uses this mixin.
  * is NOT lazy because that would be undesirable for measuring experiment performance.
  * it is different than scala-logger's StrictLogger as it has a @transient annotation
  */
trait ClassLogging {
  // logging can be serialized
  @transient protected val logger: Logger = Logger(this.getClass)
}