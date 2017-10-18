package cse.fitzgero.sorouting.app

import java.time.LocalTime

import cse.fitzgero.sorouting.algorithm.routing.localgraph._
import cse.fitzgero.sorouting.matsimrunner._
import cse.fitzgero.sorouting.matsimrunner.network.MATSimNetworkToCollection
import cse.fitzgero.sorouting.matsimrunner.population._
import cse.fitzgero.sorouting.roadnetwork.costfunction.BPRCostFunction
import cse.fitzgero.sorouting.util._
import cse.fitzgero.sorouting.util.implicits._


object SORoutingLocalGraphInlineApplication03 extends App with ClassLogging {

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


  logger.info("running 100% UE simulation")
  //----------------------------------------------------------------------------------------------
  //  1. Run 100% UE Simulation, get overall congestion (measure?)
  //----------------------------------------------------------------------------------------------
  val overallNumberOfTrips: Int = {
    val routingResultUE: LocalGraphUERoutingModuleResult = LocalGraphRoutingUEModule.routeAllRequestedTimeGroups(conf, fileHelper, populationFull)
    fileHelper.savePopulation(routingResultUE.population, FullUEExp, FullUEPopulation)
    routingResultUE.routeCountUE
  }

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

  logger.info("running our algorithm")
  //----------------------------------------------------------------------------------------------
  //  2. For each snapshot, load and run our algorithm
  //----------------------------------------------------------------------------------------------
  val (logs, routeCountUE, routeCountSO) = {
    val routingResult: LocalGraphRoutingModule03Result = LocalGraphRoutingUESOModule03.routeAllRequestedTimeGroups(conf, fileHelper, populationFull)
    fileHelper.savePopulation(routingResult.population, CombinedUESOExp, CombinedUESOPopulation)


    val analytics = routingResult.logs.analytic
    val algorithmLogger = AuxLogger.get("algorithm")

    val totalRoutes = analytics.getOrZero("algorithm.global.totalroutes")
//    val altRoutesFound = analytics.getOrZero("algorithm.analytics.selection.altroutesfound")
//    val choseOriginalRoute = analytics.getOrZero("algorithm.analytics.selection.choseoriginal")
//    val selectionOriginalCost = analytics.getOrZero("algorithm.analytics.selection.originalcost.value")
//    val selectionSORoutesCost = analytics.getOrZero("algorithm.analytics.selection.socost.value")

    algorithmLogger.info("total routes (count)", totalRoutes)
//    logger.info("ksp algorithm total alt routes found (count)", altRoutesFound)
//    logger.info("ksp algorithm average alt routes found (count)", altRoutesFound / totalRoutes)

    (routingResult.logs, routingResult.routeCountUE, routingResult.routeCountSO)
  }

  logger.info("running UESO simulation")
  //----------------------------------------------------------------------------------------------
  //  3. Run 1-p% UE UNION p% SO Simulation, get overall congestion (measure?)
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

  logger.info("analyzing results")
  //----------------------------------------------------------------------------------------------
  //  4. Analyze Results
  //----------------------------------------------------------------------------------------------
  fileHelper.appendToReportFile(PrintToResultFile1(
    conf.populationSize,
    overallNumberOfTrips,
    routeCountUE,
    routeCountSO,
    (conf.routePercentage * 100).toInt,
    conf.timeWindow,
    List(0L), // runTimes.ksp,
    List(0L), // runTimes.fw,
    List(0L), // runTimes.selection,
    List(0L), // runTimes.overall,
    fileHelper.getPopulationAvgTravelTime(FullUEExp).getOrElse(-1D),
    fileHelper.getPopulationAvgTravelTime(CombinedUESOExp).getOrElse(-1D),
    fileHelper.getNetworkAvgTravelTime(FullUEExp).getOrElse(-1D),
    fileHelper.getNetworkAvgTravelTime(CombinedUESOExp).getOrElse(-1D)
  ))

  AuxLogger.setPath("algorithm", fileHelper.auxLoggingFileDirectory)
  AuxLogger.get("algorithm").writeLog()
}
