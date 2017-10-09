package cse.fitzgero.sorouting.util

import java.nio.file.{Files, Paths}
import java.time.LocalTime

import com.typesafe.config.ConfigFactory
import org.apache.directory.shared.kerberos.codec.padata.actions.StoreDataType

import scala.collection.concurrent.TrieMap
import scala.util.{Failure, Success, Try}

/**
  * Custom logger designed to mimic standard logging APIs. used for file logging, due to
  * issues configuring log4j/slf4j/logback/scala-logging in this project
  *
  * @param name the unique name (tag) of the logger
  * @param path the directory path. will be used to create a file at $path/$name.log
  */
class AuxLogger private[AuxLogger] (name: String, path: String, private val storedAnalytics: TrieMap[Long, String] = TrieMap()) {

  /**
    * if no "path" is provided, we inspect the application config for a default path
    * @param name the name of this auxiliary logger
    */
  private[AuxLogger] def this (name: String) {
    this(name, ConfigFactory.load().getString("soRouting.application.loggingPath"))
  }

  private[AuxLogger] def copy (
    name: String = name,
    path: String = path,
    storedAnalytics: TrieMap[Long, String] = storedAnalytics
  ): AuxLogger =
    new AuxLogger(name, path, storedAnalytics)

  val filePath: String = s"$path/$name.log"

  /**
    * creates a copy of the current state of the stored logs associated with this logger
    */
  def getLogs: TrieMap[Long, String] = storedAnalytics.clone
  /**
    * posts all stored logs to the logger
    */
  def consoleLogSubmissions(): Unit = storedAnalytics.foreach(tup => println(tup._2))

  def writeLog(): Unit = {
    Try({
      val fileData: String = storedAnalytics.map(tup => s"${tup._1} ${tup._2}").mkString("\n")

      Files.write(Paths.get(filePath), fileData.getBytes)
    }) match {
      case Success(_) =>
      case Failure(e) => println(s"unable to write log to file $filePath: ${e.getMessage}")
    }
  }

  def generateUniqueKey(msg: String): Long =
    (LocalTime.now.hashCode * 10000000000L) + msg.hashCode
  def info(msg: String): Unit = storedAnalytics.update(generateUniqueKey(msg), msg)
  def info(msg: String, value: Double): Unit = storedAnalytics.update(generateUniqueKey(msg+value), f"$msg $value%03f")
  def info(msg: String, t: Throwable): Unit = storedAnalytics.update(generateUniqueKey(msg), s"$msg ${t.getMessage}")
  def info(self: AnyRef, msg: String): Unit = storedAnalytics.update(generateUniqueKey(msg), s"${self.getClass.getCanonicalName} : $msg")
  def info(self: AnyRef, name: String, value: Double): Unit =
    storedAnalytics
      .update(generateUniqueKey(name+value), f"${self.getClass.getCanonicalName} - $name: $value%03f")
}

object AuxLogger {
  private val auxLoggers: TrieMap[String, AuxLogger] = TrieMap()

  def formatAnalyticPost(name: String, value: Double, precision: Int = 3): String =
    if (precision == 1) f"$name $value%01f"
    else if (precision == 2) f"$name $value%02f"
    else if (precision == 3) f"$name $value%03f"
    else if (precision == 4) f"$name $value%04f"
    else if (precision == 5) f"$name $value%05f"
    else if (precision == 6) f"$name $value%06f"
    else {
      f"$name $value"
    }
  private def hasAuxLogger(name: String): Boolean = auxLoggers.isDefinedAt(name)

  /**
    * creates an AuxLogger if it does not yet exist and returns it to the user
    * @param name name for the logger, to be referenced in the program via Logger(name)
    * @param path file path where to place the output file, ie "path/name.log"
    * @return
    */
  def get(name: String, path: Option[String] = None): AuxLogger = synchronized {
    if (!hasAuxLogger(name)) {
      val absolutePath = s"${Paths.get("").toAbsolutePath.toString}/$path"
      val logger =
        path match {
          case Some(providedPath) => new AuxLogger(name, providedPath)
          case None => new AuxLogger(name)
        }
      auxLoggers.update(name, logger)
    }
    auxLoggers(name)
  }

  def setPath(name: String, path: String): Option[AuxLogger] = synchronized {
    if (hasAuxLogger(name)) {
      val updatedLogger: AuxLogger = auxLoggers(name).copy(path = path)
      auxLoggers.update(name, updatedLogger)
      Some(updatedLogger)
    } else None
  }
}


//      // this was a way to declare additional loggers within log4j that have file output.
//      // i was unable to get it to work. maybe because i needed it to be sl
//
//      https://stackoverflow.com/questions/2763740/log4j-log-output-of-a-specific-class-to-a-specific-appender
//
//      val appenderName = s"${name}Appender"
//      System.setProperty(s"log4j.logger.$name", s"${logLevel.toString}, $appenderName")
//      System.setProperty(s"log4j.additivity.$name", "false")
//      System.setProperty(s"log4j.appender.$appenderName", "org.apache.log4j.FileAppender")
//      System.setProperty(s"log4j.appender.$appenderName.File", s"$absolutePath/$name.log")
//      Files.createFile(Paths.get(s"$absolutePath/$name.log"))