package cse.fitzgero.sorouting.app

import com.typesafe.config._
import cse.fitzgero.sorouting.algorithm.pathsearch.ksp.{KSPBounds, PathsFoundBounds, TimeBounds}
import cse.fitzgero.sorouting.algorithm.flowestimation.{FWBounds, IterationFWBounds, RelativeGapFWBounds, RunningTimeFWBounds}
import cse.fitzgero.sorouting.util._
import org.apache.log4j.Level
import org.rogach.scallop._


case class SORoutingApplicationConfig (
  configFilePath: String,
  networkFilePath: String,
  outputDirectory: String,
  processes: Parallelism,
  timeWindow: Int,
  k: Int,
  kspBounds: KSPBounds,
  fwBounds: FWBounds,
  populationSize: Int,
  routePercentage: Double,
  startTime: String,
  endTime: String
)

object SORoutingApplicationConfig {
  def apply(args: Seq[String]): SORoutingApplicationConfig = {

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

    // Algorithm configuration
    val timeWindow: Int = config.getInt("soRouting.algorithm.timeWindow")
    val k: Int = config.getInt("soRouting.algorithm.ksp.k")
    val kspBounds: KSPBounds = config.getString("soRouting.algorithm.ksp.kspBoundsType") match {
      case "pathsfound" => PathsFoundBounds(config.getInt("soRouting.algorithm.ksp.kspBoundsValue"))
      case "time" => TimeBounds(config.getInt("soRouting.algorithm.ksp.kspBoundsValue"))
      case _ => throw new ConfigException.NotResolved("soRouting.algorithm.ksp.kspBounds{Type|Value} should be defined in application.conf")
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

    SORoutingApplicationConfig(
      configFile,
      networkFile,
      outputDirectory,
      processes,
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

    // REQUIRED
    val config = opt[String](descr = "MATSim config.xml file")
    val network = opt[String](descr = "MATSim network.xml file")
    val dest = opt[String](descr = "directory to write results")
    val win = opt[Int](validate = {_ > 0}, descr = "algorithm batch window duration, in seconds")
    val pop = opt[Int](validate = {_ > 0}, descr = "population size (in number of people, which may differ from the total number of trips)")
    val route = opt[Double](validate = (r) => {0.0D <= r && r <= 1.0D}, descr = "% of population to route using our routing algorithm, in range [0.0, 1.0]")

    // OPTIONAL
    val k = opt[Int](required = false, descr = "number of alternate shortest paths to discover per driver")
    val ksptype = opt[String](required = false, descr = "limit the k-shortest paths by {pathsfound|time}")
    val kspvalue = opt[String](required = false, descr = "value noting the upper limit on paths found or the duration (in seconds)")
    val fwtype = opt[String](required = false, descr = "limit the frank-wolfe estimation by {iteration|time|relgap}")
    val fwvalue = opt[String](required = false, descr = "value noting the upper limit in terms of # iterations, time (in seconds), or error")

    verify()
  }

  /**
    * takes application input parameters and sets them to system properties
    * typesafe config expects these values in the environment
    * @param conf a Scallop Config class that identifies application parameters for SoRouting
    */
  def setSystemPropertiesFromConf(conf: Conf): Unit = {

    // REQUIRED
    System.setProperty("soRouting.application.configFile", conf.config())
    System.setProperty("soRouting.application.networkFile", conf.network())
    System.setProperty("soRouting.application.outputDirectory", conf.dest())
    System.setProperty("soRouting.algorithm.timeWindow", conf.win().toString)
    System.setProperty("soRouting.population.size", conf.pop().toString)
    System.setProperty("soRouting.population.routePercentage", conf.route().toString)

    // OPTIONAL
    conf.k.toOption match {
      case Some(k) => System.setProperty("soRouting.algorithm.ksp.k", k.toString)
      case None =>
    }
    conf.ksptype.toOption match {
      case Some(t) => conf.kspvalue.toOption match {
        case Some(v) =>
          System.setProperty("soRouting.algorithm.ksp.kspBoundsType", t)
          System.setProperty("soRouting.algorithm.ksp.kspBoundsValue", v)
        case None =>
      }
      case None =>
    }
    conf.fwtype.toOption match {
      case Some(t) => conf.fwvalue.toOption match {
        case Some(v) =>
          System.setProperty("soRouting.algorithm.fwBoundsType", t)
          System.setProperty("soRouting.algorithm.fwBoundsValue", v)
        case None =>
      }
      case None =>
    }
  }
}
