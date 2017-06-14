package cse.fitzgero.sorouting.matsimrunner

import cse.fitzgero.sorouting.matsimrunner.snapshot._
import org.matsim.api.core.v01.network.Link
import org.matsim.api.core.v01.{Id, Scenario}
import org.matsim.core.config.{Config, ConfigUtils}
import org.matsim.core.controler.{AbstractModule, Controler}
import org.matsim.core.scenario.ScenarioUtils

import scala.collection.JavaConverters._

class MATSimRunnerModule (matsimConfig: AppConfig) {
  // example AppConfig("examples/tutorial/programming/example7-config.xml", "output/example7", "5", "06:00:00", "07:00:00", ArgsNotMissingValues)

  val matsimOutputDirectory: String = s"${matsimConfig.outputDirectory}/matsim"
  val snapshotOutputDirectory: String = s"${matsimConfig.outputDirectory}/snapshot"

  val config: Config = ConfigUtils.loadConfig(matsimConfig.inputDirectory)
  config.controler().setOutputDirectory(matsimOutputDirectory)
  val scenario: Scenario = ScenarioUtils.loadScenario(config)
  val controler: Controler = new Controler(config)


  def run(): String = {
    val networkLinks: scala.collection.mutable.Map[Id[Link], _] = scenario.getNetwork.getLinks.asScala
    var currentNetworkState: NetworkStateCollector = NetworkStateCollector(networkLinks)
    var currentIteration: Int = 1
    var timeTracker: TimeTracker = TimeTracker(matsimConfig.window, matsimConfig.startTime, matsimConfig.endTime)


    // add the events handlers
    controler.addOverridingModule(new AbstractModule(){
      @Override def install (): Unit = {
        this.addEventHandlerBinding().toInstance(new SnapshotEventHandler({
          case LinkEventData(e) =>
            if (!timeTracker.isDone && e.time >= timeTracker.currentTimeGroup) {
              if (timeTracker.belongsToThisTimeGroup(e))
                currentNetworkState = currentNetworkState.update(e)
              else {
                val writerData: WriterData = WriterData(snapshotOutputDirectory, currentIteration, timeTracker.currentTimeString)
                NetworkStateCollector.toRawFile(writerData, currentNetworkState)

                while(!timeTracker.belongsToThisTimeGroup(e))
                  timeTracker = timeTracker.advance

                currentNetworkState = currentNetworkState.update(e)
              }
            }

          case NewIteration(i) =>
            // make sure everything's written from the last
            val writerData: WriterData = WriterData(snapshotOutputDirectory, currentIteration, s"${timeTracker.currentTimeString}-final")
            NetworkStateCollector.toRawFile(writerData, currentNetworkState)

            // start next iteration
            timeTracker = TimeTracker(matsimConfig.window, matsimConfig.startTime, matsimConfig.endTime)
            currentNetworkState = NetworkStateCollector(networkLinks)
            currentIteration = i
        }))
      }
    })

    //start the simulation
    controler.run()

    // handle writing final snapshot
    val writerData: WriterData = WriterData(snapshotOutputDirectory, currentIteration, s"${timeTracker.currentTimeString}-final")
    NetworkStateCollector.toRawFile(writerData, currentNetworkState)

    // return directory location of output (matsim output and snapshot output)
    matsimConfig.outputDirectory
  }
}

object MATSimRunnerModule {
  def apply(conf: AppConfig): MATSimRunnerModule = new MATSimRunnerModule(conf)
}