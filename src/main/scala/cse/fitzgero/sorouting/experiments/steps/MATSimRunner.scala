package cse.fitzgero.sorouting.experiments.steps

import java.nio.file.{Files, Paths}
import java.time.LocalTime

import cse.fitzgero.sorouting.experiments.ops.{ExperimentFSOps, ExperimentStepOps}
import cse.fitzgero.sorouting.matsimrunner.network.MATSimNetworkToCollection
import cse.fitzgero.sorouting.matsimrunner.snapshot.NewNetworkAnalyticStateCollector.SnapshotCollector
import cse.fitzgero.sorouting.matsimrunner.snapshot.{NewNetworkAnalyticStateCollector, _}
import cse.fitzgero.sorouting.model.roadnetwork.costfunction.BPRCostFunctionType
import edu.ucdenver.fitzgero.lib.experiment.{ExperimentGlobalLog, ExperimentStepLog, StepStatus, SyncStep}
import org.matsim.api.core.v01.Scenario
import org.matsim.core.config.{Config, ConfigUtils}
import org.matsim.core.controler.{AbstractModule, Controler}
import org.matsim.core.scenario.ScenarioUtils

import scala.io.Source
import scala.util.matching.Regex
import scala.util.{Failure, Success, Try}
import scala.xml.XML

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
          val snapshotFilePath: String = MATSimRun(instanceDirectory, config.startTime, endtimeInterpretation, config.timeWindow)
          Map("fs.xml.snapshot" -> snapshotFilePath)
        } flatMap {
          logWithSnapshotFile: Map[String, String] => Try {
            val networkAvgTravelTime: String = getNetworkAvgTravelTime(config.experimentInstanceDirectory)
            val populationTravelTime: String = getPopulationAvgTravelTime(config.experimentInstanceDirectory)

            logWithSnapshotFile ++ Map(
              "experiment.result.traveltime.avg.network" -> networkAvgTravelTime,
              "experiment.result.traveltime.avg.population" -> populationTravelTime
            )
          }
        }

      ExperimentStepOps.resolveTry(t, Some(name))
    }
  }


  /**
    * runs a MATSim simulation, generating a single snapshot file at termination
    * @param currentDirectory base directory of this experiment instance, which should be populated with the config, network, and population assets
    * @param startTime the start of day in simulation time
    * @param endTime the time in simulation time to end, which defaults to the value LocalTime.MAX
    * @return
    */
  def MATSimRun (currentDirectory: String, startTime: LocalTime, endTime: LocalTime, timeWindow: Int): String = {

    val matsimOutputDirectory: String = s"$currentDirectory/matsim"
    Files.createDirectories(Paths.get(matsimOutputDirectory))

    val matsimConfig: Config = ConfigUtils.loadConfig(s"$currentDirectory/config.xml")

    matsimConfig.controler().setOutputDirectory(matsimOutputDirectory)
    val scenario: Scenario = ScenarioUtils.loadScenario(matsimConfig)
    val controler: Controler = new Controler(matsimConfig)


    def run(): String = {
      val networkLinks = MATSimNetworkToCollection(s"$currentDirectory/network.xml")
      var currentNetworkState: SnapshotCollector = NewNetworkAnalyticStateCollector(networkLinks, BPRCostFunctionType, timeWindow)
      var currentIteration: Int = 1
      val timeTracker: TimeTracker = TimeTracker(startTime.format(ExperimentFSOps.HHmmssFormat), endTime.format(ExperimentFSOps.HHmmssFormat))

      // add the events handlers
      controler.addOverridingModule(new AbstractModule(){
        @Override def install (): Unit = {
          this.addEventHandlerBinding().toInstance(new SnapshotEventHandler({
            case LinkEventData(e) =>
              val belongsToTimeGroup = timeTracker.belongsToThisTimeGroup(e)
              if (belongsToTimeGroup)
                synchronized {
                  currentNetworkState = NewNetworkAnalyticStateCollector.update(currentNetworkState, e)
                }
            case NewIteration(i) =>
              currentIteration = i
            case o =>
              println("other link data died here")
          }))
        }
      })

      //start the simulation
      controler.run()

      // write snapshot and return filename
      NewNetworkAnalyticStateCollector.toXMLFile(currentDirectory, currentNetworkState) match {
        case Success(file) => file
        case Failure(e) => throw e
      }
    }

    run()
  }

  /**
    * grabs the MATSim-generated average trip duration value
    * @param instanceDirectory path to the experiment instance directory
    * @return the average trip duration value, or an explanation as to why we couldn't find it
    */
  def getPopulationAvgTravelTime(instanceDirectory: String): String = {
    val tripDurationsRelativePath = "matsim/ITERS/it.0/0.tripdurations.txt"
    val path = s"$instanceDirectory/$tripDurationsRelativePath"
    val regex: Regex = ".*average trip duration: (\\d+.\\d*).*".r

    Try {
      Source.fromFile(path).getLines.mkString
    } match {
      case Success(tripDurationsFile) =>
        tripDurationsFile match {
          case regex(g0) => g0
          case _ => "file found but value not found in file"
        }
      case Failure(e) => s"attempting to load population avg travel time, could not find file $path. ${e.getMessage}"
    }
  }

  /**
    * scrape the network average travel time from the snapshot output file
    * @param instanceDirectory path to the experiment instance directory
    * @return the average network link travel time, or an explanation as to why we couldn't find it
    */
  def getNetworkAvgTravelTime(instanceDirectory: String): String = {
    val path = s"$instanceDirectory/snapshot.xml"

    Try {
      XML.loadFile(path)
    } match {
      case Success(snapshotOutputFile) =>
        Try {
          (snapshotOutputFile \ "global" \ "@avgtraveltime").text
        } match {
          case Success(avgTravelTime) => avgTravelTime
          case Failure(e) => s"found snapshot output file but could not parse root\\global@avgTravelTime. ${e.getMessage}"
        }
      case Failure(e) => s"attempting to load network avg travel time, could not find file $path. ${e.getMessage}"
    }
  }
}
