package cse.fitzgero.sorouting

import java.io.File

import scala.util.{Failure, Success, Try}

/**
  * designed to provide automated cleanup of side effect data
  * @param testName (unique) name of the test suite, which will be used for making the root path (in "testRootPath")
  */
abstract class FileWriteSideEffectTestTemplate (testName: String) extends SORoutingUnitTestTemplate {
  /**
    * this value should be used in place of any root directory paths in the test suite
    */
  val testRootPath: String = s"src/test/temp/$testName"

  private def clearTempData: Try[String]= {
    Try({
      val file: File = new File(testRootPath)
      delete(file).filter(_._2).map(_._1).mkString("\n")
    })
  }

  private def delete(file: File): Array[(String, Boolean)] = {
    Option(file.listFiles).map(_.flatMap(f => delete(f))).getOrElse(Array()) :+ (file.getPath -> file.delete)
  }

  before {
    clearTempData match {
      case Success(deleteList) =>
        if (deleteList.nonEmpty) println(s"~~ before test file cleanup ~~\n$deleteList")
      case Failure(e) => println(s"~~ before test file cleanup FAILED ~~\n$e")
    }
  }

  after {
    clearTempData match {
      case Success(deleteList) =>
        if (deleteList.nonEmpty) println(s"~~ after test file cleanup ~~\n$deleteList")
      case Failure(e) => println(s"~~ after test file cleanup FAILED ~~\n$e")
    }
  }
}
