package cse.fitzgero.sorouting.matsimrunner

import org.matsim.api.core.v01.{Id, Scenario}
import org.matsim.core.config.{Config, ConfigUtils}
import org.matsim.core.controler.{AbstractModule, Controler}
import org.matsim.core.scenario.ScenarioUtils

class MATSimRunnerModule (matsimConfig: MATSimRunnerConfig) {
  // example AppConfig("examples/tutorial/programming/example7-config.xml", "output/example7", "5", "06:00:00", "07:00:00", ArgsNotMissingValues)

  val matsimOutputDirectory: String = s"${matsimConfig.outputDirectory}"

  val config: Config = ConfigUtils.loadConfig(matsimConfig.matsimConfigFile)
  config.controler().setOutputDirectory(matsimOutputDirectory)
  val scenario: Scenario = ScenarioUtils.loadScenario(config)
  val controler: Controler = new Controler(config)


  def run(): String = {
    //start the simulation
    controler.run()

    // return directory location of output (matsim output and snapshot output)
    matsimConfig.outputDirectory
  }
}

object MATSimRunnerModule {
  def apply(matsimConfig: MATSimRunnerConfig): MATSimRunnerModule = new MATSimRunnerModule(matsimConfig)
}
