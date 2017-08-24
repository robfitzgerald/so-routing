package cse.fitzgero.sorouting.matsimrunner


import scala.collection.JavaConverters._
import scala.util.{Failure, Success}

import org.matsim.api.core.v01.network.Link
import org.matsim.api.core.v01.{Id, Scenario}
import org.matsim.core.config.{Config, ConfigUtils}
import org.matsim.core.controler.{AbstractModule, Controler}
import org.matsim.core.scenario.ScenarioUtils

import cse.fitzgero.sorouting.app.MATSimSimulator
import cse.fitzgero.sorouting.matsimrunner.snapshot._


class MATSimSingleSnapshotRunnerModule (matsimConfig: MATSimRunnerConfig) extends MATSimSimulator {

//  println(matsimConfig.toString)

  val matsimOutputDirectory: String = s"${matsimConfig.outputDirectory}/matsim"
  val snapshotOutputDirectory: String = s"${matsimConfig.outputDirectory}/snapshot"

  val config: Config = ConfigUtils.loadConfig(matsimConfig.matsimConfigFile)

  config.controler().setOutputDirectory(matsimOutputDirectory)
  val scenario: Scenario = ScenarioUtils.loadScenario(config)
  val controler: Controler = new Controler(config)


  def run(): String = {
    val networkLinks: scala.collection.mutable.Map[Id[Link], _] = scenario.getNetwork.getLinks.asScala
    var currentNetworkState: NetworkStateCollector = NetworkStateCollector(networkLinks)
    var currentIteration: Int = 1
    val timeTracker: TimeTracker = TimeTracker(matsimConfig.startTime, matsimConfig.endTime)


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
    suppressMATSimInfoLogging()
    controler.run()

    // write snapshot and return filename
    NetworkStateCollector.toXMLFile(snapshotOutputDirectory, currentNetworkState) match {
      case Success(file) => file
      case Failure(e) => throw e
    }
  }

  val filePath: String = run()
}

object MATSimSingleSnapshotRunnerModule {
  def apply(conf: MATSimRunnerConfig): MATSimSingleSnapshotRunnerModule = new MATSimSingleSnapshotRunnerModule(conf)
}