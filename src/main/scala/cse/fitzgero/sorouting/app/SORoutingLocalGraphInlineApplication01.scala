package cse.fitzgero.sorouting.app

import java.time.LocalTime

import cse.fitzgero.sorouting.matsimrunner._
import cse.fitzgero.sorouting.matsimrunner.population._
import cse.fitzgero.sorouting.algorithm.routing.localgraph.{LocalGraphRoutingUESOModule01, LocalGraphRoutingModuleResult}
import cse.fitzgero.sorouting.matsimrunner.network.MATSimNetworkToCollection
import cse.fitzgero.sorouting.matsimrunner.util.GenerateSelfishPopulationFile
import cse.fitzgero.sorouting.roadnetwork.costfunction.BPRCostFunction
import cse.fitzgero.sorouting.util._
import cse.fitzgero.sorouting.util.implicits._




object SORoutingLocalGraphInlineApplication01 extends App with ClassLogging {

  val conf: SORoutingApplicationConfig = SORoutingApplicationConfig(args)
  logger.info(conf.toString)

  val fileHelper = SORoutingFilesHelper(conf)
  val networkData = MATSimNetworkToCollection(fileHelper.thisNetworkFilePath)

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


  //----------------------------------------------------------------------------------------------
  //  1. Run 100% UE Simulation, get overall congestion (measure?)
  //----------------------------------------------------------------------------------------------
  val overallNumberOfTrips: Int = GenerateSelfishPopulationFile(populationFull, conf, fileHelper)
  MATSimSingleAnalyticSnapshotRunnerModule(
    MATSimRunnerConfig(
      fileHelper.finalConfigFilePath(FullUEExp),
      fileHelper.experimentPath(FullUEExp),
      conf.timeWindow,
      conf.startTime,
      conf.endTime,
      ArgsNotMissingValues
    ),
    networkData,
    BPRCostFunction
  )

  //----------------------------------------------------------------------------------------------
  //  3. For each snapshot, load and run our algorithm
  //----------------------------------------------------------------------------------------------
  val routingResult: LocalGraphRoutingModuleResult = LocalGraphRoutingUESOModule01.routeAllRequestedTimeGroups(conf, fileHelper, populationFull)
  fileHelper.savePopulation(routingResult.population, CombinedUESOExp, CombinedUESOPopulation)

  //----------------------------------------------------------------------------------------------
  //  4. Run 1-p% UE UNION p% SO Simulation, get overall congestion (measure?)
  //----------------------------------------------------------------------------------------------
  MATSimSingleAnalyticSnapshotRunnerModule(
    MATSimRunnerConfig(
      fileHelper.finalConfigFilePath(CombinedUESOExp),
      fileHelper.experimentPath(CombinedUESOExp),
      conf.timeWindow,
      conf.startTime,
      conf.endTime,
      ArgsNotMissingValues
    ),
    networkData,
    BPRCostFunction
  )

  //----------------------------------------------------------------------------------------------
  //  5. Analyze Results
  //----------------------------------------------------------------------------------------------
  fileHelper.appendToReportFile(PrintToResultFile(
    conf.populationSize,
    overallNumberOfTrips,
    routingResult.routeCountUE,
    routingResult.routeCountSO,
    (conf.routePercentage * 100).toInt,
    conf.timeWindow,
    routingResult.runTimes.ksp,
    routingResult.runTimes.fw,
    routingResult.runTimes.selection,
    routingResult.runTimes.overall,
    fileHelper.getPopulationAvgTravelTime(FullUEExp).getOrElse(-1D),
    fileHelper.getPopulationAvgTravelTime(CombinedUESOExp).getOrElse(-1D),
    fileHelper.getNetworkAvgTravelTime(FullUEExp).getOrElse(-1D),
    fileHelper.getNetworkAvgTravelTime(CombinedUESOExp).getOrElse(-1D)
  ))
}
