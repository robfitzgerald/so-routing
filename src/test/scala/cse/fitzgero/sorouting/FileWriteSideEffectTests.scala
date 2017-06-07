package cse.fitzgero.sorouting

import java.io.File

import scala.util.{Success, Try}

/**
  * designed to provide automated cleanup of side effect data
  * @param testName (unique) name of the test suite
  */
abstract class FileWriteSideEffectTests (val testName: String) extends SORoutingUnitTests {
  def clearTempData: Try[String]= {
    Try({
      val file: File = new File(s"test/temp/$testName")
      val list: String = delete(file).mkString("\n")
    })
  }
  private def delete(file: File): Array[(String, Boolean)] = {
    Option(file.listFiles).map(_.flatMap(f => delete(f))).getOrElse(Array()) :+ (file.getPath -> file.delete)
  }
  before {
    clearTempData match {
      case Success(deleteList) => println(s"before: deleted $deleteList")
    }
  }

  after {
    clearTempData match {
      case Success(deleteList) => println(s"after: deleted $deleteList")
    }
  }
}
