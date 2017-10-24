package cse.fitzgero.sorouting.experiments.steps

import java.io.{PrintWriter, StringWriter}
import java.nio.file.{Files, Path, Paths}

import edu.ucdenver.fitzgero.lib.experiment._

import scala.util.{Failure, Success, Try}

/**
  * decorate a config object with the required data for this step
  */
trait GenerateTextFileLogConfig {
  val reportPath: String
}

object GenerateTextFileLog {

  type Config = Any with GenerateTextFileLogConfig

  /**
    * experiment step which will gather any data in logs and write it out to a file with a simple human-readable format
    * @param conf config object decorated with the GenerateTextFileLogConfig trait
    * @param log experiment log
    * @return success|failure tuples
    */
  def apply(conf: Config, log: Map[String, Map[String, String]]): (StepStatus, Map[String, String]) = {
    val outputData: Array[Byte] =
      log
        .map(
          cat =>
            s"${cat._1}\n${
              cat._2
                .map(tup =>
                  s"${tup._1}: ${tup._2}")
                .mkString("\n")
            }").mkString("\n").getBytes

    Try({
      val path: Path = Paths.get(conf.reportPath)
      Files.write(path, outputData)
    }) match {
      case Success(resultPath) => (StepSuccess(Some(resultPath.toString)), Map())
      case Failure(exception) =>
        val exceptionString: StringWriter = new StringWriter
        exception.printStackTrace(new PrintWriter(exceptionString))
        (StepFailure(Some(exception.getMessage)), Map("stack trace" -> exceptionString.toString))
    }
  }
}
