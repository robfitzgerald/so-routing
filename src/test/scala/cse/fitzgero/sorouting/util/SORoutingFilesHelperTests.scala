package cse.fitzgero.sorouting.util

import java.io.{File, FileNotFoundException}
import java.nio.file.{Files, Paths}
import java.time.LocalTime

import cse.fitzgero.sorouting.FileWriteSideEffectTestTemplate

class SORoutingFilesHelperTests extends FileWriteSideEffectTestTemplate("SORoutingFilesHelperTests"){
  val testRoot: String = testRootPath
  "SORoutingFilesHelper" when {
    trait Config {
      def goodConfig: SORoutingApplicationConfig = makeConfig("config.xml")
      def badConfig: SORoutingApplicationConfig = makeConfig("fileDoesNotExist")
      private def makeConfig(configFile: String) =
        SORoutingApplicationConfig(
          s"src/test/resources/SORoutingFilesHelperTests/$configFile",
          s"src/test/resources/SORoutingFilesHelperTests/network.xml",
          testRootPath,
          "*",
          "5",
          1000,
          100,
          "08:00",
          "10:00"
        )
    }
    "constructed" should {
      "throw an error when the file with the config file path is not found" in new Config {
        val thrown = the [FileNotFoundException] thrownBy SORoutingFilesHelper(badConfig)
        thrown getMessage() should equal ("src/test/resources/SORoutingFilesHelperTests/fileDoesNotExist (No such file or directory)")
      }
    }
    // @TODO these tests are now invalid since filesHelper has constructor side-effects. future version should pull those out into a test-able method.
//    "confirmWhatAlreadyExists" should {
//      "remove nothing from the scaffolding list when nothing has been made before" in new Config {
//        val result: Set[String] = SORoutingFilesHelper(goodConfig).assetsToConstruct
//        // all values in the result should be the same as the scaffolding
//        SORoutingFilesHelper.scaffolding.zip(result).map(tup => tup._1 == tup._2).reduce(_&&_) should equal (true)
//      }
//      "remove a file that was found to already exist" in new Config {
//        val filesHelper = SORoutingFilesHelper(goodConfig)
////        val filePath: String = s"${filesHelper.thisConfigDirectory}/snapshot/test.txt"
////        val file = new File(filePath)
////        file.getParentFile.mkdirs
//
//        val result: Set[String] = filesHelper.assetsToConstruct
//        result("snapshot") should equal (false)
//        result.size should be (SORoutingFilesHelper.scaffolding.size - 1)
//      }
//    }
//    "configDirectoryExists" should {
//      "find if a config directory already exists" in new Config {
//        val filesHelper = SORoutingFilesHelper(goodConfig)
//        val filePath: String = s"${filesHelper.thisConfigDirectory}/test.txt"
//        val file = new File(filePath)
//        file.getParentFile.mkdirs
//
//        filesHelper.configDirectoryExists should equal (true)
//      }
//      "return false when that config directory doesn't exist" in new Config {
//        val filesHelper = SORoutingFilesHelper(goodConfig)
//
//        filesHelper.configDirectoryExists should equal (false)
//      }
//    }
    "parseSnapshotForTime" when {
      "passed a snapshot filename which contains a time" should {
        "parse out that time value and make it a LocalTime object" in new Config {
          val result: LocalTime = SORoutingFilesHelper(goodConfig).parseSnapshotForTime("/Users/robertfitzgerald/dev/ucd/phd/projects/2017su/SO-Routing/result/equil/snapshot/10/snapshot-06:52:45.nscData")
          result.getHour should equal (6)
          result.getMinute should equal (52)
          result.getSecond should equal (45)
        }
      }
    }
  }
}
