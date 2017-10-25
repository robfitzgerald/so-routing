package cse.fitzgero.sorouting.experiments.steps

import java.io.{PrintWriter, StringWriter}
import java.nio.file.{Files, Path, Paths}

import cse.fitzgero.sorouting.experiments.ExperimentStepOps
import edu.ucdenver.fitzgero.lib.experiment._

import scala.util.{Failure, Success, Try}


object AllLogsToTextFile extends SyncStep {
  val name: String = "Generate Text File Log"

  type StepConfig = {
    def reportPath: String
  }

  /**
    * experiment step which will gather any data in logs and write it out to a file with a simple human-readable format
    * @param conf config object decorated with the GenerateTextFileLogConfig trait
    * @param log experiment log
    * @return success|failure tuples
    */
  def apply(conf: StepConfig, log: ExperimentGlobalLog): Option[(StepStatus, ExperimentStepLog)] = {
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
      case Success(resultPath) => Some(StepSuccess(Some(resultPath.toString)), Map())
      case Failure(exception) =>
        val e = ExperimentStepOps.extractExceptionData(exception)
        ()
    }
  }
}