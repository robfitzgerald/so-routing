package cse.fitzgero.sorouting.experiments.steps

import java.nio.file.{Files, Paths}
import java.time.LocalTime

import cse.fitzgero.sorouting.experiments.ops.{ExperimentFSOps, ExperimentStepOps}
import cse.fitzgero.sorouting.matsimrunner.snapshot._
import edu.ucdenver.fitzgero.lib.experiment.{ExperimentGlobalLog, ExperimentStepLog, StepStatus, SyncStep}
import org.matsim.api.core.v01.network.Link
import org.matsim.api.core.v01.{Id, Scenario}
import org.matsim.core.config.{Config, ConfigUtils}
import org.matsim.core.controler.{AbstractModule, Controler}
import org.matsim.core.scenario.ScenarioUtils

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

object MATSimRunner {

  type MATSimRunnerConfig = {
    def experimentInstanceDirectory: String
    def timeWindow: Int
    def startTime: LocalTime
    def endTime: Option[LocalTime]
  }


  object AnalyticSnapshot extends SyncStep {
    val name: String = "MATSim Simulation runner with snapshot data generation"

    type StepConfig = MATSimRunnerConfig

    override def apply(config: StepConfig, log: ExperimentGlobalLog = Map()): Option[(StepStatus, ExperimentStepLog)] = Some {

      val instanceDirectory: String = s"${config.experimentInstanceDirectory}"
      val endtimeInterpretation: LocalTime = config.endTime match {
        case Some(time) => time
        case None => LocalTime.MAX
      }

      val t: Try[Map[String, String]] =
        Try {
          val snapshotFilePath: String = MATSimRun(instanceDirectory, config.timeWindow, config.startTime, endtimeInterpretation)
          Map("fs.xml.snapshot" -> snapshotFilePath)
        }

      ExperimentStepOps.resolveTry(t, Some(name))
    }
  }

  def MATSimRun (currentDirectory: String, timeWindow: Int, startTime: LocalTime, endTime: LocalTime): String = {

    val matsimOutputDirectory: String = s"$currentDirectory/matsim"
    Files.createDirectories(Paths.get(matsimOutputDirectory))

    val config: Config = ConfigUtils.loadConfig(s"$currentDirectory/config.xml")

    config.controler().setOutputDirectory(matsimOutputDirectory)
    val scenario: Scenario = ScenarioUtils.loadScenario(config)
    val controler: Controler = new Controler(config)


    def run(): String = {
      val networkLinks: scala.collection.mutable.Map[Id[Link], _] = scenario.getNetwork.getLinks.asScala
      var currentNetworkState: NetworkStateCollector = NetworkStateCollector(networkLinks)
      var currentIteration: Int = 1
      val timeTracker: TimeTracker = TimeTracker(startTime.format(ExperimentFSOps.HHmmssFormat), endTime.format(ExperimentFSOps.HHmmssFormat))


      // add the events handlers
      controler.addOverridingModule(new AbstractModule(){
        @Override def install (): Unit = {
          this.addEventHandlerBinding().toInstance(new SnapshotEventHandler({
            case LinkEventData(e) =>
              if (timeTracker.belongsToThisTimeGroup(e))
                currentNetworkState = currentNetworkState.update(e)
            case NewIteration(i) =>
              currentIteration = i
          }))
        }
      })

      //start the simulation
      controler.run()

      // write snapshot and return filename
      NetworkStateCollector.toXMLFile(s"$currentDirectory", currentNetworkState, "snapshot.xml") match {
        case Success(file) => file
        case Failure(e) => throw e
      }
    }

    run()
  }
}
