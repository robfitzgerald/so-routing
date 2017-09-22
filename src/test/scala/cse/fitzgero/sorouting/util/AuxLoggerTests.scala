package cse.fitzgero.sorouting.util

import java.nio.file.{Files, Paths}
import scala.collection.JavaConverters._

import cse.fitzgero.sorouting.FileWriteSideEffectTestTemplate

class AuxLoggerTests extends FileWriteSideEffectTestTemplate("AuxLogger") {
  "createAuxLogger" when {
    "called with a new aux logger name and a file output path" should {
      "write log values to a file" in {
        val testLogName = "writeToAFile"
        val testMessage = "this is a test"
        val expectedFilePath = s"$testRootPath/$testLogName.log"
        val logger = AuxLogger.get(testLogName, Some(testRootPath))
        logger.info(testMessage)
        logger.writeLog()
        val fileData = Files.readAllLines(Paths.get(expectedFilePath)).asScala
        fileData.foreach(println)
      }
    }
    "calling the same logger from multiple threads" should {
      "have the correct number of entries" in {
        val testLogName = "concurrent"
        val expectedFilePath = s"$testRootPath/$testLogName.log"
        val sumOf1To100 = (1 to 100).sum
        (1 to 100)
          .par
          .foreach(n => {
            val logger = AuxLogger.get(testLogName, Some(testRootPath))
            logger.info(s"$n")
          })
        val logger = AuxLogger.get(testLogName, Some(testRootPath))
        logger.writeLog()
        val fileData = Files.readAllLines(Paths.get(s"$expectedFilePath")).asScala
        fileData.map(_.split(" ").last.toInt).sum should equal (sumOf1To100)
      }
    }
    "calling setPath after a logger has been invoked" should {
      "update the file path destination for the logger and still have all of it's logs" in {
        val testLogName = "setPathCalled"
        val expectedFilePath = s"$testRootPath/$testLogName.log"
        val sumOf1To10 = 55
        (1 to 10)
          .par
          .foreach(n => {
            val logger = AuxLogger.get(testLogName, Some("incorrect path"))
            logger.info(s"$n")
          })
        AuxLogger.setPath(testLogName, testRootPath)
        val logger = AuxLogger.get(testLogName)
        logger.filePath should equal (expectedFilePath)
        logger.writeLog()
        logger.getLogs.values.map(_.toInt).sum should equal (sumOf1To10)
      }
    }
  }
}
