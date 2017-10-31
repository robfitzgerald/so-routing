package cse.fitzgero.sorouting.experiments

import java.time.{LocalDateTime, LocalTime}

import cse.fitzgero.sorouting.experiments.steps.{ExperimentAssetGenerator, Reporting, SystemOptimalRouting}
import cse.fitzgero.sorouting.matsimrunner.MATSimSimulator
import edu.ucdenver.fitzgero.lib.experiment.Experiment

object MATSimSOTest extends Experiment with App with MATSimSimulator {

  val pop: Int = if (args.size > 0) args(0).toInt else 100
  val win: Int = if (args.size > 1) args(1).toInt else 60
  val route: Double = if (args.size > 2) args(2).toDouble else 0.10D

  case class Config (
    sourceAssetsDirectory: String,
    experimentConfigDirectory: String,
    networkURI: String,
    experimentInstanceDirectory: String,
    reportPath: String,
    populationSize: Int,
    timeWindow: Int,
    routePercentage: Double,
    startTime: LocalTime,
    departTime: LocalTime,
    endTime: Option[LocalTime],
    timeDeviation: Option[LocalTime]
  )
  val config = Config(
    "data/rye",
    "result/20171031",
    "data/rye/network.xml",
    s"result/20171031/${LocalDateTime.now.toString}",
    "result/20171031",
    pop,
    win,
    route,
    LocalTime.parse("08:00:00"),
    LocalTime.parse("08:15:00"),
    Some(LocalTime.parse("09:00:00")),
    Some(LocalTime.parse("00:05:00"))
  )

  // TODO: consolidate config requirements when common elements, parameterize relative paths for reports, populations
  runSync(config, List(
    // TODO: add scaffold of directory structure
    ExperimentAssetGenerator.SetupConfigDirectory,
    ExperimentAssetGenerator.SetupInstanceDirectory,
    ExperimentAssetGenerator.RepeatedPopulation,
    SystemOptimalRouting.Incremental, // TODO: add SO experiment runner
    Reporting.SelectedLogData
  ))
}

// TODO: run selfish routing tests over weekend. compare unique to repeated populations

//object foo {
//  type DirectoriesConfig = {
//    def experimentSetDirectory: String // sits above all configurations in a set of related tests
//    def experimentConfigDirectory: String // has the base config and a set of instance directories
//    def experimentInstanceDirectory: String // a date/time-named directory
//  }
//}