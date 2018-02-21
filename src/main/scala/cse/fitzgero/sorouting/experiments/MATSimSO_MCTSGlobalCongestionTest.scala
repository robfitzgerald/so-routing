package cse.fitzgero.sorouting.experiments

import java.nio.file.Paths
import java.time.{LocalDateTime, LocalTime}

import cse.fitzgero.graph.config.KSPBounds
import cse.fitzgero.sorouting.experiments.steps.{ExperimentAssetGenerator, Reporting, SystemOptimalMCTSRouting, SystemOptimalMCTSGlobalCongestionRouting}
import cse.fitzgero.sorouting.matsimrunner.MATSimSimulator
import cse.fitzgero.sorouting.model.population.{LocalPopulationNormalGenerator, LocalPopulationOps}
import edu.ucdenver.fitzgero.lib.experiment.Experiment

object MATSimSO_MCTSGlobalCongestionTest extends Experiment with App with MATSimSimulator {

  val pop: Int = if (args.length > 0) args(0).toInt else 100
  val win: Int = if (args.length > 1) args(1).toInt else 15
  val route: Double = if (args.length > 2) args(2).toDouble else 0.20D
  val congestionThreshold: Double = if (args.length > 3) args(3).toDouble else 1.50D
  val name: String = if (args.length > 4) args(4) else "mctsTest"
  val sourceAssetDirectory: String = if (args.length > 5) args(5) else "data/5x5"
  val sourceAssetName: String = Paths.get(sourceAssetDirectory).getFileName.toString
  val configLabel: String = s"$sourceAssetName-$pop-$win-$route-$congestionThreshold"

  case class Config (
    configLabel: String,
    sourceAssetsDirectory: String,
    experimentBaseDirectory: String,
    experimentConfigDirectory: String,
    experimentInstanceDirectory: String,
    populationGenerator: LocalPopulationOps,
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
    computationalLimit: Long, // ms
    randomSeed: Long
  )
  val config = Config(
    configLabel,

    // experiment file system arguments
    sourceAssetDirectory,
    s"result/$name",
    s"result/$name/$configLabel",
    s"result/$name/$configLabel/${LocalDateTime.now.toString}",

    LocalPopulationNormalGenerator,
//    LocalPopulationSelectedSourceSinkGenerator("254129111","254809988"), // rye

    // general experiment parameters
    pop,
    win,
    route,
    startTime = LocalTime.parse("08:00:00"),
    departTime = LocalTime.parse("08:15:00"),
    endTime = Some(LocalTime.parse("09:00:00")),
    timeDeviation = Some(LocalTime.parse("00:15:00")),

    // algorithm-specific parameters
    k = 4, // the k in KSP
    kspBounds = Some(KSPBounds.IterationOrTime(10, 20000L)), // the way we determine ending our search for alternative paths in KSP
    overlapThreshold = 1.0D, // the percentage that alternate paths are allowed to overlap in KSP
//    coefficientCp = 1e-6, // less exploration for our problem seems to work better
    coefficientCp = 0.7071D, // shown by Kocsis and Szepesvari (2006) to perform well (satisfy the 'Hoeffding inequality')
//    coefficientCp = 0D, // finds a solution if possible in most situations, best for our case (flat monte carlo, yes?)
    congestionRatioThreshold = congestionThreshold, // the amount that the network congestion can increase as a result of a simulation in order to receive a reward in MCTS
    computationalLimit = 10000L, // milliseconds
    randomSeed = 0L
  )

  runSync(config, List(
    ExperimentAssetGenerator.SetupConfigDirectory,
    ExperimentAssetGenerator.SetupInstanceDirectory,
    ExperimentAssetGenerator.RepeatedPopulation,
    SystemOptimalMCTSGlobalCongestionRouting.Incremental,
    Reporting.AppendToReportCSVFiles,
    Reporting.AllLogsToTextFile
  ))
}