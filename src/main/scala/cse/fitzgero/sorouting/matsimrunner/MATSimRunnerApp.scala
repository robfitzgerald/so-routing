package cse.fitzgero.sorouting.matsimrunner

import java.io.{File, PrintWriter}

import cse.fitzgero.sorouting.util.TimeStringConvert
import org.matsim.api.core.v01.Scenario
import org.matsim.core.config.Config
import org.matsim.core.config.ConfigUtils
import org.matsim.core.controler.AbstractModule
import org.matsim.core.controler.Controler
import org.matsim.core.controler.ControlerUtils
import org.matsim.core.scenario.ScenarioUtils

object MATSimRunnerApp extends App {

  val ArgsMissingValues = true
  val ArgsNotMissingValues = false
  case class AppConfig(inputDirectory: String = "", outputDirectory: String = "", window: String = "", startTime: String = "", endTime: String = "", incomplete: Boolean = ArgsMissingValues)
  // example AppConfig("examples/tutorial/programming/example7-config.xml", "output/example7", "5", "06:00:00", "07:00:00", ArgsNotMissingValues)
  val appConfig: AppConfig = args match {
    case Array(in, out, wD, sT, eT) => AppConfig(in, out, wD, sT, eT, ArgsNotMissingValues)
    case _ => AppConfig()
  }

  if (appConfig.incomplete) {
    println(s"usage: ")
  } else {
    //  ControlerUtils.initializeOutputLogging();
    val config: Config = ConfigUtils.loadConfig(appConfig.inputDirectory)
    config.controler().setOutputDirectory(appConfig.outputDirectory)
    val scenario: Scenario = ScenarioUtils.loadScenario(config)
    val controler: Controler = new Controler(config)

    var currentNetworkState: NetworkStateCollector = NetworkStateCollector()
    var currentIteration: Int = 1
    var timeTracker: TimeTracker = TimeTracker(appConfig.window, appConfig.startTime, appConfig.endTime)


    // add the events handlers
    controler.addOverridingModule(new AbstractModule(){
      @Override def install (): Unit = {
        this.addEventHandlerBinding().toInstance(new SnapshotEventHandler({
          case LinkEventData(e) =>
            if (timeTracker.belongsToThisTimeGroup(e))
              currentNetworkState = currentNetworkState.update(e)
            else {
              NetworkStateCollector.toFile(
                appConfig.outputDirectory,
                currentIteration,
                timeTracker.currentTimeString,
                currentNetworkState
              )

              timeTracker = timeTracker.advance
              currentNetworkState = currentNetworkState.update(e)
            }

            case NewIteration(i) => {
              println(s"start new iteration $i")

              // TODO: make sure everything's written

              // start over
              timeTracker = TimeTracker(appConfig.window, appConfig.startTime, appConfig.endTime)
              currentNetworkState = NetworkStateCollector()
              currentIteration = i
              // TODO: open new directory
            }
          }))
      }
    })

    // get the first file saving directory created

    //call run() to start the simulation
    controler.run()

  }
}

