package cse.fitzgero.sorouting.matsimrunner

import java.time.LocalTime

import scala.collection.JavaConverters._
import org.matsim.api.core.v01.network.Link
import org.matsim.api.core.v01.{Id, Scenario}
import org.matsim.core.config.{Config, ConfigUtils}
import org.matsim.core.controler.{AbstractModule, Controler}
import org.matsim.core.scenario.ScenarioUtils
import cse.fitzgero.sorouting.matsimrunner.snapshot._
import cse.fitzgero.sorouting.matsimrunner.network._
import cse.fitzgero.sorouting.roadnetwork.costfunction.{CostFunction, CostFunctionFactory}
import cse.fitzgero.sorouting.util.Logging

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


class MATSimMultipleAnalyticSnapshotRunnerModule (matsimConfig: MATSimRunnerConfig, networkData: Network, costFunctionFactory: CostFunctionFactory) extends MATSimSimulator with Logging {
  // example AppConfig("examples/tutorial/programming/example7-config.xml", "output/example7", "5", "06:00:00", "07:00:00", ArgsNotMissingValues)

  logger.info(matsimConfig.toString)

  val matsimOutputDirectory: String = s"${matsimConfig.outputDirectory}/matsim"
  val snapshotOutputDirectory: String = s"${matsimConfig.outputDirectory}/snapshot"

  val config: Config = ConfigUtils.loadConfig(matsimConfig.matsimConfigFile)
  config.controler().setOutputDirectory(matsimOutputDirectory)
  val scenario: Scenario = ScenarioUtils.loadScenario(config)
  val controler: Controler = new Controler(config)


  def run(): String = {
    var currentNetworkState: NetworkAnalyticStateCollector = NetworkAnalyticStateCollector(networkData, costFunctionFactory, matsimConfig.window)
    var currentIteration: Int = 1
    var timeTracker: TimeTracker = TimeTracker(matsimConfig.window, matsimConfig.startTime, matsimConfig.endTime)


    // TODO: update analytics in the event handler calls
    controler.addOverridingModule(new AbstractModule(){
      @Override def install (): Unit = {
        this.addEventHandlerBinding().toInstance(new SnapshotEventHandler({
          case LinkEventData(e) =>
            if (!timeTracker.isDone && e.time >= timeTracker.currentTimeGroup) {
              if (timeTracker.belongsToThisTimeGroup(e))
                currentNetworkState = currentNetworkState.update(e)
              else {
                val writerData: WriterData = WriterData(snapshotOutputDirectory, currentIteration, timeTracker.currentTimeStringFS)
                NetworkAnalyticStateCollector.toXMLFile(writerData, currentNetworkState)

                while(!timeTracker.belongsToThisTimeGroup(e)) {
                  val writerData: WriterData = WriterData(snapshotOutputDirectory, currentIteration, timeTracker.currentTimeStringFS)
                  NetworkAnalyticStateCollector.toXMLFile(writerData, currentNetworkState)
                  timeTracker = timeTracker.advance
                }

                currentNetworkState = currentNetworkState.update(e)
              }
            }

          case NewIteration(i) =>
            // make sure everything's written from the last
            val writerData: WriterData = WriterData(snapshotOutputDirectory, currentIteration, s"${timeTracker.currentTimeStringFS}-final")
            NetworkAnalyticStateCollector.toXMLFile(writerData, currentNetworkState)

            // start next iteration
            timeTracker = TimeTracker(matsimConfig.window, matsimConfig.startTime, matsimConfig.endTime)
            currentNetworkState = NetworkAnalyticStateCollector(networkData, costFunctionFactory, matsimConfig.window)
            currentIteration = i
        }))
      }
    })



    //start the simulation
    suppressMATSimInfoLogging()
    controler.run()

    // handle writing final snapshot
    val writerData: WriterData = WriterData(snapshotOutputDirectory, currentIteration, s"${timeTracker.currentTimeStringFS}-final")
    NetworkAnalyticStateCollector.toXMLFile(writerData, currentNetworkState)

    // return directory location of output (matsim output and snapshot output)
    matsimConfig.outputDirectory
  }

  run()
}

object MATSimMultipleAnalyticSnapshotRunnerModule {
  def apply(conf: MATSimRunnerConfig, networkData: Network, costFunctionFactory: CostFunctionFactory): MATSimMultipleAnalyticSnapshotRunnerModule = new MATSimMultipleAnalyticSnapshotRunnerModule(conf, networkData, costFunctionFactory)
}