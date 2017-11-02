package cse.fitzgero.sorouting.experiments.steps

import java.nio.file.{Files, Path, Paths, StandardOpenOption}

import cse.fitzgero.sorouting.experiments.ops.ExperimentStepOps
import edu.ucdenver.fitzgero.lib.experiment._
import scala.util.Try


object Reporting {

  object AppendToReportCSVFiles extends SyncStep {
    val name: String = "[Reporting:AppendToReportCSV] Log average trip costs and some performance stats to the report csv file"

    type StepConfig = {
      def populationSize: Int
      def timeWindow: Int
      def routePercentage: Double
      def sourceAssetsDirectory: String
      def experimentBaseDirectory: String
      def experimentInstanceDirectory: String
      def experimentConfigDirectory: String
    }

    val header: Array[Byte] = "experiment type,source dir,instance dir,population size,optimal route population size,route percentage,time window,network avg travel time,population avg travel time,expected cost effect,combinations,has alternate paths\n".getBytes

    /**
      * experiment step which will gather any data in logs and write it out to a file with a simple human-readable format
      * @param conf config object decorated with the GenerateTextFileLogConfig trait
      * @param categoryLog experiment log
      * @return success|failure tuples
      */
    def apply(conf: StepConfig, categoryLog: ExperimentGlobalLog): Option[(StepStatus, ExperimentStepLog)] = Some {
      val baseReportFileURI: String = s"${conf.experimentBaseDirectory}/report.csv"
      val configReportFileURI: String = s"${conf.experimentConfigDirectory}/report.csv"

      val log: Map[String, String] = categoryLog.flatMap(_._2)
      val safeLog = inspectLog(log)_
      val outputData: Array[Byte] = Seq(
        safeLog("experiment.type"),
        conf.sourceAssetsDirectory,
        conf.experimentInstanceDirectory,
        conf.populationSize,
        (conf.populationSize * conf.routePercentage).toInt.toString,
        conf.routePercentage,
        conf.timeWindow,
        safeLog("experiment.result.traveltime.avg.network"),
        safeLog("experiment.result.traveltime.avg.population"),
        getExpectedCostEffect(log),
        safeLog("algorithm.selection.local.combinations"),
        safeLog("algorithm.mksp.local.hasalternates"))
        .mkString("",",","\n")
        .getBytes

      val t: Try[Map[String, String]] =
        Try({
          Map(
            "fs.csv.report.base" -> writeLogToPath(outputData, Paths.get(baseReportFileURI)),
            "fs.csv.report.config" -> writeLogToPath(outputData, Paths.get(configReportFileURI))
          )
        })

      ExperimentStepOps.resolveTry(t)
    }

    def writeLogToPath(outputData: Array[Byte], path: Path): String = {
      if (Files.notExists(path))
        Files.write(path, header, StandardOpenOption.CREATE)
      Files.write(path, outputData, StandardOpenOption.APPEND)
      path.toString
    }
  }

  object AllLogsToTextFile extends SyncStep {
    val name: String = "[Reporting:AllLogsToTextFile] Generate Text File Log"

    type StepConfig = {
      def experimentInstanceDirectory: String
    }

    /**
      * experiment step which will gather any data in logs and write it out to a file with a simple human-readable format
      * @param conf config object decorated with the GenerateTextFileLogConfig trait
      * @param log experiment log
      * @return success|failure tuples
      */
    def apply(conf: StepConfig, log: ExperimentGlobalLog): Option[(StepStatus, ExperimentStepLog)] = Some {
      val reportFileURI: String = s"${conf.experimentInstanceDirectory}/report.txt"
      val outputData: Array[Byte] =
        log
          .map(
            cat =>
              s"${cat._1}\n${cat._2
                              .map(tup => s"${tup._1}: ${tup._2}")
                              .mkString("\n")}")
          .mkString("\n\n")
          .getBytes

      val t: Try[Map[String, String]] =
        Try({
          val path: Path = Paths.get(reportFileURI)
          Files.write(path, outputData)
          Map("fs.text.report" -> path.toString)
        })

      ExperimentStepOps.resolveTry(t)
    }
  }


  def inspectLog(log: Map[String, String])(key: String): String =
    if (log.isDefinedAt(key))
      log(key)
    else ""

  def getExpectedCostEffect(log: Map[String, String]): String =
    if (log.isDefinedAt("algorithm.selection.local.cost.effect") && log.isDefinedAt("algorithm.mssp.local.cost.effect"))
      (log("algorithm.selection.local.cost.effect").toLong + log("algorithm.mssp.local.cost.effect").toLong).toString
    else if (log.isDefinedAt("algorithm.mssp.local.cost.effect"))
      log("algorithm.mssp.local.cost.effect")
    else
      ""

}