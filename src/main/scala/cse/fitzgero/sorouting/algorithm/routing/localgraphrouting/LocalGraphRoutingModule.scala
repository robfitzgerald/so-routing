package cse.fitzgero.sorouting.algorithm.routing.localgraphrouting

import java.time.LocalTime
import java.time.format.DateTimeFormatter

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}
import cse.fitzgero.sorouting.algorithm.pathsearch.ksp.PathsFoundBounds
import cse.fitzgero.sorouting.algorithm.pathsearch.od.localgraph.LocalGraphODPair
import cse.fitzgero.sorouting.algorithm.routing.{LocalRoutingConfig, ParallelRoutingConfig, RoutingResult}
import cse.fitzgero.sorouting.algorithm.trafficassignment.IterationTerminationCriteria
import cse.fitzgero.sorouting.matsimrunner.{ArgsNotMissingValues, MATSimRunnerConfig, MATSimSingleSnapshotRunnerModule}
import cse.fitzgero.sorouting.matsimrunner.population.PopulationOneTrip
import cse.fitzgero.sorouting.roadnetwork.costfunction.BPRCostFunction
import cse.fitzgero.sorouting.roadnetwork.localgraph.{LocalGraphMATSim, LocalGraphMATSimFactory}
import cse.fitzgero.sorouting.util.{SORoutingApplicationConfig, SORoutingFilesHelper}


object LocalGraphRoutingModule {

  case class TimeGroup (startRange: Int, endRange: Int)

  val RoutingAlgorithmTimeout: Duration = 120 seconds

  def routeAllRequestedTimeGroups(
    conf: SORoutingApplicationConfig,
    fileHelper: SORoutingFilesHelper,
    population: PopulationOneTrip): PopulationOneTrip = {

    val (populationSO, populationPartial) = population.subsetPartition(conf.routePercentage)

    val timeGroups: Iterator[TimeGroup] =
      (0 +: (LocalTime.parse(conf.startTime).toSecondOfDay until LocalTime.parse(conf.endTime).toSecondOfDay by conf.algorithmTimeWindow.toInt))
        .sliding(2)
        .map(vec => TimeGroup(vec(0), vec(1)))

    val HHmmssFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    timeGroups.foldLeft(populationPartial)((populationWithUpdates, timeGroupSecs) => {
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
        timeGroupEnd.format(HHmmssFormat),
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
  }
}
