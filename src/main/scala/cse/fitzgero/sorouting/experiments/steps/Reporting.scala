package cse.fitzgero.sorouting.experiments.steps

import java.nio.file.{Files, Path, Paths}

import scala.util.Try

import cse.fitzgero.sorouting.experiments.ops.{ExperimentOps, ExperimentStepOps}
import edu.ucdenver.fitzgero.lib.experiment._


object Reporting {

  type BasicReportData = {
    def populationSize: Int
    def timeWindow: Int
    def routePercentage: Double
    def sourceAssetsDirectory: String
    def experimentBaseDirectory: String
    def experimentInstanceDirectory: String
    def experimentConfigDirectory: String
    def congestionRatioThreshold: Double
  }

  case class ReportData(header: String, data: String)

  object AppendToReportCSVFiles extends SyncStep {
    val name: String = "[Reporting:AppendToReportCSV] Log average trip costs and some performance stats to the report csv file"

    type StepConfig = BasicReportData

    /**
      * experiment step which will gather any data in logs and write it out to a file with a simple human-readable format
      * @param conf config object decorated with the GenerateTextFileLogConfig trait
      * @param categoryLog experiment log
      * @return success|failure tuples
      */
    def apply(conf: StepConfig, categoryLog: ExperimentGlobalLog): Option[(StepStatus, ExperimentStepLog)] = Some {
      val header: String = "experiment type,source dir,instance dir,population size,optimal route population size,route percentage,time window,congestionThreshold,,network avg travel time,population avg travel time,expected cost effect,,has alternate paths,mcts run count,mcts found complete solution,selfish had overlap,optimal had overlap,mcts routes found,selfish overlap count,optimal overlap count,selfish same as optimal,not embarrassingly solvable requests\n"
      val baseReportFileURI: String = s"${conf.experimentBaseDirectory}/report.csv"
      val configReportFileURI: String = s"${conf.experimentConfigDirectory}/report.csv"

      val log: Map[String, String] = categoryLog.flatMap(_._2)
      val safeLog = inspectLog(log)_
      val outputData: String = Seq(
        safeLog("experiment.type"),
        conf.sourceAssetsDirectory,
        conf.experimentInstanceDirectory,
        conf.populationSize,
        (conf.populationSize * conf.routePercentage).toInt.toString,
        conf.routePercentage,
        conf.timeWindow,
        conf.congestionRatioThreshold.toString,
        "",
        safeLog("experiment.result.traveltime.avg.network"),
        safeLog("experiment.result.traveltime.avg.population"),
        getExpectedCostEffect(log),
        "",
        safeLog("algorithm.mksp.local.hasalternates"),
        safeLog("algorithm.selection.local.called"),
        safeLog("algorithm.selection.local.mcts.solution.complete"),
        safeLog("algorithm.selection.local.mcts.true.shortest.paths.had.overlap"),
        safeLog("algorithm.selection.local.mcts.optimal.paths.had.overlap"),
        safeLog("algorithm.selection.local.mcts.solution.route.count"),
        safeLog("algorithm.selection.local.mcts.overlap.count.selfish"),
        safeLog("algorithm.selection.local.mcts.overlap.count.optimal"),
        safeLog("algorithm.selection.local.mcts.selfish.matches.optimal"),
        safeLog("algorithm.selection.local.mcts.solution.meaningful")
        //
      ).mkString("",",","\n")


      val t: Try[Map[String, String]] =
        Try({
          Map(
            "fs.csv.report.base" -> ExperimentOps.writeLogToPath(outputData, Paths.get(baseReportFileURI), Some(header)),
            "fs.csv.report.config" -> ExperimentOps.writeLogToPath(outputData, Paths.get(configReportFileURI), Some(header))
          )
        })

      ExperimentStepOps.resolveTry(t)
    }
  }



  /**
    * build the basic report data without finalizing it (ie. adding a newline character)
    * @param conf the experiment config object
    * @param log the experiment running logger
    * @return
    */
  def basicReport(conf: BasicReportData, log: ExperimentGlobalLog): ReportData = {
    val safeLog = inspectLog(log.flatMap(_._2))_
    val header: String = "experiment type,source dir,instance dir,population size,optimal route population size,route percentage,time window,network avg travel time,population avg travel time"
    val dataRow: String = Seq(
      safeLog("experiment.type"),
      conf.sourceAssetsDirectory,
      conf.experimentInstanceDirectory,
      conf.populationSize,
      (conf.populationSize * conf.routePercentage).toInt.toString,
      conf.routePercentage,
      conf.timeWindow,
      safeLog("experiment.result.traveltime.avg.network"),
      safeLog("experiment.result.traveltime.avg.population")
    ).mkString(",")
    ReportData(header, dataRow)
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