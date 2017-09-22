package cse.fitzgero.sorouting.matsimrunner

import cse.fitzgero.sorouting.util.ClassLogging
import org.matsim.api.core.v01.{Id, Scenario}
import org.matsim.core.config.{Config, ConfigUtils}
import org.matsim.core.controler.{AbstractModule, Controler}
import org.matsim.core.scenario.ScenarioUtils

class MATSimRunnerModule (matsimConfig: MATSimRunnerConfig) extends MATSimSimulator with ClassLogging {
  logger.debug(matsimConfig.toString)

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

  run()
}

object MATSimRunnerModule {
  def apply(matsimConfig: MATSimRunnerConfig): MATSimRunnerModule = new MATSimRunnerModule(matsimConfig)
}
