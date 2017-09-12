package cse.fitzgero.sorouting.algorithm.routing.localgraph

import java.time.LocalTime
import java.time.format.DateTimeFormatter

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}
import cse.fitzgero.sorouting.algorithm.pathsearch.ksp.PathsFoundBounds
import cse.fitzgero.sorouting.algorithm.pathsearch.od.localgraph.LocalGraphODPairByVertex
import cse.fitzgero.sorouting.algorithm.pathsearch.sssp.localgraphsimplesssp.{LocalGraphMATSimSSSP, LocalGraphVertexOrientedSSSP}
import cse.fitzgero.sorouting.algorithm.routing.{LocalRoutingConfig, ParallelRoutingConfig, RoutingResult}
import cse.fitzgero.sorouting.algorithm.trafficassignment.IterationFWBounds
import cse.fitzgero.sorouting.app.SORoutingApplicationConfig
import cse.fitzgero.sorouting.matsimrunner.{ArgsNotMissingValues, MATSimRunnerConfig, MATSimSingleSnapshotRunnerModule}
import cse.fitzgero.sorouting.matsimrunner.population.PopulationOneTrip
import cse.fitzgero.sorouting.roadnetwork.costfunction.BPRCostFunction
import cse.fitzgero.sorouting.roadnetwork.localgraph.{EdgeMATSim, LocalGraphMATSim, LocalGraphMATSimFactory, VertexMATSim}
import cse.fitzgero.sorouting.util._

import scala.xml.XML

//case class LocalGraphRoutingResultRunTimes(ksp: List[Long] = List(), fw: List[Long] = List(), selection: List[Long] = List(), overall: List[Long] = List())

/**
  * return tuple containing results from the completion of the UE routing algorithm
  * @param population the complete population, with selfish routes applied
  * @param routeCountUE number of selfish routes produced. each person may have more than one route.
  */
case class LocalGraphUERoutingModuleResult(population: PopulationOneTrip, routeCountUE: Int = 0)

object LocalGraphRoutingUEModule {

  val StartOfDay = 0
  val RoutingAlgorithmTimeout: Duration = 600 seconds
  val HHmmssFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
  case class TimeGroup (startRange: Int, endRange: Int)

  val sssp = LocalGraphMATSimSSSP()

  /**
    * solves the routing over a provided network with a provided population
    * @param conf
    * @param fileHelper
    * @param population
    * @return
    */
  def routeAllRequestedTimeGroups(conf: SORoutingApplicationConfig, fileHelper: SORoutingFilesHelper, population: PopulationOneTrip): LocalGraphUERoutingModuleResult = {

    val timeGroups: Iterator[TimeGroup] =
      (StartOfDay +: (LocalTime.parse(conf.startTime).toSecondOfDay until LocalTime.parse(conf.endTime).toSecondOfDay by conf.timeWindow))
        .sliding(2)
        .map(vec => TimeGroup(vec(0), vec(1)))

    // run UE algorithm for each time window, updating the population data with their routes while stepping through time windows
    timeGroups.foldLeft(LocalGraphUERoutingModuleResult(population, routeCountUE = population.exportAsODPairsByEdge.size))((acc, timeGroupSecs) => {
      val (timeGroupStart, timeGroupEnd) =
        (LocalTime.ofSecondOfDay(timeGroupSecs.startRange),
          LocalTime.ofSecondOfDay(timeGroupSecs.endRange))

      val groupToRoute: PopulationOneTrip = population.exportTimeGroup(timeGroupStart, timeGroupEnd)

      if (groupToRoute.persons.isEmpty) acc
      else {
        val snapshotPopulation: PopulationOneTrip = acc.population.exportTimeGroup(LocalTime.MIN, timeGroupEnd)
        val snapshotDirectory: String = fileHelper.scaffoldSnapshot(snapshotPopulation, timeGroupStart, timeGroupEnd)

        println(s"${LocalTime.now()} [routeUE] snapshot run for timeGroup [$timeGroupStart, $timeGroupEnd) ${snapshotPopulation.size} persons")
        // ----------------------------------------------------------------------------------------
        // 1. run MATSim snapshot for the populations associated with all previous time groups
        val matsimSnapshotRun = MATSimSingleSnapshotRunnerModule(MATSimRunnerConfig(
          s"$snapshotDirectory/config-snapshot.xml",
          s"$snapshotDirectory/matsim-output",
          conf.timeWindow,
          conf.startTime,
          timeGroupStart.format(HHmmssFormat),
          ArgsNotMissingValues
        ))

        // ----------------------------------------------------------------------------------------
        // 2. run routing algorithm for the UE routed population for current time group, using snapshot

        val networkFilePath: String = s"$snapshotDirectory/network-snapshot.xml"
        val snapshotFilePath: String = matsimSnapshotRun.filePath

//        println(s"${LocalTime.now()} [routeUE] loading graph")
        val graph: LocalGraphMATSim =
          LocalGraphMATSimFactory(BPRCostFunction, AlgorithmFlowRate = conf.timeWindow)
            .fromFileAndSnapshot(networkFilePath, snapshotFilePath) match {
            case Success(g) => g.par
            case Failure(e) => throw new Error(s"failed to load network file $networkFilePath and snapshot $snapshotFilePath")
          }

        fileHelper.removeSnapshotFiles(timeGroupStart)

//        println(s"${LocalTime.now()} [routeUE] running shortest path search")
        val withUpdatedRoutes: PopulationOneTrip =
          groupToRoute
            .exportAsODPairsByEdge
            .par
            .map(sssp.shortestPath(graph, _))
            .foldLeft(groupToRoute)(_.updatePerson(_))

//        println(s"${LocalTime.now()} [routeUE] finishing timeGroup [$timeGroupStart, $timeGroupEnd)")
        acc.copy(population = acc.population.reintegrateSubset(withUpdatedRoutes))
      }
    })
  }
}
