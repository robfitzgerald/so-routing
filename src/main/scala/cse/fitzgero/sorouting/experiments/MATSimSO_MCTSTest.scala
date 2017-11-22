package cse.fitzgero.sorouting.experiments

import java.nio.file.Paths
import java.time.{LocalDateTime, LocalTime}

import cse.fitzgero.graph.config.KSPBounds
import cse.fitzgero.graph.config.KSPBounds.Iteration
import cse.fitzgero.sorouting.experiments.steps.{ExperimentAssetGenerator, Reporting, SystemOptimalMCTSRouting}
import cse.fitzgero.sorouting.matsimrunner.MATSimSimulator
import edu.ucdenver.fitzgero.lib.experiment.Experiment

object MATSimSO_MCTSTest extends Experiment with App with MATSimSimulator {

  val pop: Int = if (args.length > 0) args(0).toInt else 100
  val win: Int = if (args.length > 1) args(1).toInt else 15
  val route: Double = if (args.length > 2) args(2).toDouble else 0.20D
  val name: String = if (args.length > 3) args(3) else "mctsTest"
  val sourceAssetDirectory: String = if (args.length > 4) args(4) else "data/5x5"
  val sourceAssetName: String = Paths.get(sourceAssetDirectory).getFileName.toString
  val configLabel: String = s"$sourceAssetName-$pop-$win-$route"

  case class Config (
    sourceAssetsDirectory: String,
    experimentBaseDirectory: String,
    experimentConfigDirectory: String,
    experimentInstanceDirectory: String,
    populationSize: Int,
    timeWindow: Int,
    routePercentage: Double,
    startTime: LocalTime,
    departTime: LocalTime,
    endTime: Option[LocalTime],
    timeDeviation: Option[LocalTime],
    k: Int,
    kspBounds: Option[KSPBounds],
    overlapThreshold: Double,
    coefficientCp: Double, // 0 means flat monte carlo
    congestionRatioThreshold: Double,
    computationalLimit: Long // ms
  )
  val config = Config(
    sourceAssetDirectory,
    s"result/$name",
    s"result/$name/$configLabel",
    s"result/$name/$configLabel/${LocalDateTime.now.toString}",
    pop,
    win,
    route,
    startTime = LocalTime.parse("08:00:00"),
    departTime = LocalTime.parse("08:15:00"),
    endTime = Some(LocalTime.parse("09:00:00")),
    timeDeviation = Some(LocalTime.parse("00:15:00")),
    k = 4,
    kspBounds = Some(KSPBounds.IterationOrTime(10, 60000L)),
    overlapThreshold = 1.0D,
    coefficientCp = 0.05D,
    congestionRatioThreshold = 1.5D,
    computationalLimit = 10000L // milliseconds
  )

  runSync(config, List(
    ExperimentAssetGenerator.SetupConfigDirectory,
    ExperimentAssetGenerator.SetupInstanceDirectory,
    ExperimentAssetGenerator.RepeatedPopulation,
    SystemOptimalMCTSRouting.Incremental,
    Reporting.AppendToReportCSVFiles,
    Reporting.AllLogsToTextFile
  ))
}