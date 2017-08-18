package cse.fitzgero.sorouting.app

import java.time.LocalTime
import org.apache.log4j.{Level, Logger}

import cse.fitzgero.sorouting.matsimrunner._
import cse.fitzgero.sorouting.matsimrunner.population._
import cse.fitzgero.sorouting.algorithm.routing.localgraphrouting.LocalGraphRoutingModule
import cse.fitzgero.sorouting.util._
import cse.fitzgero.sorouting.util.convenience._



object SORoutingLocalGraphInlineApplication extends App {

//  Logger.getRootLogger.setLevel(Level.WARN)
//  val log = Logger.getLogger(this.getClass)

  val conf: SORoutingApplicationConfig = SORoutingApplicationConfigParseArgs(args)

  println(conf)

  val fileHelper = SORoutingFilesHelper(conf)

  // Create a population from the road network topology
  // from that population, partition it to drivers that will receive our routing and drivers that will not
  val populationFull: PopulationOneTrip =
    PopulationOneTrip
      .generateRandomOneTripPopulation(
        fileHelper.getNetwork,
        RandomPopulationOneTripConfig(
          conf.populationSize,
          Seq(
            ActivityConfig2(
              "home",
              LocalTime.parse("09:00:00") endTime,
              30 minutesDeviation),
            ActivityConfig2(
              "work",
              LocalTime.parse("17:00:00") endTime,
              30 minutesDeviation),
            ActivityConfig2(
              "home",
              LocalTime.MIDNIGHT endTime,
              30 minutesDeviation)
          ),
          Seq(ModeConfig("car"))
        )
      )
  val (populationSO, populationPartial) = populationFull.subsetPartition(conf.routePercentage)


  fileHelper.savePopulation(populationFull, FullUEExp, FullUEPopulation)


  //----------------------------------------------------------------------------------------------
  //  1. Run 100% UE Simulation, get overall congestion (measure?)
  //----------------------------------------------------------------------------------------------
  // TODO: determine how to measure congestion
  MATSimRunnerModule(MATSimRunnerConfig(
    fileHelper.finalConfigFilePath(FullUEExp),
    fileHelper.experimentPath(FullUEExp),
    conf.algorithmTimeWindow,
    conf.startTime,
    conf.endTime,
    ArgsNotMissingValues
  ))


  //----------------------------------------------------------------------------------------------
  //  3. For each snapshot, load and run our algorithm
  //----------------------------------------------------------------------------------------------

  val populationCombinedUESO = LocalGraphRoutingModule.routeAllRequestedTimeGroups(conf, fileHelper, populationFull)

  fileHelper.savePopulation(populationCombinedUESO, CombinedUESOExp, CombinedUESOPopulation)

  //----------------------------------------------------------------------------------------------
  //  4. Run 1-p% UE UNION p% SO Simulation, get overall congestion (measure?)
  //----------------------------------------------------------------------------------------------
  // TODO: determine how to measure congestion
  MATSimRunnerModule(MATSimRunnerConfig(
    fileHelper.finalConfigFilePath(CombinedUESOExp),
    fileHelper.experimentPath(CombinedUESOExp),
    conf.algorithmTimeWindow,
    conf.startTime,
    conf.endTime,
    ArgsNotMissingValues
  ))

  //----------------------------------------------------------------------------------------------
  //  5. Analyze Results (what kinds of analysis?)
  //----------------------------------------------------------------------------------------------
  // TODO: determine how to measure results
}
