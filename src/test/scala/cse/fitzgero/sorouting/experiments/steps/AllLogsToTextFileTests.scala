package cse.fitzgero.sorouting.experiments.steps

import cse.fitzgero.sorouting.FileWriteSideEffectTestTemplate
import edu.ucdenver.fitzgero.lib.experiment.StepFailure

import scala.io.Source

class AllLogsToTextFileTests extends FileWriteSideEffectTestTemplate("GenerateTextFileLog") {
  "GenerateTextFileLog" when {
    "called with some data and a valid result path" should {
      "write that data to a test file" in {
        case class Config(reportPath: String)
        val reportPath = s"$testRootPath/test.txt"
        val config = Config(reportPath)
        val category1 = "foo"
        val key1 = "bar"
        val value1 = "baz"
        val category2 = "boog"
        val key2 = "noog"
        val value2 = "yoog"
        val log: Map[String, Map[String, String]] = Map(
          category1 -> Map(
            key1 -> value1
          ),
          category2 -> Map(
          key2 -> value2
          )
        )
        Reporting.AllLogsToTextFile(config, log)
        val result = Source.fromFile(reportPath).getLines.toVector
        result(0) should equal (category1)
        result(1) should equal (s"$key1: $value1")
        result(2) should equal (category2)
        result(3) should equal (s"$key2: $value2")
      }
    }
    "called with some data and a bad result path" should {
      "write that data to a test file" in {
        case class Config(reportPath: String)
        val reportPath = "/tmp/var/tmp/local/bin/bar/bash/boodle/potempkin/hindenberg.txt"
        val config = Config(reportPath)
        val category = "foo"
        val key = "bar"
        val value = "baz"
        val log: Map[String, Map[String, String]] = Map(
          category -> Map(
            key -> value
          )
        )
        val result = Reporting.AllLogsToTextFile(config, log)
        result.get._1 should equal (StepFailure(Some(reportPath)))
        result.get._2.isDefinedAt("stack trace") should be (true)
        result.get._2("stack trace").take(33).contains("java.nio.file.NoSuchFileException") should be (true)
      }
    }
  }
}
