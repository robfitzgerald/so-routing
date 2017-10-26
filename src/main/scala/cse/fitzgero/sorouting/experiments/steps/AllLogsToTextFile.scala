package cse.fitzgero.sorouting.experiments.steps

import java.nio.file.{Files, Path, Paths}

import cse.fitzgero.sorouting.experiments.ops.ExperimentStepOps
import edu.ucdenver.fitzgero.lib.experiment._

import scala.util.Try


object AllLogsToTextFile extends SyncStep {
  val name: String = "AllLogsToTextFile: Generate Text File Log"

  type StepConfig = {
    def reportPath: String
  }

  /**
    * experiment step which will gather any data in logs and write it out to a file with a simple human-readable format
    * @param conf config object decorated with the GenerateTextFileLogConfig trait
    * @param log experiment log
    * @return success|failure tuples
    */
  def apply(conf: StepConfig, log: ExperimentGlobalLog): Option[(StepStatus, ExperimentStepLog)] = Some {
    val reportFileURI: String = s"${conf.reportPath}/report.txt"
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

    val t: Try[Map[String, String]] =
      Try({
        val path: Path = Paths.get(reportFileURI)
        Files.write(path, outputData)
        Map("fs.text.report" -> path.toString)
      })

    ExperimentStepOps.resolveTry(t)
  }
}