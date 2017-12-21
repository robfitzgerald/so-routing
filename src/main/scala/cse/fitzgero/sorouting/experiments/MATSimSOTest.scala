package cse.fitzgero.sorouting.experiments

import java.nio.file.Paths
import java.time.{LocalDateTime, LocalTime}

import cse.fitzgero.sorouting.experiments.steps.{ExperimentAssetGenerator, Reporting, SystemOptimalRouting}
import cse.fitzgero.sorouting.matsimrunner.MATSimSimulator
import cse.fitzgero.sorouting.model.population.{LocalPopulationNormalGenerator, LocalPopulationOps}
import edu.ucdenver.fitzgero.lib.experiment.Experiment

object MATSimSOTest extends Experiment with App with MATSimSimulator {

  val pop: Int = if (args.length > 0) args(0).toInt else 100
  val win: Int = if (args.length > 1) args(1).toInt else 15
  val route: Double = if (args.length > 2) args(2).toDouble else 0.20D
  val congestionThreshold: Double = if (args.length > 3) args(3).toDouble else 1.50D
  val name: String = if (args.length > 4) args(4) else "mctsTest"
  val sourceAssetDirectory: String = if (args.length > 5) args(5) else "data/5x5"
  val sourceAssetName: String = Paths.get(sourceAssetDirectory).getFileName.toString
  val configLabel: String = s"$sourceAssetName-$pop-$win-$route-$congestionThreshold"

  case class Config (
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
    timeDeviation: Option[LocalTime]
  )
  val config = Config(
    sourceAssetDirectory,
    s"result/$name",
    s"result/$name/$configLabel",
    s"result/$name/$configLabel/${LocalDateTime.now.toString}",
//    LocalPopulationSelectedSourceSinkGenerator("a", "b"),
    LocalPopulationNormalGenerator,
    pop,
    win,
    route,
    LocalTime.parse("08:00:00"),
    LocalTime.parse("08:15:00"),
    Some(LocalTime.parse("09:00:00")),
    Some(LocalTime.parse("00:15:00"))
  )

  runSync(config, List(
    ExperimentAssetGenerator.SetupConfigDirectory,
    ExperimentAssetGenerator.SetupInstanceDirectory,
    ExperimentAssetGenerator.RepeatedPopulation,
    SystemOptimalRouting.Incremental,
    Reporting.AppendToReportCSVFiles,
    Reporting.AllLogsToTextFile
  ))
}