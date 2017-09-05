package cse.fitzgero.sorouting.util

import com.typesafe.config._
import cse.fitzgero.sorouting.algorithm.pathsearch.ksp.{KSPBounds, PathsFoundBounds, TimeBounds}
import cse.fitzgero.sorouting.algorithm.trafficassignment.{IterationFWBounds, RelativeGapFWBounds, RunningTimeFWBounds, FWBounds}
import cse.fitzgero.sorouting.util.convenience._
import org.apache.log4j.Level

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
  def apply(): SORoutingConfig3 = {

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
}
