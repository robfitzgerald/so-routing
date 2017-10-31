package cse.fitzgero.sorouting.experiments

import java.time.{LocalDateTime, LocalTime}

import cse.fitzgero.sorouting.experiments.steps.{AllLogsToTextFile, ExperimentAssetGenerator, MATSimRunner}
import edu.ucdenver.fitzgero.lib.experiment.Experiment

// notes
// 1. do we have a problem where drivers are starting on links, and therefore, our counts are off?
//    do we need to add each driver's starting link position to our network count?
//    or do we just need to ignore link removals when the count dips below 0?
// 2. see the various TODOs on this page
// 3. get UE Snapshot Runner running by tonight for weekend experiments
//


object MATSimTest extends Experiment with App {

  val pop: Int = if (args.size > 0) args(0).toInt else 500
  val win: Int = if (args.size > 1) args(1).toInt else 15
//  val route: Double = if (args.size > 2) args(2).toDouble else 0.20D

  case class Config (
    sourceAssetsDirectory: String,
    experimentConfigDirectory: String,
    networkURI: String,
    experimentInstanceDirectory: String,
    reportPath: String,
    populationSize: Int,
    timeWindow: Int,
    startTime: LocalTime,
    departTime: LocalTime,
    endTime: Option[LocalTime],
    timeDeviation: Option[LocalTime]
  )
  val config = Config(
    "data/rye",
    "result/20171030",
    "data/rye/network.xml",
    s"result/20171030/${LocalDateTime.now.toString}",
    "result/20171030",
    pop,
    win,
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
    MATSimRunner.AnalyticSnapshot, // TODO: command-line config; fix bug!
    AllLogsToTextFile
    // TODO: GetMATSimResultsUE, GetMATSimResultsSO (the same?)
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