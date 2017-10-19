package cse.fitzgero.sorouting.experiments

import java.time.LocalTime

import cse.fitzgero.sorouting.algorithm.flowestimation.IterationFWBounds
import cse.fitzgero.sorouting.algorithm.pathsearch.ksp.NoKSPBounds
import cse.fitzgero.sorouting.app.SORoutingApplicationConfig
import cse.fitzgero.sorouting.matsimrunner._
import cse.fitzgero.sorouting.matsimrunner.network.MATSimNetworkToCollection
import cse.fitzgero.sorouting.model.population.LocalPopulationOps.LocalPopulationConfig
import cse.fitzgero.sorouting.model.population.{LocalPopulationOps, LocalResponse}
import cse.fitzgero.sorouting.model.roadnetwork.local.LocalGraphOps
import cse.fitzgero.sorouting.roadnetwork.costfunction.BPRCostFunction
import cse.fitzgero.sorouting.util._
import org.apache.log4j.{Level, Logger}

import scala.collection.GenSeq


object SOExperimentRefactor extends App {

  val pop: Int = if (args.size > 0) args(0).toInt else 100
  val win: Int = if (args.size > 1) args(1).toInt else 10
  val route: Double = if (args.size > 2) args(2).toDouble else 0.20D

  Logger.getRootLogger.setLevel(Level.WARN)

  val conf: SORoutingApplicationConfig = SORoutingApplicationConfig (
    configFilePath = "data/rye/config.xml",
    networkFilePath = "data/rye/network.xml",
    outputDirectory = "result/20171019",
    processes = AllProcs,
    timeWindow = win,
    k = 4,
    kspBounds = NoKSPBounds,
    fwBounds = IterationFWBounds(0),
    populationSize = pop,
    routePercentage = route,
    startTime = "08:25:00",
    endTime = "08:35:00"
  )

  val fileHelper = SORoutingFilesHelper(conf)
  val networkData = MATSimNetworkToCollection(fileHelper.thisNetworkFilePath)

  val networkXml: xml.Elem = fileHelper.getNetwork
  val graph = LocalGraphOps.readMATSimXML(networkXml)
  val populationConfig: LocalPopulationConfig = LocalPopulationConfig(conf.populationSize, LocalTime.parse("08:30:00"), Some(LocalTime.parse("00:05:00")))
  val populationFull = LocalPopulationOps.generateRequests(graph, populationConfig)

  //----------------------------------------------------------------------------------------------
  //  1. Run 100% UE Simulation, get overall congestion (measure?)
  //----------------------------------------------------------------------------------------------
  val runUEInThisExperiment: Boolean = fileHelper.needToRunUEExperiment
  val ueLogs = if (runUEInThisExperiment) {
    val routingResultUE: (GenSeq[LocalResponse], Map[String, Long]) = LocalGraphRoutingUERefactor.routeAllRequestedTimeGroups(conf, fileHelper, populationFull)
    val ueXml: xml.Elem = LocalPopulationOps.generateXMLResponses(graph, routingResultUE._1)
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
    routingResultUE._2
  } else {
    println("selfish control experiment found (full-ue directory)")
    Map.empty[String, Long]
  }



  //----------------------------------------------------------------------------------------------
  //  2. For each snapshot, load and run our algorithm
  //----------------------------------------------------------------------------------------------
  val routingResultSO: (GenSeq[LocalResponse], Map[String, Long]) = LocalGraphRoutingUESORefactor.routeAllRequestedTimeGroups(conf, fileHelper, populationFull)
  val soXml: xml.Elem = LocalPopulationOps.generateXMLResponses(graph, routingResultSO._1)
  fileHelper.savePopulationRef(soXml, CombinedUESOExp, CombinedUESOPopulation)
  val soLogs = routingResultSO._2

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

  //----------------------------------------------------------------------------------------------
  //  4. Analyze Results
  //----------------------------------------------------------------------------------------------

  println("~~ Logs ~~")
  println("~~~~~~~~~~")
  println("~~ Selfish Routing ~~")
  ueLogs.toSeq.sortBy(_._1).foreach(println)
  println("~~ Optimal Routing ~~")
  routingResultSO._2.toSeq.sortBy(_._1).foreach(println)

  // TODO: replace with a file-helper call to load this value from the log file if we didn't run UE experiment this time
  val expectedUECost: Long =
    if (fileHelper.needToRunUEExperiment)
      soLogs("algorithm.mssp.local.cost.effect")
    else
      0L

  val expectedSOCost: Long = soLogs("algorithm.selection.local.cost.effect") + soLogs("algorithm.mssp.local.cost.effect")


  // TODO: manually echo the header row to the result file. see PrintToResultFile2.resultFileHeader

  fileHelper.appendToReportFile(PrintToResultFile2(
    conf.populationSize,
    populationFull.size,
    soLogs("algorithm.mssp.local.batch.completed").toInt,
    soLogs("algorithm.routing.local.batch.completed").toInt,
    (conf.routePercentage * 100).toInt,
    conf.timeWindow,
    soLogs("algorithm.selection.local.combinations").toInt,
    soLogs("algorithm.mksp.local.hasalternates").toInt,
    expectedUECost.toInt,
    expectedSOCost.toInt,
    fileHelper.getPopulationAvgTravelTime(FullUEExp).getOrElse(-1D),
    fileHelper.getPopulationAvgTravelTime(CombinedUESOExp).getOrElse(-1D),
    fileHelper.getNetworkAvgTravelTime(FullUEExp).getOrElse(-1D),
    fileHelper.getNetworkAvgTravelTime(CombinedUESOExp).getOrElse(-1D)
  ))

  if (runUEInThisExperiment)
    ExperimentOps.writeLog(ueLogs, fileHelper.experimentPath(FullUEExp), "log-ue.txt")
  ExperimentOps.writeLog(routingResultSO._2, fileHelper.experimentPath(CombinedUESOExp), "log-so.txt")
}
