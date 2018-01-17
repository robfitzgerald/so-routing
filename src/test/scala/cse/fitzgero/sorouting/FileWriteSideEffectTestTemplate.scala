package cse.fitzgero.sorouting

import java.io.File
import java.nio.file.{Files, Path, Paths}

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

  private val fullPath: Path = Paths.get(s"${Paths.get("").toAbsolutePath.toString}/$testRootPath")

  def createPath(): Unit = if (!Files.exists(fullPath)) Files.createDirectories(fullPath)

  private def clearTempData: Try[String]= {
    Try({
//      Files.deleteIfExists(Paths.get(testRootPath))
      val file: File = new File(s"$testRootPath")
      delete(file).filter(_._2).map(_._1).mkString("\n")
    })
  }

  private def delete(file: File): Array[(String, Boolean)] = {
    Option(file.listFiles).map(_.flatMap(f => delete(f))).getOrElse(Array()) :+ (file.getPath -> file.delete)
  }

  before {
    createPath()
//    clearTempData match {
//      case Success(deleteList) =>
//        if (deleteList.nonEmpty) println(s"~~ before test file cleanup ~~\n$deleteList")
//      case Failure(e) => println(s"~~ before test file cleanup FAILED ~~\n$e")
//    }
  }

  after {
    clearTempData match {
      case Success(deleteList) =>
        if (deleteList.nonEmpty) println(s"~~ after test file cleanup ~~\n$deleteList")
      case Failure(e) => println(s"~~ after test file cleanup FAILED ~~\n$e")
    }
  }
}
