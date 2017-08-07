package cse.fitzgero.sorouting.app

import java.time.LocalTime
import scala.util.{Failure, Success}

import org.apache.spark.{SparkConf, SparkContext}

import cse.fitzgero.sorouting.algorithm.pathsearch.mssp.graphx.simplemssp._
import cse.fitzgero.sorouting.algorithm.trafficassignment.graphx._
import cse.fitzgero.sorouting.algorithm.trafficassignment._
import cse.fitzgero.sorouting.matsimrunner._
import cse.fitzgero.sorouting.matsimrunner.population._
import cse.fitzgero.sorouting.roadnetwork.graphx.graph.GraphXMacroRoadNetwork
import cse.fitzgero.sorouting.roadnetwork.costfunction.BPRCostFunction
import cse.fitzgero.sorouting.util._


object SORoutingApplication extends App {

  val conf: SORoutingApplicationConfig = SORoutingApplicationConfigParseArgs(args)

  val sparkConf = new SparkConf()
    .setAppName("cse.fitzgero.sorouting.app.SORoutingApplication")
    .setMaster(s"local[${conf.sparkProcesses}]")
    .set("spark.executor.memory","1g")
  val sc = new SparkContext(sparkConf)
  sc.setLogLevel("ERROR")

  val fileHelper = SORoutingFilesHelper(conf)

  // Create a population from the road network topology
  // from that population, partition it to drivers that will receive our routing and drivers that will not
  val populationFull: Population =
    PopulationFactory
      .generateRandomPopulation(
        fileHelper.network,
        RandomPopulationConfig(
          conf.populationSize,
          HomeConfig("h"),
          Seq(ActivityConfig("w", LocalTime.parse("09:00"), LocalTime.parse("08:00"), 30L)),
          Seq(ModeConfig("car"))
        )
      )
  val (populationSubsetToRoute, populationRemainder) = populationFull.subsetPartition(conf.routePercentage)


  fileHelper.savePopulation(populationFull, FullUEExp)
  fileHelper.savePopulation(populationRemainder, PartialUEExp)



  //----------------------------------------------------------------------------------------------
  //  1. Run 100% UE Simulation, get overall congestion (measure?)
  //----------------------------------------------------------------------------------------------
  // TODO: determine how to measure congestion
  MATSimRunnerModule(MATSimRunnerConfig(
    fileHelper.configFilePath(FullUEExp),
    fileHelper.experimentPath(FullUEExp),
    conf.algorithmTimeWindow,
    conf.startTime,
    conf.endTime,
    ArgsNotMissingValues
  ))

  //----------------------------------------------------------------------------------------------
  //  2. Run 1-p% UE Simulation, get snapshots
  //----------------------------------------------------------------------------------------------
  MATSimSnapshotRunnerModule(MATSimRunnerConfig(
    fileHelper.configFilePath(PartialUEExp),
    fileHelper.experimentPath(PartialUEExp),
    conf.algorithmTimeWindow,
    conf.startTime,
    conf.endTime,
    ArgsNotMissingValues
  ))


  //----------------------------------------------------------------------------------------------
  //  3. For each snapshot, load as graphx and run our algorithm
  //----------------------------------------------------------------------------------------------
  val populationSubsetRouted: Population = fileHelper.snapshotFileList.foldLeft(populationSubsetToRoute)((populationAccumulator, snapshotFile) => {
    val graph =
      GraphXMacroRoadNetwork(sc, BPRCostFunction)
        .fromFileAndSnapshot(fileHelper.thisNetworkFilePath, snapshotFile) match {
          case Success(g) => g
          case Failure(e) => throw new Error(s"failed to load network file ${fileHelper.thisNetworkFilePath} and snapshot $snapshotFile")
        }
    val startOfTimeRange: LocalTime = fileHelper.parseSnapshotForTime(snapshotFile)
    val endOfTimeRange: LocalTime = startOfTimeRange.plusSeconds(conf.algorithmTimeWindow.toLong)
    val groupToRoute: ODPairs = populationSubsetToRoute.fromTimeGroup(startOfTimeRange, endOfTimeRange)

    if (groupToRoute.nonEmpty) println(s"${LocalTime.now} - routing group in range [$startOfTimeRange,$endOfTimeRange) of size ${groupToRoute.size}")

    val result: GraphXFWSolverResult = GraphXFrankWolfe.solve(graph, groupToRoute, IterationTerminationCriteria(10))

    if (result.iterations != 0) println(s"${LocalTime.now} - completed in ${result.time} ms")

    result.paths.foldLeft(populationAccumulator)((pop, path) => {
      pop.updatePerson(path)
    })
  })

  fileHelper.savePopulation(populationFull.reintegrateSubset(populationSubsetRouted), CombinedUESOExp)

  //----------------------------------------------------------------------------------------------
  //  4. Run 1-p% UE UNION p% SO Simulation, get overall congestion (measure?)
  //----------------------------------------------------------------------------------------------
  // TODO: determine how to measure congestion
  MATSimRunnerModule(MATSimRunnerConfig(
    fileHelper.configFilePath(CombinedUESOExp),
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

  sc.stop()
}
