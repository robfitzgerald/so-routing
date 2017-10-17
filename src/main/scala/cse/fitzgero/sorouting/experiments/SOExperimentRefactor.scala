package cse.fitzgero.sorouting.experiments

import java.time.LocalTime

import cse.fitzgero.sorouting.algorithm.flowestimation.IterationFWBounds
import cse.fitzgero.sorouting.algorithm.pathsearch.ksp.NoKSPBounds
import cse.fitzgero.sorouting.algorithm.routing.localgraph._
import cse.fitzgero.sorouting.app.SORoutingApplicationConfig
import cse.fitzgero.sorouting.matsimrunner._
import cse.fitzgero.sorouting.matsimrunner.network.MATSimNetworkToCollection
import cse.fitzgero.sorouting.matsimrunner.population._
import cse.fitzgero.sorouting.model.population.{LocalPopulationOps, LocalResponse}
import cse.fitzgero.sorouting.model.population.LocalPopulationOps.LocalPopulationConfig
import cse.fitzgero.sorouting.model.roadnetwork.local.{LocalGraph, LocalGraphOps}
import cse.fitzgero.sorouting.roadnetwork.costfunction.BPRCostFunction
import cse.fitzgero.sorouting.util._
import cse.fitzgero.sorouting.util.implicits._

import scala.collection.GenSeq
import scala.xml.XML


object SOExperimentRefactor extends App {

  val conf: SORoutingApplicationConfig = SORoutingApplicationConfig (
    configFilePath = "data/rye/config.xml",
    networkFilePath = "data/rye/network.xml",
    outputDirectory = "result/newoutput",
    processes = AllProcs,
    timeWindow = 10,
    k = 4,
    kspBounds = NoKSPBounds,
    fwBounds = IterationFWBounds(0),
    populationSize = 500,
    routePercentage = 0.20D,
    startTime = "08:00:00",
    endTime = "09:00:00"
  )

  val fileHelper = SORoutingFilesHelper(conf)
  val networkData = MATSimNetworkToCollection(fileHelper.thisNetworkFilePath)

  val networkXml: xml.Elem = fileHelper.getNetwork
  val graph = LocalGraphOps.readMATSimXML(networkXml)
  val populationConfig: LocalPopulationConfig = LocalPopulationConfig(conf.populationSize, LocalTime.parse("08:30:00"), Some(LocalTime.parse("00:30:00")))
  val populationFull = LocalPopulationOps.generateRequests(graph, populationConfig)

  populationFull.foreach(println)

  //----------------------------------------------------------------------------------------------
  //  1. Run 100% UE Simulation, get overall congestion (measure?)
  //----------------------------------------------------------------------------------------------
  val routingResultUE: GenSeq[LocalResponse] = LocalGraphRoutingUERefactor.routeAllRequestedTimeGroups(conf, fileHelper, populationFull)
  val ueXml: xml.Elem = LocalPopulationOps.generateXMLResponses(graph, routingResultUE)
  fileHelper.savePopulationRef(ueXml, FullUEExp, FullUEPopulation)

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
  //  2. For each snapshot, load and run our algorithm
  //----------------------------------------------------------------------------------------------
//  val (logs, routeCountUE, routeCountSO) = {
//    val routingResult: LocalGraphRoutingModule03Result = LocalGraphRoutingUESOModule03.routeAllRequestedTimeGroups(conf, fileHelper, populationFull)
//    fileHelper.savePopulation(routingResult.population, CombinedUESOExp, CombinedUESOPopulation)
//
//
//    val analytics = routingResult.logs.analytic
//    val algorithmLogger = AuxLogger.get("algorithm")
//
//    val totalRoutes = analytics.getOrZero("algorithm.global.totalroutes")
//
//    algorithmLogger.info("total routes (count)", totalRoutes)
//
//    (routingResult.logs, routingResult.routeCountUE, routingResult.routeCountSO)
//  }

  //----------------------------------------------------------------------------------------------
  //  3. Run 1-p% UE UNION p% SO Simulation, get overall congestion (measure?)
  //----------------------------------------------------------------------------------------------
//  MATSimSingleAnalyticSnapshotRunnerModule(
//    MATSimRunnerConfig(
//      fileHelper.finalConfigFilePath(CombinedUESOExp),
//      fileHelper.experimentPath(CombinedUESOExp),
//      conf.timeWindow,
//      conf.startTime,
//      conf.endTime,
//      ArgsNotMissingValues
//    ),
//    networkData,
//    BPRCostFunction
//  )

  //----------------------------------------------------------------------------------------------
  //  4. Analyze Results
  //----------------------------------------------------------------------------------------------
//  fileHelper.appendToReportFile(PrintToResultFile(
//    conf.populationSize,
//    overallNumberOfTrips,
//    routeCountUE,
//    routeCountSO,
//    (conf.routePercentage * 100).toInt,
//    conf.timeWindow,
//    List(0L), // runTimes.ksp,
//    List(0L), // runTimes.fw,
//    List(0L), // runTimes.selection,
//    List(0L), // runTimes.overall,
//    fileHelper.getPopulationAvgTravelTime(FullUEExp).getOrElse(-1D),
//    fileHelper.getPopulationAvgTravelTime(CombinedUESOExp).getOrElse(-1D),
//    fileHelper.getNetworkAvgTravelTime(FullUEExp).getOrElse(-1D),
//    fileHelper.getNetworkAvgTravelTime(CombinedUESOExp).getOrElse(-1D)
//  ))
}
