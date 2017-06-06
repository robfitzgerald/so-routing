package cse.fitzgero.sorouting.matsimrunner

import org.matsim.api.core.v01.Scenario
import org.matsim.core.config.Config
import org.matsim.core.config.ConfigUtils
import org.matsim.core.controler.AbstractModule
import org.matsim.core.controler.Controler
import org.matsim.core.controler.ControlerUtils
import org.matsim.core.scenario.ScenarioUtils

class MATSimRunnerApp extends App {
  val outputDirectory: String = "output/example7" ;

//  ControlerUtils.initializeOutputLogging();

  var config: Config = {
    if (args.isEmpty)
      ConfigUtils.loadConfig( "examples/tutorial/programming/example7-config.xml" )
    else ConfigUtils.loadConfig(args(0))
  }
  config.controler().setOutputDirectory(outputDirectory)
  val scenario: Scenario = ScenarioUtils.loadScenario(config)
  val controler: Controler = new Controler(config)

  var currentNetworkState: NetworkStateCollector = NetworkStateCollector()


  // add the events handlers
  controler.addOverridingModule(new AbstractModule(){
    @Override def install(): Unit = {
      this.addEventHandlerBinding().toInstance( new SnapshotEventHandler((event: SnapshotEventData) => {
        currentNetworkState = currentNetworkState.update(event)
      }))
    }
  })

  //call run() to start the simulation
  controler.run()
}
