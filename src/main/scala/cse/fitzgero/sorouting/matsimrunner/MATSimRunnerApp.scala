package cse.fitzgero.sorouting.matsimrunner

import scala.collection.JavaConverters._

import org.matsim.core.config.Config
import org.matsim.core.config.ConfigUtils
import org.matsim.core.controler.Controler
import org.matsim.core.controler.ControlerUtils
import org.matsim.core.controler.AbstractModule
import org.matsim.core.scenario.ScenarioUtils
import org.matsim.api.core.v01.{Id, Scenario}
import org.matsim.api.core.v01.network.Link

object MATSimRunnerApp extends App {
  val ArgsMissingValues = true
  val ArgsNotMissingValues = false
  case class AppConfig(inputDirectory: String = "", outputDirectory: String = "", window: String = "", startTime: String = "", endTime: String = "", incomplete: Boolean = ArgsMissingValues)
  // example AppConfig("examples/tutorial/programming/example7-config.xml", "output/example7", "5", "06:00:00", "07:00:00", ArgsNotMissingValues)
  val appConfig: AppConfig = args match {
    case Array(in, out, wD, sT, eT) => AppConfig(in, out, wD, sT, eT, ArgsNotMissingValues)
    case _ => AppConfig()
  }
  val matsimOutputDirectory: String = s"${appConfig.outputDirectory}/matsim"
  val snapshotOutputDirectory: String = s"${appConfig.outputDirectory}/snapshot"

  if (appConfig.incomplete) {
    println(s"usage: ")
  } else {
    //  ControlerUtils.initializeOutputLogging();
    val config: Config = ConfigUtils.loadConfig(appConfig.inputDirectory)
    config.controler().setOutputDirectory(matsimOutputDirectory)
    val scenario: Scenario = ScenarioUtils.loadScenario(config)
    val controler: Controler = new Controler(config)

    val networkLinks: scala.collection.mutable.Map[Id[Link], _] = scenario.getNetwork.getLinks.asScala
    var currentNetworkState: NetworkStateCollector = NetworkStateCollector(networkLinks)
    var currentIteration: Int = 1
    var timeTracker: TimeTracker = TimeTracker(appConfig.window, appConfig.startTime, appConfig.endTime)


    // add the events handlers
    controler.addOverridingModule(new AbstractModule(){
      @Override def install (): Unit = {
        this.addEventHandlerBinding().toInstance(new SnapshotEventHandler({
          case LinkEventData(e) =>
            if (!timeTracker.isDone && e.time >= timeTracker.currentTimeGroup) {
              if (timeTracker.belongsToThisTimeGroup(e))
                currentNetworkState = currentNetworkState.update(e)
              else {
                NetworkStateCollector.toFile(
                  snapshotOutputDirectory,
                  currentIteration,
                  timeTracker.currentTimeString,
                  currentNetworkState
                )

                while(!timeTracker.belongsToThisTimeGroup(e))
                  timeTracker = timeTracker.advance

                currentNetworkState = currentNetworkState.update(e)
              }
            }

            case NewIteration(i) =>
              println(s"start new iteration $i")

              // make sure everything's written from the last
              NetworkStateCollector.toFile(
                snapshotOutputDirectory,
                currentIteration,
                s"${timeTracker.currentTimeString}-final",
                currentNetworkState
              )

              // start next iteration
              timeTracker = TimeTracker(appConfig.window, appConfig.startTime, appConfig.endTime)
              currentNetworkState = NetworkStateCollector(networkLinks)
              currentIteration = i

          }))
      }
    })

    // get the first file saving directory created

    //call run() to start the simulation
    controler.run()

    // handle writing final snapshot
    NetworkStateCollector.toFile(
      snapshotOutputDirectory,
      currentIteration,
      s"${timeTracker.currentTimeString}-final",
      currentNetworkState
    )
  }
}

