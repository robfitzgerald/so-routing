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
  val name: String = if (args.length > 3) args(3) else "test"
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
    kSPBounds: Option[KSPBounds],
    overlapThreshold: Double,
    coefficientCp: Double, // 0 means flat mon
    congestionRatioThreshold: Double,
    computationalLimit: Long // ms.
  )
  val config = Config(
    sourceAssetDirectory,
    s"result/$name",
    s"result/$name/$configLabel",
    s"result/$name/$configLabel/${LocalDateTime.now.toString}",
    pop,
    win,
    route,
    LocalTime.parse("08:00:00"),
    LocalTime.parse("08:15:00"),
    Some(LocalTime.parse("09:00:00")),
    Some(LocalTime.parse("00:15:00")),
    4,
    Some(Iteration(10)),
    1.0D,
    0D,
    3D,
    5000L
  )

  // TODO: consolidate config requirements when common elements, parameterize relative paths for reports, populations
  runSync(config, List(
    // TODO: add scaffold of directory structure
    ExperimentAssetGenerator.SetupConfigDirectory,
    ExperimentAssetGenerator.SetupInstanceDirectory,
    ExperimentAssetGenerator.RepeatedPopulation,
    SystemOptimalMCTSRouting.Incremental,
    Reporting.AppendToReportCSVFiles,
    Reporting.AllLogsToTextFile
  ))
}

//object foo {
//  type DirectoriesConfig = {
//    def experimentSetDirectory: String // sits above all configurations in a set of related tests
//    def experimentConfigDirectory: String // has the base config and a set of instance directories
//    def experimentInstanceDirectory: String // a date/time-named directory
//  }
//}