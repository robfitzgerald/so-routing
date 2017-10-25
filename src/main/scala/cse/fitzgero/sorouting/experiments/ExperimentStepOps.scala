package cse.fitzgero.sorouting.experiments

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

  def resolveTry(t: Try[Map[String, String]]): (StepStatus, Map[String, String]) = {
    t match {
      case Success(log) =>
        (StepSuccess(None), log)
      case Failure(e) =>
        val e = extractExceptionData(e)
        (StepFailure(Some(e.msg)), Map(e.msg -> e.stackTrace))
    }
  }

//      match {
//        case Success(result) =>
//          Some((StepSuccess(Some("")), Map.empty[String, String]))
//        case Failure(throwable) =>
//          val e = ExperimentStepOps.extractExceptionData(throwable)
//          Some((StepFailure(Some(e.msg)), Map.empty[String, String]))
//      }
}
