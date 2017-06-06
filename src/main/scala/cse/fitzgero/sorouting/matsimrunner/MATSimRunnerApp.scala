package cse.fitzgero.sorouting.matsimrunner

import org.matsim.api.core.v01.Scenario
import org.matsim.core.config.Config
import org.matsim.core.config.ConfigUtils
import org.matsim.core.controler.AbstractModule
import org.matsim.core.controler.Controler
import org.matsim.core.controler.ControlerUtils
import org.matsim.core.scenario.ScenarioUtils

class MATSimRunnerApp extends App {

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
    var timeTracker: TimeTracker = TimeTracker(appConfig.window, appConfig.startTime, appConfig.endTime)

    // add the events handlers
    controler.addOverridingModule(new AbstractModule(){
      @Override def install(): Unit = {
        this.addEventHandlerBinding().toInstance(new SnapshotEventHandler({
          // TODO: hey, maybe you want to abstract this out? would require making addDriver/removeDriver into "update" again..
            case LinkEnterData(time, link, vehicle) => {
              if (timeTracker.belongsToThisTimeGroup(time)) // TODO: expects an Event object, not an Int
                currentNetworkState = currentNetworkState.addDriver(link, vehicle)
              else {
                // TODO: write snapshot(s) to file (there could be a few windows where nothing has changed)

                timeTracker = timeTracker.advance

                currentNetworkState = currentNetworkState.addDriver(link, vehicle)
              }
            }
            case LinkLeaveData(time, link, vehicle) => {
              if (timeTracker.belongsToThisTimeGroup(time)) // TODO: expects an Event object, not an Int
                currentNetworkState = currentNetworkState.removeDriver(link, vehicle)
              else {
                // TODO: write snapshot(s) to file (there could be a few windows where nothing has changed)

                timeTracker = timeTracker.advance

                currentNetworkState = currentNetworkState.removeDriver(link, vehicle)
              }
            }
            case NewIteration(i) => {
              println(s"start new iteration $i")

              // TODO: make sure everything's written

              // start over
              timeTracker = TimeTracker(appConfig.window, appConfig.startTime, appConfig.endTime)
              currentNetworkState = NetworkStateCollector()

              // TODO: open new directory
            }
          }))
      }
    })

    // TODO: open first file here

    //call run() to start the simulation
    controler.run()

  }
}
