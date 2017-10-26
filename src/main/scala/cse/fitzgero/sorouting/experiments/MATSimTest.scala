package cse.fitzgero.sorouting.experiments

import java.time.{LocalDateTime, LocalTime}

import cse.fitzgero.sorouting.experiments.steps.{AllLogsToTextFile, ExperimentAssetGenerator, MATSimRunner}
import edu.ucdenver.fitzgero.lib.experiment.{Experiment, SyncStep}

object MATSimTest extends Experiment with App {
  case class Config (
    sourceAssetsDirectory: String,
    experimentConfigDirectory: String,
    networkURI: String,
    experimentInstanceDirectory: String,
    reportPath: String,
    populationSize: Int,
    timeWindow: Int,
    startTime: LocalTime,
    endTime: Option[LocalTime]
  )
  val config = Config(
    "data/5x5",
    "result/20171026",
    "data/5x5/network.xml",
    s"result/20171026/${LocalDateTime.now.toString}",
    "result/20171026",
    500,
    60,
    LocalTime.parse("08:00:00"),
    Some(LocalTime.parse("10:00:00"))
  )

  runSync(config, Seq[SyncStep](
    ExperimentAssetGenerator.SetupConfigDirectory,
    ExperimentAssetGenerator.SetupInstanceDirectory,
    ExperimentAssetGenerator.Unique,
    MATSimRunner.AnalyticSnapshot,
    AllLogsToTextFile
  ))
}
