package cse.fitzgero.sorouting

import java.io.File

import org.apache.spark.{SparkConf, SparkContext}

import scala.util.{Failure, Success, Try}

/**
  * Unit Test Template that combines the FileWriteSideEffect and SparkUnit Test Templates
  * @note see http://mkuthan.github.io/blog/2015/03/01/spark-unit-testing/
  */
abstract class SparkAndFileWriteUnitTestTemplate(testName: String, cores: String = "*") extends SORoutingUnitTestTemplate {

  /**
    * this value should be used in place of any root directory paths in the test suite
    */
  val testRootPath: String = s"src/test/temp/$testName"

  private val master = s"local[$cores]"
  private val appName = testName
  protected var sc: SparkContext = _

  before {
    val conf = new SparkConf()
      .setMaster(master)
      .setAppName(appName)

    sc = new SparkContext(conf)
    sc.setLogLevel("ERROR")

    clearTempData match {
      case Success(deleteList) =>
        if (deleteList.nonEmpty) println(s"~~ before test file cleanup ~~\n$deleteList")
      case Failure(e) => println(s"~~ before test file cleanup FAILED ~~\n$e")
    }
  }

  after {
    if (sc != null) {
      sc.stop()
    }

    clearTempData match {
      case Success(deleteList) =>
        if (deleteList.nonEmpty) println(s"~~ after test file cleanup ~~\n$deleteList")
      case Failure(e) => println(s"~~ after test file cleanup FAILED ~~\n$e")
    }
  }


  private def clearTempData: Try[String]= {
    Try({
      val file: File = new File(testRootPath)
      delete(file).filter(_._2).map(_._1).mkString("\n")
    })
  }

  private def delete(file: File): Array[(String, Boolean)] = {
    Option(file.listFiles).map(_.flatMap(f => delete(f))).getOrElse(Array()) :+ (file.getPath -> file.delete)
  }
}
