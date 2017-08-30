package cse.fitzgero.sorouting.algorithm.routing.localgraphrouting

import java.time.LocalTime
import java.time.format.DateTimeFormatter

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}
import cse.fitzgero.sorouting.algorithm.pathsearch.ksp.PathsFoundBounds
import cse.fitzgero.sorouting.algorithm.pathsearch.od.localgraph.LocalGraphODPairByVertex
import cse.fitzgero.sorouting.algorithm.pathsearch.sssp.localgraphsimplesssp.{LocalGraphMATSimSSSP, LocalGraphVertexOrientedSSSP}
import cse.fitzgero.sorouting.algorithm.routing.{LocalRoutingConfig, ParallelRoutingConfig, RoutingResult}
import cse.fitzgero.sorouting.algorithm.trafficassignment.IterationTerminationCriteria
import cse.fitzgero.sorouting.matsimrunner.{ArgsNotMissingValues, MATSimRunnerConfig, MATSimSingleSnapshotRunnerModule}
import cse.fitzgero.sorouting.matsimrunner.population.PopulationOneTrip
import cse.fitzgero.sorouting.roadnetwork.costfunction.BPRCostFunction
import cse.fitzgero.sorouting.roadnetwork.localgraph.{EdgeMATSim, LocalGraphMATSim, LocalGraphMATSimFactory, VertexMATSim}
import cse.fitzgero.sorouting.util.{SORoutingApplicationConfig1, SORoutingFilesHelper}

import scala.xml.XML

case class LocalGraphRoutingModuleResult(population: PopulationOneTrip, routeCountUE: Int = 0, routeCountSO: Int = 0, runTime: List[Long] = List.empty[Long])

object LocalGraphRoutingModule {

  val StartOfDay = 0
  val RoutingAlgorithmTimeout: Duration = 600 seconds
  val HHmmssFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
  case class TimeGroup (startRange: Int, endRange: Int)

  val sssp = LocalGraphMATSimSSSP()

  def routeAllRequestedTimeGroups(conf: SORoutingApplicationConfig1, fileHelper: SORoutingFilesHelper, population: PopulationOneTrip): LocalGraphRoutingModuleResult = {

    val SomeParallelProcessesSetting: Int = 2 // TODO: more clearly handle parallelism at config level

    val (populationSO, populationPartial) = population.subsetPartition(conf.routePercentage)

//    XML.save(s"${fileHelper.thisExperimentDirectory}/soPop.xml", populationSO.toXml)

    // assign shortest path search to all UE drivers
    val graphWithNoFlows: LocalGraphMATSim =
      LocalGraphMATSimFactory(BPRCostFunction, AlgorithmFlowRate = conf.algorithmTimeWindow.toDouble)
        .fromFile(fileHelper.thisNetworkFilePath) match {
        case Success(g) => g
        case Failure(_) => throw new Error(s"failed to load network file ${fileHelper.thisNetworkFilePath}")
      }
    val populationDijkstrasRoutes =
      if (SomeParallelProcessesSetting == 1)  // TODO again, parallel config here.
        populationPartial.exportAsODPairsByEdge.map(sssp.shortestPath(graphWithNoFlows, _))
      else
        populationPartial.exportAsODPairsByEdge.par.map(sssp.shortestPath(graphWithNoFlows.par, _))

    val populationUE = populationDijkstrasRoutes.foldLeft(populationPartial)(_.updatePerson(_))

    val timeGroups: Iterator[TimeGroup] =
      (StartOfDay +: (LocalTime.parse(conf.startTime).toSecondOfDay until LocalTime.parse(conf.endTime).toSecondOfDay by conf.algorithmTimeWindow.toInt))
        .sliding(2)
        .map(vec => TimeGroup(vec(0), vec(1)))


    // run SO algorithm for each time window, updating the population data with their routes while stepping through time windows
    timeGroups.foldLeft(LocalGraphRoutingModuleResult(populationUE, routeCountUE = populationDijkstrasRoutes.size))((acc, timeGroupSecs) => {
      val (timeGroupStart, timeGroupEnd) =
        (LocalTime.ofSecondOfDay(timeGroupSecs.startRange),
          LocalTime.ofSecondOfDay(timeGroupSecs.endRange))

      val groupToRoute: PopulationOneTrip = populationSO.exportTimeGroup(timeGroupStart, timeGroupEnd)

      if (groupToRoute.persons.isEmpty)
      acc
      else {
        val snapshotPopulation: PopulationOneTrip = acc.population.exportTimeGroup(LocalTime.MIN, timeGroupEnd)
        val snapshotDirectory: String = fileHelper.scaffoldSnapshot(snapshotPopulation, timeGroupStart, timeGroupEnd)

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
            case Success(g) => g.par
            case Failure(e) => throw new Error(s"failed to load network file $networkFilePath and snapshot $snapshotFilePath")
          }

        fileHelper.removeSnapshotFiles(timeGroupStart)

//        println(s"${timeGroupStart.format(HHmmssFormat)} : routing ${groupToRoute.persons.size} requests: ${groupToRoute.persons.map(p => (p.id, p.act1.opts)).mkString(", ")}")

        val routingConfig =
          if (SomeParallelProcessesSetting == 1)
            LocalRoutingConfig(k = 4, PathsFoundBounds(5), IterationTerminationCriteria(10))
          else
            ParallelRoutingConfig(k = 4, PathsFoundBounds(5), IterationTerminationCriteria(10))


        val routingAlgorithm: Future[RoutingResult] = LocalGraphRouting.route(graph, groupToRoute, routingConfig)

        val routingResult: RoutingResult = Await.result(routingAlgorithm, RoutingAlgorithmTimeout)
        routingResult match {
          case LocalGraphRoutingResult(routes, runTime) =>
            val withUpdatedRoutes = routes.foldLeft(groupToRoute)(_.updatePerson(_))
            acc.copy(
              population = acc.population.reintegrateSubset(withUpdatedRoutes),
              routeCountSO = acc.routeCountSO + routes.size,
              runTime = acc.runTime :+ runTime
            )
          case _ =>
            acc
        }
      }
    })
  }
}
