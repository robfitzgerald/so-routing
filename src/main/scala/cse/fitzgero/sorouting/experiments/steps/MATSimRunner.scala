package cse.fitzgero.sorouting.experiments.steps

import java.time.LocalTime

import scala.util.Try

import cse.fitzgero.sorouting.experiments.ops.{ExperimentStepOps, MATSimOps}
import edu.ucdenver.fitzgero.lib.experiment.{ExperimentGlobalLog, ExperimentStepLog, StepStatus, SyncStep}

object MATSimRunner {

  type MATSimRunnerConfig = {
    def experimentInstanceDirectory: String
    def timeWindow: Int
    def startTime: LocalTime
    def endTime: Option[LocalTime]
  }


  object AnalyticSnapshot extends SyncStep {
    val name: String = "[AnalyticSnapshot] MATSim Simulation runner with snapshot data generation"

    type StepConfig = MATSimRunnerConfig

    override def apply(config: StepConfig, log: ExperimentGlobalLog = Map()): Option[(StepStatus, ExperimentStepLog)] = Some {

      val instanceDirectory: String = s"${config.experimentInstanceDirectory}"
      val endtimeInterpretation: LocalTime = config.endTime match {
        case Some(time) => time
        case None => LocalTime.MAX
      }

      val t: Try[Map[String, String]] =
        Try {
          val snapshotFilePath: String = MATSimOps.MATSimRun(instanceDirectory, config.startTime, endtimeInterpretation, config.timeWindow)
          Map("fs.xml.snapshot" -> snapshotFilePath)
        } flatMap {
          logWithSnapshotFile: Map[String, String] => Try {
            val networkAvgTravelTime: String = MATSimOps.getNetworkAvgTravelTime(config.experimentInstanceDirectory)
            val populationTravelTime: String = MATSimOps.getPopulationAvgTravelTime(config.experimentInstanceDirectory)

            logWithSnapshotFile ++ Map(
              "experiment.result.traveltime.avg.network" -> networkAvgTravelTime,
              "experiment.result.traveltime.avg.population" -> populationTravelTime
            )
          }
        }

      ExperimentStepOps.resolveTry(t, Some(name))
    }
  }
}
