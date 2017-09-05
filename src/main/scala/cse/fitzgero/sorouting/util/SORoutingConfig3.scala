package cse.fitzgero.sorouting.util

import com.typesafe.config._
import org.rogach.scallop._
import org.apache.log4j.Level

import cse.fitzgero.sorouting.algorithm.pathsearch.ksp.{KSPBounds, PathsFoundBounds, TimeBounds}
import cse.fitzgero.sorouting.algorithm.trafficassignment.{FWBounds, IterationFWBounds, RelativeGapFWBounds, RunningTimeFWBounds}
import cse.fitzgero.sorouting.util.convenience._


case class SORoutingConfig3(
  configFilePath: String,
  networkFilePath: String,
  outputDirectory: String,
  processes: Parallelism,
  logging: Level,
  timeWindow: Int,
  k: Int,
  kspBounds: KSPBounds,
  fwBounds: FWBounds,
  populationSize: Int,
  routePercentage: Double,
  startTime: String,
  endTime: String
)

object SORoutingConfig3 {
  def apply(args: Seq[String]): SORoutingConfig3 = {

    setSystemPropertiesFromConf(new Conf(args))

    val config: Config = ConfigFactory.load()

    // Application-level configuration
    val configFile: String = config.getString("soRouting.application.configFile")
    val networkFile: String = config.getString("soRouting.application.networkFile")
    val outputDirectory: String = config.getString("soRouting.application.outputDirectory")
    val processes: Parallelism = config.getString("soRouting.application.processes") match {
      case "*" => AllProcs
      case "1" => OneProc
      case x: String => NumProcs(x.toInt)
    }
    val logging: Level = Level.toLevel(config.getString("soRouting.application.logging"))

    // Algorithm configuration
    val timeWindow: Int = config.getInt("soRouting.algorithm.timeWindow")
    val k: Int = config.getInt("soRouting.algorithm.k")
    val kspBounds: KSPBounds = config.getString("soRouting.algorithm.kspBoundsType") match {
      case "pathsfound" => PathsFoundBounds(config.getInt("soRouting.algorithm.kspBoundsValue"))
      case "time" => TimeBounds(config.getInt("soRouting.algorithm.kspBoundsValue"))
      case _ => throw new ConfigException.NotResolved("soRouting.algorithm.kspBounds{Type|Value} should be defined in application.conf")
    }
    val fwBounds: FWBounds = config.getString("soRouting.algorithm.fwBoundsType") match {
      case "iteration" => IterationFWBounds(config.getInt("soRouting.algorithm.fwBoundsValue"))
      case "time" => RunningTimeFWBounds(config.getInt("soRouting.algorithm.fwBoundsValue"))
      case "relgap" => RelativeGapFWBounds(config.getInt("soRouting.algorithm.fwBoundsValue"))
      case _ => throw new ConfigException.NotResolved("soRouting.algorithm.fwBounds{Type|Value} should be defined in application.conf")
    }

    // Population configuration
    val population = config.getInt("soRouting.population.size")
    val routePercentage = config.getDouble("soRouting.population.routePercentage")
    val startTime = config.getString("soRouting.population.startTime")
    val endTime = config.getString("soRouting.population.endTime")

    SORoutingConfig3(
      configFile,
      networkFile,
      outputDirectory,
      processes,
      logging,
      timeWindow,
      k,
      kspBounds,
      fwBounds,
      population,
      routePercentage,
      startTime,
      endTime
    )
  }

  /**
    * finds application arguments for this experiment
    * @param args the command line arguments for this application
    */
  class Conf(args: Seq[String]) extends ScallopConf(args) {
    val config = opt[String](descr = "MATSim config.xml file")
    val network = opt[String](descr = "MATSim network.xml file")
    val dest = opt[String](descr = "directory to write results")
    val win = opt[Int](descr = "algorithm batch window duration, in seconds")
    val pop = opt[Int](descr = "population size (in number of people, which may differ from the total number of trips)")
    val route = opt[Double](descr = "% of population to route using our routing algorithm, in range [0.0, 1.0)")

    verify()
  }

  /**
    * takes application input parameters and sets them to system properties
    * typesafe config expects these values in the environment
    * @param conf a Scallop Config class that identifies application parameters for SoRouting
    */
  def setSystemPropertiesFromConf(conf: Conf): Unit = {
    System.setProperty("soRouting.application.configFile", conf.config())
    System.setProperty("soRouting.application.networkFile", conf.network())
    System.setProperty("soRouting.application.outputDirectory", conf.dest())
    System.setProperty("soRouting.algorithm.timeWindow", conf.win().toString)
    System.setProperty("soRouting.population.size", conf.pop().toString)
    System.setProperty("soRouting.population.routePercentage", conf.route().toString)
  }
}
