package cse.fitzgero.sorouting.algorithm.routing.localgraph

import java.time.LocalTime
import java.time.format.DateTimeFormatter

import cse.fitzgero.sorouting.algorithm.pathsearch.sssp.localgraphsimplesssp.LocalGraphMATSimSSSP
import cse.fitzgero.sorouting.algorithm.routing.{LocalRoutingConfig, ParallelRoutingConfig, RoutingResult}
import cse.fitzgero.sorouting.app.SORoutingApplicationConfig
import cse.fitzgero.sorouting.matsimrunner.population.PopulationOneTrip
import cse.fitzgero.sorouting.matsimrunner.{ArgsNotMissingValues, MATSimRunnerConfig, MATSimSingleSnapshotRunnerModule}
import cse.fitzgero.sorouting.roadnetwork.costfunction.BPRCostFunction
import cse.fitzgero.sorouting.roadnetwork.localgraph.{LocalGraphMATSim, LocalGraphMATSimFactory}
import cse.fitzgero.sorouting.util._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

case class LocalGraphRoutingUESOResult03RunTimes(ksp: List[Long] = List(), fw: List[Long] = List(), selection: List[Long] = List(), overall: List[Long] = List())

/**
  * return tuple containing results from the completion of the routing algorithm
  * @param population the complete population, with selfish and system-optimal routes applied
  * @param routeCountUE number of selfish routes produced. each person may have more than one route.
  * @param routeCountSO number of system-optimal routes produced.
  * @param runTimes an object capturing the types of runtime values we are aggregating
  */
case class LocalGraphRoutingModule03Result(population: PopulationOneTrip, routeCountUE: Int = 0, routeCountSO: Int = 0, runTimes: LocalGraphRoutingUESOResult03RunTimes = LocalGraphRoutingUESOResult03RunTimes())

object LocalGraphRoutingUESOModule03 extends ClassLogging {

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
  def routeAllRequestedTimeGroups(conf: SORoutingApplicationConfig, fileHelper: SORoutingFilesHelper, population: PopulationOneTrip): LocalGraphRoutingModule03Result = {

    val (populationSO, populationUE) = population.subsetPartition(conf.routePercentage)

    val timeGroups: Iterator[TimeGroup] =
      (StartOfDay +: (LocalTime.parse(conf.startTime).toSecondOfDay until LocalTime.parse(conf.endTime).toSecondOfDay by conf.timeWindow))
        .sliding(2)
        .map(vec => TimeGroup(vec(0), vec(1)))

    // run SO algorithm for each time window, updating the population data with their routes while stepping through time windows
    timeGroups.foldLeft(LocalGraphRoutingModule03Result(PopulationOneTrip()))((acc, timeGroupSecs) => {
      val (timeGroupStart, timeGroupEnd) =
        (LocalTime.ofSecondOfDay(timeGroupSecs.startRange),
          LocalTime.ofSecondOfDay(timeGroupSecs.endRange))

      val groupToRouteSO: PopulationOneTrip = populationSO.exportTimeGroup(timeGroupStart, timeGroupEnd)
      val groupToRouteUE: PopulationOneTrip = populationUE.exportTimeGroup(timeGroupStart, timeGroupEnd)

      if (groupToRouteSO.persons.isEmpty && groupToRouteUE.persons.isEmpty) acc
      else {
        val snapshotPopulation: PopulationOneTrip = acc.population.exportTimeGroup(LocalTime.MIN, timeGroupEnd)
        val snapshotDirectory: String = fileHelper.scaffoldSnapshot(snapshotPopulation, timeGroupStart, timeGroupEnd)

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
        // 2. run routing algorithm for the SO routed population for current time group, using snapshot

        val networkFilePath: String = s"$snapshotDirectory/network-snapshot.xml"
        val snapshotFilePath: String = matsimSnapshotRun.filePath

        val graph: LocalGraphMATSim =
          LocalGraphMATSimFactory(BPRCostFunction, AlgorithmFlowRate = conf.timeWindow)
            .fromFileAndSnapshot(networkFilePath, snapshotFilePath) match {
            case Success(g) =>
              g.par
            case Failure(e) => throw new Error(s"failed to load network file $networkFilePath and snapshot $snapshotFilePath")
          }

        fileHelper.removeSnapshotFiles(timeGroupStart)
        val routingConfig = conf.processes match {
          case OneProc => LocalRoutingConfig(conf.k, conf.kspBounds, conf.fwBounds)
          case AllProcs => ParallelRoutingConfig(conf.k, conf.kspBounds, conf.fwBounds)
          case NumProcs(n) => ParallelRoutingConfig(conf.k, conf.kspBounds, conf.fwBounds, n)
        }

        // run shortest path based on time/cost function graph for selfish (UE) population subset
        val routingUE: Future[PopulationOneTrip] =
          Future {
            groupToRouteUE
              .exportAsODPairsByEdge
              .par // TODO: parallelism should be selectable
              .map(sssp.shortestPath(graph, _))
              .foldLeft(groupToRouteUE)(_.updatePerson(_))
          }

        // run system-optimal routing algorithm for system-optimal (SO) population subset
        val routingAlgorithmResult: Future[RoutingResult] = LocalGraphRouting02.route(graph, groupToRouteSO, routingConfig)
        val routingSO: RoutingResult = Await.result(routingAlgorithmResult, RoutingAlgorithmTimeout)

        routingSO match {
          case LocalGraphRoutingResult(routesSO, kspRunTime, fwRunTime, routeSelectionRunTime, overallRunTime) =>
            val routesUE: PopulationOneTrip = Await.result(routingUE, RoutingAlgorithmTimeout)
            val withUpdatedUERoutes: PopulationOneTrip = routesUE
            val withUpdatedSORoutes: PopulationOneTrip = routesSO.foldLeft(groupToRouteSO)(_.updatePerson(_))

            val prevRT: LocalGraphRoutingUESOResult03RunTimes = acc.runTimes
            acc.copy(
              population = acc.population.reintegrateSubset(withUpdatedSORoutes).reintegrateSubset(withUpdatedUERoutes),
              routeCountUE = acc.routeCountUE + routesUE.size,
              routeCountSO = acc.routeCountSO + routesSO.size,
              runTimes = prevRT.copy(
                ksp = prevRT.ksp :+ kspRunTime,
                fw = prevRT.fw :+ fwRunTime,
                selection = prevRT.selection :+ routeSelectionRunTime,
                overall = prevRT.overall :+ overallRunTime
              )
            )
          case x: RoutingResult =>
//            println(s"routing result was $x")
            acc
        }
      }
    })
  }
}
