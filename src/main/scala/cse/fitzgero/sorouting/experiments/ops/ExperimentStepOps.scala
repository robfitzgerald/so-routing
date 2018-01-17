package cse.fitzgero.sorouting.experiments.ops

import java.io.{PrintWriter, StringWriter}

import edu.ucdenver.fitzgero.lib.experiment.{StepFailure, StepStatus, StepSuccess}

import scala.util.{Failure, Success, Try}

object ExperimentStepOps {
  case class ExceptionData(msg: String, stackTrace: String)

  def extractExceptionData(e: Throwable): ExceptionData = {
    val exceptionString: StringWriter = new StringWriter
    e.printStackTrace(new PrintWriter(exceptionString))
    ExceptionData(e.getMessage, exceptionString.toString)
  }

  def resolveTry(t: Try[Map[String, String]], successMessage: Option[String] = None): (StepStatus, Map[String, String]) = {
    t match {
      case Success(log) =>
        (StepSuccess(successMessage), log)
      case Failure(error) =>
        val e = extractExceptionData(error)
        (StepFailure(Some(e.msg)), Map(e.msg -> e.stackTrace))
    }
  }
}
