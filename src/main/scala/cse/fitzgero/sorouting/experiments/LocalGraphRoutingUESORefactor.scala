package cse.fitzgero.sorouting.experiments

import java.time.LocalTime
import java.time.format.DateTimeFormatter

import cse.fitzgero.graph.config.KSPBounds
import cse.fitzgero.sorouting.algorithm.local.ksp.KSPLocalDijkstrasConfig
import cse.fitzgero.sorouting.algorithm.local.mssp.MSSPLocalDijkstrasService
import cse.fitzgero.sorouting.algorithm.local.routing.KSPCombinatorialRoutingService
import cse.fitzgero.sorouting.algorithm.pathsearch.sssp.localgraphsimplesssp.LocalGraphMATSimSSSP
import cse.fitzgero.sorouting.app.SORoutingApplicationConfig
import cse.fitzgero.sorouting.experiments.ops.ExperimentOps
import cse.fitzgero.sorouting.matsimrunner.{ArgsNotMissingValues, MATSimRunnerConfig, MATSimSingleSnapshotRunnerModule}
import cse.fitzgero.sorouting.model.population.{LocalPopulationOps, LocalRequest, LocalResponse}
import cse.fitzgero.sorouting.model.roadnetwork.costfunction.BPRCostFunctionType
import cse.fitzgero.sorouting.model.roadnetwork.local.{LocalGraph, LocalGraphOps}
import cse.fitzgero.sorouting.util._

import scala.collection.GenSeq
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.Random
import scala.xml.XML

object LocalGraphRoutingUESORefactor extends ClassLogging {

  val random = new Random

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
  def routeAllRequestedTimeGroups(conf: SORoutingApplicationConfig, fileHelper: SORoutingFilesHelper, population: GenSeq[LocalRequest]): (GenSeq[LocalResponse], Map[String, Long]) = {

    val kspConfig: KSPLocalDijkstrasConfig = KSPLocalDijkstrasConfig(4, Some(KSPBounds.Iteration(8)))

    val (testGroup, controlGroup) = ExperimentOps.splitPopulation(population, conf.routePercentage)

    val timeGroups: Iterator[TimeGroup] =
      (StartOfDay +: (LocalTime.parse(conf.startTime).toSecondOfDay until LocalTime.parse(conf.endTime).toSecondOfDay by conf.timeWindow))
        .sliding(2)
        .map(vec => TimeGroup(vec(0), vec(1)))

    // run SO algorithm for each time window, updating the population data with their routes while stepping through time windows
    timeGroups.foldLeft((GenSeq.empty[LocalResponse], Map.empty[String, Long]))((acc, timeGroupSecs) => {
      val (timeGroupStart, timeGroupEnd) =
        (LocalTime.ofSecondOfDay(timeGroupSecs.startRange),
          LocalTime.ofSecondOfDay(timeGroupSecs.endRange))

      val groupToRouteSO: GenSeq[LocalRequest] = testGroup.filter(req => {
        timeGroupStart.compareTo(req.requestTime) <= 0 &&
          req.requestTime.compareTo(timeGroupEnd) < 0
      })
      val groupToRouteUE: GenSeq[LocalRequest] = controlGroup.filter(req => {
        timeGroupStart.compareTo(req.requestTime) <= 0 &&
          req.requestTime.compareTo(timeGroupEnd) < 0
      })

      if (groupToRouteSO.isEmpty && groupToRouteUE.isEmpty) acc
      else {
        val snapshotPopulation: GenSeq[LocalResponse] = acc._1
        val previousPopGraph: LocalGraph = LocalGraphOps.readMATSimXML(fileHelper.getNetwork, None, BPRCostFunctionType, conf.timeWindow)
        val previousPopXML: xml.Elem = LocalPopulationOps.generateXMLResponses(previousPopGraph, snapshotPopulation)
        val snapshotDirectory: String = fileHelper.scaffoldSnapshotRef(previousPopXML, timeGroupStart, timeGroupEnd)

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

        val snapshotXML: xml.Elem = XML.loadFile(snapshotFilePath)
        val snapshotGraph: LocalGraph = LocalGraphOps.readMATSimXML(fileHelper.getNetwork, Some(snapshotXML), BPRCostFunctionType, conf.timeWindow)

        fileHelper.removeSnapshotFiles(timeGroupStart)

        // run shortest path based on time/cost function graph for selfish (UE) population subset

        val future = for {
          routesUE <- MSSPLocalDijkstrasService.runService(snapshotGraph, groupToRouteUE)
          routesSO <- KSPCombinatorialRoutingService.runService(snapshotGraph, groupToRouteSO, Some(kspConfig))
        } yield (routesUE, routesSO)

        val (resolvedUE, resolvedSO) = Await.result(future, 1 hour) // TODO: better timeout bounds

        val resultUE: (GenSeq[LocalResponse], Map[String, Long]) =
          resolvedUE match {
            case Some(res) => (res.result, res.logs)
            case None => (GenSeq.empty[LocalResponse], Map.empty[String, Long])
          }

        val resultSO: (GenSeq[LocalResponse], Map[String, Long]) =
          resolvedSO match {
            case Some(res) => (res.result, res.logs)
            case None => (GenSeq.empty[LocalResponse], Map.empty[String, Long])
          }

        val updatedResult: GenSeq[LocalResponse] = acc._1 ++ resultUE._1 ++ resultSO._1
        val updatedLogs: Map[String, Long] = ExperimentOps.sumLogs(ExperimentOps.sumLogs(acc._2, resultUE._2), resultSO._2)

        val combinations: Long =
          if (resultSO._2.isDefinedAt("algorithm.selection.local.combinations"))
            resultSO._2("algorithm.selection.local.combinations")
          else 0L

        println(s"${LocalTime.now} [UESO Router] routed group at time ${timeGroupStart.format(HHmmssFormat)} with ${resultSO._1.size} requests and $combinations combinations.")

        (updatedResult, updatedLogs)
      }
    })
  }
}
