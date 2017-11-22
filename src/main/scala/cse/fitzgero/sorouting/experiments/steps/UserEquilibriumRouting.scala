package cse.fitzgero.sorouting.experiments.steps

import java.time.{LocalDateTime, LocalTime}
import java.time.format.DateTimeFormatter

import scala.collection.GenSeq
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Try
import scala.xml.XML

import cse.fitzgero.sorouting.algorithm.local.mssp.MSSPLocalDijkstrasService
import cse.fitzgero.sorouting.experiments.ops.ExperimentOps.TimeGroup
import cse.fitzgero.sorouting.experiments.ops.{ExperimentFSOps, ExperimentOps, ExperimentStepOps, MATSimOps}
import cse.fitzgero.sorouting.model.population.{LocalPopulationOps, LocalRequest, LocalResponse}
import cse.fitzgero.sorouting.model.roadnetwork.costfunction.BPRCostFunctionType
import cse.fitzgero.sorouting.model.roadnetwork.local.{LocalGraph, LocalGraphOps}
import edu.ucdenver.fitzgero.lib.experiment.{ExperimentGlobalLog, ExperimentStepLog, StepStatus, SyncStep}

object UserEquilibriumRouting {

  type MATSimRunnerConfig = {
    def experimentInstanceDirectory: String
    def timeWindow: Int
    def startTime: LocalTime
    def endTime: Option[LocalTime]
  }

  val RoutingAlgorithmTimeout: Duration = 10 minutes

  val HHmmssFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
  val StartOfDay: LocalTime = LocalTime.MIN

  object Incremental extends SyncStep {
    val name: String = "[UserEquilibriumRouting:Incremental] Run Selfish Routing Using MSSP and MATSim"

    type StepConfig = MATSimRunnerConfig

    override def apply(config: StepConfig, log: ExperimentGlobalLog = Map()): Option[(StepStatus, ExperimentStepLog)] = Some {
      val t: Try[Map[String, String]] =
        Try {
          // load population file, convert to LocalRequest, solve shortest path, save back to XML file
          val populationXML: xml.Elem = XML.load(s"${config.experimentInstanceDirectory}/population.xml")
          val population: GenSeq[LocalRequest] = LocalPopulationOps.fromXML(populationXML)
          val networkXML: xml.Elem = XML.load(s"${config.experimentInstanceDirectory}/network.xml")

          // run an incremental algorithm here
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
          val incrementalStep: ((GenSeq[LocalResponse], Map[String, Long]), TimeGroup) => (GenSeq[LocalResponse], Map[String, Long]) =
            incrementalLoopLocal(population, networkXML, config.experimentInstanceDirectory, config.timeWindow)
          val result = timeGroups.foldLeft((GenSeq.empty[LocalResponse], Map.empty[String, Long]))(incrementalStep)

          // save final population which is the combined UE/SO population
          val graph = LocalGraphOps.readMATSimXML(networkXML, None, BPRCostFunctionType, config.timeWindow)
          val populationUE: xml.Elem = LocalPopulationOps.generateXMLResponses(graph, result._1)
          ExperimentFSOps.saveXmlDocType(s"${config.experimentInstanceDirectory}/population.xml", populationUE, ExperimentFSOps.PopulationDocType)

          // run MATSim one last time to produce a Snapshot file here
          MATSimOps.MATSimRun(config.experimentInstanceDirectory, config.startTime, endTime, config.timeWindow)

          val networkAvgTravelTime: String = MATSimOps.getNetworkAvgTravelTime(config.experimentInstanceDirectory)
          val populationTravelTime: String = MATSimOps.getPopulationAvgTravelTime(config.experimentInstanceDirectory)
          val outputLog = result._2.mapValues(_.toString) ++ Map(
            "experiment.result.traveltime.avg.network" -> networkAvgTravelTime,
            "experiment.result.traveltime.avg.population" -> populationTravelTime,
            "experiment.type" -> "User Equilibrium"
          )

          outputLog
        }

      ExperimentStepOps.resolveTry(t, Some(name))
    }

    protected def incrementalLoopLocal(population: GenSeq[LocalRequest], networkXML: xml.Elem, currentInstanceDirectory: String, timeWindow: Int)
      (accumulator : (GenSeq[LocalResponse], Map[String, Long]), timeGroup: TimeGroup): (GenSeq[LocalResponse], Map[String, Long]) = {

      val groupToRoute: GenSeq[LocalRequest] = population.filter(ExperimentOps.filterByTimeGroup(timeGroup))

      if (groupToRoute.isEmpty)
        accumulator
      else {
        // population with solved routes for all times before the current time group
        val snapshotPopulation: GenSeq[LocalResponse] = accumulator._1
        val previousPopGraph: LocalGraph = LocalGraphOps.readMATSimXML(networkXML, None, BPRCostFunctionType, timeWindow)
        val previousPopXML: xml.Elem = LocalPopulationOps.generateXMLResponses(previousPopGraph, snapshotPopulation)

        // create a temp snapshot directory with the required assets and the previousPopXML population.xml file
        val snapshotDirectory: String = ExperimentFSOps.importAssetsToTempDirectory(currentInstanceDirectory)
        ExperimentFSOps.saveXmlDocType(
          s"$snapshotDirectory/population.xml",
          previousPopXML,
          ExperimentFSOps.PopulationDocType
        )



        val snapshotURI: String = MATSimOps.MATSimRun(snapshotDirectory, StartOfDay, timeGroup.startRange, timeWindow)
        val snapshotXML: xml.Elem = XML.loadFile(snapshotURI)
        val snapshotGraph: LocalGraph = LocalGraphOps.readMATSimXML(networkXML, Some(snapshotXML), BPRCostFunctionType, timeWindow)

        ExperimentFSOps.recursiveDelete(snapshotDirectory)

        val future = MSSPLocalDijkstrasService.runService(snapshotGraph, groupToRoute)

        Await.result(future, RoutingAlgorithmTimeout) match {
          case Some(result) =>
            val updatedResult: GenSeq[LocalResponse] = accumulator._1 ++ result.result
            val updatedLogs: Map[String, Long] = ExperimentOps.sumLogs(accumulator._2, result.logs)

            println(s"${LocalDateTime.now} [UE Router] routed group at time ${timeGroup.startRange.format(HHmmssFormat)} with ${result.result.size} requests")

            (updatedResult, updatedLogs)
          case None =>
            accumulator
        }
      }
    }
  }
}
