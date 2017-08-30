package cse.fitzgero.sorouting.matsimrunner

import java.time.LocalTime

import cse.fitzgero.sorouting.app.MATSimSimulator

import scala.collection.JavaConverters._
import scala.util.{Failure, Success}

import org.matsim.api.core.v01.network.Link
import org.matsim.api.core.v01.{Id, Scenario}
import org.matsim.core.config.{Config, ConfigUtils}
import org.matsim.core.controler.{AbstractModule, Controler}
import org.matsim.core.scenario.ScenarioUtils

import cse.fitzgero.sorouting.matsimrunner.snapshot._
import cse.fitzgero.sorouting.matsimrunner.network._
import cse.fitzgero.sorouting.roadnetwork.costfunction.{CostFunction, CostFunctionFactory}


// should collect more interesting information about events.
// for each snapshot,
//   for each network link,
//     linkId, vehicles on link, link cost, min cost, max cost, mean cost
// aggregated to an overall network analysis
//   for each network link,
//     linkId, total vehicles traveled, min cost, max cost, mean cost
// for each driver,
//   personId, UE travel time, UE distance, UESO travel time, UESO distance
// aggregated to an overall population analysis


class MATSimSingleAnalyticSnapshotRunnerModule (matsimConfig: MATSimRunnerConfig, networkData: Network, costFunctionFactory: CostFunctionFactory) extends MATSimSimulator {

  //  println(matsimConfig.toString)

  val matsimOutputDirectory: String = s"${matsimConfig.outputDirectory}/matsim"
  val snapshotOutputDirectory: String = s"${matsimConfig.outputDirectory}/snapshot"

  val config: Config = ConfigUtils.loadConfig(matsimConfig.matsimConfigFile)
  config.controler().setOutputDirectory(matsimOutputDirectory)
  val scenario: Scenario = ScenarioUtils.loadScenario(config)
  val controler: Controler = new Controler(config)


  def run(): String = {
    var currentNetworkState: NetworkAnalyticStateCollector = NetworkAnalyticStateCollector(networkData, costFunctionFactory, matsimConfig.window)
    var currentIteration: Int = 1
    val timeTracker: TimeTracker = TimeTracker(matsimConfig.startTime, matsimConfig.endTime)


    // TODO: update analytics in the event handler calls
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
    NetworkAnalyticStateCollector.toXMLFile(snapshotOutputDirectory, currentNetworkState) match {
      case Success(file) => file
      case Failure(e) => throw e
    }
  }

  val filePath: String = run()
}

object MATSimSingleAnalyticSnapshotRunnerModule {
  def apply(conf: MATSimRunnerConfig, networkData: Network, costFunctionFactory: CostFunctionFactory): MATSimSingleAnalyticSnapshotRunnerModule = new MATSimSingleAnalyticSnapshotRunnerModule(conf, networkData, costFunctionFactory)
}