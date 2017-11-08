package cse.fitzgero.sorouting.experiments.steps

import java.time.{LocalDateTime, LocalTime}
import java.time.format.DateTimeFormatter

import scala.collection.GenSeq
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.Try
import scala.xml.XML

import cse.fitzgero.graph.config.KSPBounds.Iteration
import cse.fitzgero.sorouting.algorithm.local.ksp.KSPLocalDijkstrasConfig
import cse.fitzgero.sorouting.algorithm.local.mssp.MSSPLocalDijkstrasService
import cse.fitzgero.sorouting.algorithm.local.routing.KSPCombinatorialRoutingService
import cse.fitzgero.sorouting.experiments.ops.ExperimentOps.TimeGroup
import cse.fitzgero.sorouting.experiments.ops.{ExperimentFSOps, ExperimentOps, ExperimentStepOps, MATSimOps}
import cse.fitzgero.sorouting.model.population.{LocalPopulationOps, LocalRequest, LocalResponse}
import cse.fitzgero.sorouting.model.roadnetwork.costfunction.BPRCostFunctionType
import cse.fitzgero.sorouting.model.roadnetwork.local.{LocalGraph, LocalGraphOps}
import edu.ucdenver.fitzgero.lib.experiment._

object SystemOptimalRouting {

  type SORoutingConfig = {
    def experimentInstanceDirectory: String
    def startTime: LocalTime
    def endTime: Option[LocalTime]
    def timeWindow: Int
    def routePercentage: Double
  }

  val HHmmssFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
  val StartOfDay: LocalTime = LocalTime.MIN
  val RoutingAlgorithmTimeout: Duration = 1 hour

  object Incremental extends SyncStep {

    override type StepConfig = SORoutingConfig
    val name: String = "[SystemOptimalRouting:Incremental] KSP Combinatorial Experiment for LocalGraph graphs with incremental (batch) evaluation"

    override def apply(config: StepConfig, log: ExperimentGlobalLog): Option[(StepStatus, ExperimentStepLog)] = Some {
      // setup instance directory has already occurred, so we have population.xml, config.xml, network.xml
      val t: Try[ExperimentStepLog] =
        Try [ExperimentStepLog] {
          // load population.xml as GenSeq[Request], split according to config.routePercentage
          val populationXML: xml.Elem = XML.load(s"${config.experimentInstanceDirectory}/population.xml")
          val population: GenSeq[LocalRequest] = LocalPopulationOps.fromXML(populationXML)
          val (testGroup, controlGroup) = ExperimentOps.splitPopulation(population, config.routePercentage)

          // generate time groups from 0 to config.endTime by config.timeWindow
          val endTime = config.endTime match {
            case Some(time) => time
            case None => LocalTime.MAX
          }
          val timeGroups: Iterator[TimeGroup] =
            (StartOfDay.toSecondOfDay +: (config.startTime.toSecondOfDay until endTime.toSecondOfDay by config.timeWindow))
              .sliding(2)
              .map(vec =>
                TimeGroup(
                  LocalTime.ofSecondOfDay(vec(0)),
                  LocalTime.ofSecondOfDay(vec(1))
                )
              )

          // run incremental phase for each time window
          val kspConfig = KSPLocalDijkstrasConfig(4, Some(Iteration(10)))
          val incrementalStep: ((GenSeq[LocalResponse], Map[String, Long]), TimeGroup) => (GenSeq[LocalResponse], Map[String, Long]) =
            Incremental.incrementalLoopLocal(testGroup, controlGroup, config.experimentInstanceDirectory, config.timeWindow, kspConfig)
          val result = timeGroups.foldLeft((GenSeq.empty[LocalResponse], Map.empty[String, Long]))(incrementalStep)

          // save final population which is the combined UE/SO population
          val networkXml: xml.Elem = XML.load(s"${config.experimentInstanceDirectory}/network.xml")
          val graph = LocalGraphOps.readMATSimXML(networkXml)
          val populationUESO: xml.Elem = LocalPopulationOps.generateXMLResponses(graph, result._1)
          ExperimentFSOps.saveXmlDocType(s"${config.experimentInstanceDirectory}/population.xml", populationUESO, ExperimentFSOps.PopulationDocType)

          // run MATSim one last time to produce a Snapshot file here
          MATSimOps.MATSimRun(config.experimentInstanceDirectory, config.startTime, endTime, config.timeWindow)

          val networkAvgTravelTime: String = MATSimOps.getNetworkAvgTravelTime(config.experimentInstanceDirectory)
          val populationTravelTime: String = MATSimOps.getPopulationAvgTravelTime(config.experimentInstanceDirectory)
          val outputLog = result._2.mapValues(_.toString) ++ Map(
            "experiment.result.traveltime.avg.network" -> networkAvgTravelTime,
            "experiment.result.traveltime.avg.population" -> populationTravelTime,
            "experiment.type" -> "System Optimal Incremental"
          )

          outputLog
        }

      ExperimentStepOps.resolveTry(t, Some(name))
    }


