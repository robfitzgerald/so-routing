package cse.fitzgero.sorouting.app

import java.nio.file.Files
import java.time.LocalTime

import scala.collection.GenSeq
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success}
import cse.fitzgero.sorouting.algorithm.pathsearch.ksp.PathsFoundBounds
import cse.fitzgero.sorouting.algorithm.pathsearch.mssp.graphx.simplemssp._
import cse.fitzgero.sorouting.algorithm.pathsearch.od.localgraph.{LocalGraphODPair, LocalGraphODPath}
import cse.fitzgero.sorouting.algorithm.routing.{LocalRoutingConfig, NoRoutingSolution, ParallelRoutingConfig, RoutingResult}
import cse.fitzgero.sorouting.algorithm.routing.localgraphrouting.{LocalGraphRouting, LocalGraphRoutingResult}
import cse.fitzgero.sorouting.algorithm.trafficassignment._
import cse.fitzgero.sorouting.algorithm.trafficassignment.graphx._
import cse.fitzgero.sorouting.algorithm.trafficassignment.localgraph.{LocalGraphFWSolverResult, LocalGraphFrankWolfe}
import cse.fitzgero.sorouting.matsimrunner._
import cse.fitzgero.sorouting.matsimrunner.population._
import cse.fitzgero.sorouting.roadnetwork.costfunction.BPRCostFunction
import cse.fitzgero.sorouting.roadnetwork.graphx.graph.GraphXMacroRoadNetwork
import cse.fitzgero.sorouting.roadnetwork.localgraph.{LocalGraphMATSim, LocalGraphMATSimFactory}
import cse.fitzgero.sorouting.util._
import cse.fitzgero.sorouting.util.convenience._



object SORoutingLocalGraphInlineApplication extends App {

  val RoutingAlgorithmTimeout = 120 seconds

  val conf: SORoutingApplicationConfig = SORoutingApplicationConfigParseArgs(args)

//  val sparkConf = new SparkConf()
//    .setAppName("cse.fitzgero.sorouting.app.SORoutingApplication")
//    .setMaster(s"local[${conf.sparkProcesses}]")
//    .set("spark.executor.memory","1g")
//  val sc = new SparkContext(sparkConf)
//  sc.setLogLevel("ERROR")

  val fileHelper = SORoutingFilesHelper(conf)

  // Create a population from the road network topology
  // from that population, partition it to drivers that will receive our routing and drivers that will not
  val populationFull: PopulationOneTrip =
    PopulationOneTrip
      .generateRandomOneTripPopulation(
        fileHelper.network,
        RandomPopulationOneTripConfig(
          conf.populationSize,
          Seq(
            ActivityConfig2(
              "home",
              LocalTime.parse("08:00:00") endTime,
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
  //  3. For each snapshot, load as graphx and run our algorithm
  //----------------------------------------------------------------------------------------------

  // for each time group,



  case class TimeGroup (startRange: Int, endRange: Int)
  val timeGroups: Iterator[TimeGroup] =
    (0 +: (LocalTime.parse(conf.startTime).toSecondOfDay until LocalTime.parse(conf.endTime).toSecondOfDay))
      .sliding(2)
      .map(vec => TimeGroup(vec(0), vec(1)))


  val populationCombinedUESO: PopulationOneTrip = timeGroups.foldLeft(populationPartial)((populationWithUpdates, timeGroupSecs) => {
    val (timeGroupStart, timeGroupEnd) =
      (LocalTime.ofSecondOfDay(timeGroupSecs.startRange),
        LocalTime.ofSecondOfDay(timeGroupSecs.endRange))

    val snapshotPopulation: PopulationOneTrip = populationWithUpdates.exportTimeGroup(timeGroupStart, timeGroupEnd)

    val snapshotDirectory: String = fileHelper.scaffoldSnapshot(snapshotPopulation, timeGroupStart)

    // ----------------------------------------------------------------------------------------
    // 1. run MATSim snapshot for the populations associated with all previous time groups
    val matsimSnapshotRun = MATSimSingleSnapshotRunnerModule(MATSimRunnerConfig(
      s"$snapshotDirectory/config-snapshot.xml",
      s"$snapshotDirectory/matsim-output",
      conf.algorithmTimeWindow,
      conf.startTime,
      timeGroupEnd.formatted("HH:mm:ss"),
      ArgsNotMissingValues
    ))

    val networkFilePath: String = s"$snapshotDirectory/network-snapshot.xml"
    val snapshotFilePath: String = matsimSnapshotRun.filePath

    // ----------------------------------------------------------------------------------------
    // 2. run routing algorithm for the SO routed population for current time group, using snapshot

    val graph: LocalGraphMATSim =
      LocalGraphMATSimFactory(BPRCostFunction, AlgorithmFlowRate = conf.algorithmTimeWindow.toDouble)
        .fromFileAndSnapshot(networkFilePath, snapshotFilePath) match {
          case Success(g) => g
          case Failure(e) => throw new Error(s"failed to load network file $networkFilePath and snapshot $snapshotFilePath")
        }

    val groupToRoute: PopulationOneTrip = populationSO.exportTimeGroup(timeGroupStart, timeGroupEnd)
    val odPairs: Seq[LocalGraphODPair] = groupToRoute.exportAsODPairs

    val SomeParallelProcessesSetting: Int = 1 // TODO: more clearly handle parallelism at config level
    val routingConfig =
      if (SomeParallelProcessesSetting == 1)
        LocalRoutingConfig(k = 4, PathsFoundBounds(5), IterationTerminationCriteria(10))
      else
        ParallelRoutingConfig(k = 4, PathsFoundBounds(5), IterationTerminationCriteria(10))


    val routingAlgorithm: Future[RoutingResult] = LocalGraphRouting.route(graph, odPairs, routingConfig)

    val routingResult: RoutingResult = Await.result(routingAlgorithm, RoutingAlgorithmTimeout)
    routingResult match {
      case LocalGraphRoutingResult(routes, runTime) =>
        val withUpdatedRoutes = routes.foldLeft(groupToRoute)(_.updatePerson(_))
        populationWithUpdates.reintegrateSubset(withUpdatedRoutes)
      case _ =>
        populationWithUpdates
    }
  })

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