    // inner loop function
    // given an accumulator of a response type, run a system optimal experiment for a time group that occurs between an incremental time and a time window delta
    protected def incrementalLoopLocal(testGroup: GenSeq[LocalRequest], controlGroup: GenSeq[LocalRequest], currentInstanceDirectory: String, timeWindow: Int, kspConfig: KSPLocalDijkstrasConfig)
      (accumulator : (GenSeq[LocalResponse], Map[String, Long]), timeGroup: TimeGroup): (GenSeq[LocalResponse], Map[String, Long]) = {

      val groupToRouteSO: GenSeq[LocalRequest] = testGroup.filter(ExperimentOps.filterByTimeGroup(timeGroup))
      val groupToRouteUE: GenSeq[LocalRequest] = controlGroup.filter(ExperimentOps.filterByTimeGroup(timeGroup))

      if (groupToRouteSO.isEmpty && groupToRouteUE.isEmpty) accumulator
      else {
        // population with solved routes for all times before the current time group
        val snapshotPopulation: GenSeq[LocalResponse] = accumulator._1
        val networkXML: xml.Elem = XML.load(s"$currentInstanceDirectory/network.xml")
        val previousPopGraph: LocalGraph = LocalGraphOps.readMATSimXML(networkXML, None, BPRCostFunctionType, timeWindow)
        val previousPopXML: xml.Elem = LocalPopulationOps.generateXMLResponses(previousPopGraph, snapshotPopulation)

        // create a temp snapshot directory with the required assets and the previousPopXML population.xml file
        val snapshotDirectory: String = ExperimentFSOps.importAssetsToTempDirectory(currentInstanceDirectory)
        ExperimentFSOps.saveXmlDocType(
          s"$snapshotDirectory/population.xml",
          previousPopXML,
          ExperimentFSOps.PopulationDocType
        )

        // run MATSim on the previous populations
        val snapshotURI: String = MATSimOps.MATSimRun(snapshotDirectory, StartOfDay, timeGroup.startRange, timeWindow)

        // TODO: very infrequently, fs is not completed writing the snapshot file. maybe add a bounded spin wait here?
        val snapshotXML: xml.Elem = XML.loadFile(snapshotURI)
        val snapshotGraph: LocalGraph = LocalGraphOps.readMATSimXML(networkXML, Some(snapshotXML), BPRCostFunctionType, timeWindow)

        ExperimentFSOps.recursiveDelete(snapshotDirectory)

        // TODO: parameterize the routesSO algorithm
//        val future = for {
//          routesUE <- MSSPLocalDijkstrasService.runService(snapshotGraph, groupToRouteUE)
//          routesSO <- KSPCombinatorialRoutingService.runService(snapshotGraph, groupToRouteSO, Some(kspConfig))
//        } yield (routesUE, routesSO)
//        val (resolvedUE, resolvedSO) = Await.result(future, RoutingAlgorithmTimeout)

        val routesUE = MSSPLocalDijkstrasService.runService(snapshotGraph, groupToRouteUE)
        val routesSO = KSPCombinatorialRoutingService.runService(snapshotGraph, groupToRouteSO, Some(kspConfig))
        val resolvedUE = Await.result(routesUE, 1 hour)
        val resolvedSO = Await.result(routesSO, 1 hour)

        // TODO: these should be encapsulated, but their base trait isn't designed correctly for a generalization on res.result
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

        val updatedResult: GenSeq[LocalResponse] = accumulator._1 ++ resultUE._1 ++ resultSO._1
        val updatedLogs: Map[String, Long] = ExperimentOps.sumLogs(ExperimentOps.sumLogs(accumulator._2, resultUE._2), resultSO._2)

//        println(s"merged logs at end of time group ${timeGroup.startRange}")
//        updatedLogs.foreach(println)

        val combinations: Long =
          if (resultSO._2.isDefinedAt("algorithm.selection.local.combinations"))
            resultSO._2("algorithm.selection.local.combinations")
          else 0L

        println(s"${LocalDateTime.now} [UESO Router] routed group at time ${timeGroup.startRange.format(HHmmssFormat)} with ${resultUE._1.size} selfish requests, ${resultSO._1.size} optimal requests, and $combinations combinations.")

        (updatedResult, updatedLogs)
      }
    }
  }
}
