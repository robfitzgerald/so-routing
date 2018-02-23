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
import cse.fitzgero.sorouting.model.population.{LocalPopulationNormalGenerator, LocalRequest, LocalResponse}
import cse.fitzgero.sorouting.model.roadnetwork.costfunction.BPRCostFunctionType
import cse.fitzgero.sorouting.model.roadnetwork.local.LocalGraphOps.EdgesWithFlows
import cse.fitzgero.sorouting.model.roadnetwork.local.{LocalGraph, LocalGraphOps}
import edu.ucdenver.fitzgero.lib.experiment.{ExperimentGlobalLog, ExperimentStepLog, StepStatus, SyncStep}

object UserEquilibriumRouting {

  type MATSimRunnerConfig = {
    def experimentInstanceDirectory: String
    def timeWindow: Int
    def startTime: LocalTime
    def endTime: Option[LocalTime]
    def blockSize: Int
  }

  val RoutingAlgorithmTimeout: Duration = 1 hour

  val HHmmssFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
  val StartOfDay: LocalTime = LocalTime.MIN

  object Incremental extends SyncStep {
    val name: String = "[UserEquilibriumRouting:Incremental] Run Selfish Routing Using MSSP and MATSim"

    type StepConfig = MATSimRunnerConfig

    override def apply(config: StepConfig, log: ExperimentGlobalLog = Map()): Option[(StepStatus, ExperimentStepLog)] = Some {
      val t: Try[Map[String, String]] =
        Try {
          // load population file, convert to LocalRequest
          val populationXML: xml.Elem = XML.load(s"${config.experimentInstanceDirectory}/population.xml")
          val population: GenSeq[LocalRequest] = LocalPopulationNormalGenerator.fromXML(populationXML)
          val networkXML: xml.Elem = XML.load(s"${config.experimentInstanceDirectory}/network.xml")

          // load endTime, or set as default to end of day
          val endTime = config.endTime match {
            case Some(time) => time
            case None => LocalTime.MAX
          }

          // generate time groups from 0 to config.endTime by config.timeWindow
          val timeGroups: Iterator[TimeGroup] =
            (StartOfDay.toSecondOfDay +: (config.startTime.toSecondOfDay until endTime.toSecondOfDay by config.timeWindow))
              .sliding(2)
              .map {
                vec =>
                  TimeGroup(
                    LocalTime.ofSecondOfDay(vec(0)),
                    LocalTime.ofSecondOfDay(vec(1))
                  )
              }

          // set up incrementalStep function (pass in idempotent assets)
          val incrementalStep: ((GenSeq[LocalResponse], Map[String, Long]), TimeGroup) => (GenSeq[LocalResponse], Map[String, Long]) =
            incrementalLoopLocal(population, networkXML, config.experimentInstanceDirectory, config.timeWindow)

          // run incremental phase for each time window
          val result = timeGroups.foldLeft((GenSeq.empty[LocalResponse], Map.empty[String, Long]))(incrementalStep)

          // save final population (routed) to disk
          val graph = LocalGraphOps.readMATSimXML(EdgesWithFlows, networkXML, None, BPRCostFunctionType, config.timeWindow)
          val populationUE: xml.Elem = LocalPopulationNormalGenerator.generateXMLResponses(graph, result._1)
          ExperimentFSOps.saveXmlDocType(s"${config.experimentInstanceDirectory}/population.xml", populationUE, ExperimentFSOps.PopulationDocType)

          // run MATSim one last time to produce a Snapshot file here
          MATSimOps.MATSimRun(config.experimentInstanceDirectory, config.startTime, endTime, config.timeWindow)

          // extract travel time data from disk
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

    /**
      * run on each time group to solve an incremental routing solution
      * @param population the complete population
      * @param networkXML the road network data
      * @param currentInstanceDirectory the experiment instance directory on the file system
      * @param timeWindow the batch duration of each routing batch, in seconds
      * @param accumulator collection of assigned route data that is the accumulator between routing increments
      * @param timeGroup the start time and end time of the current time window
      * @return the updated accumulator
      */
    protected def incrementalLoopLocal(population: GenSeq[LocalRequest], networkXML: xml.Elem, currentInstanceDirectory: String, timeWindow: Int)
      (accumulator : (GenSeq[LocalResponse], Map[String, Long]), timeGroup: TimeGroup): (GenSeq[LocalResponse], Map[String, Long]) = {

      // grab the group of drivers we will be solving routing for in this increment
      val groupToRoute: GenSeq[LocalRequest] = population.filter(ExperimentOps.filterByTimeGroup(timeGroup))

      if (groupToRoute.isEmpty)
        accumulator
      else {

        // grab the group of drivers who have already had routes solved, from the beginning of the day, up until this time group
        val snapshotPopulation: GenSeq[LocalResponse] = accumulator._1
        val previousPopGraph: LocalGraph = LocalGraphOps.readMATSimXML(EdgesWithFlows, networkXML, None, BPRCostFunctionType, timeWindow)
        val previousPopXML: xml.Elem = LocalPopulationNormalGenerator.generateXMLResponses(previousPopGraph, snapshotPopulation)

        // run a MATSim simulation from the beginning of the day up to the beginning of this time group, and grab the final snapshot
        val snapshotDirectory: String = ExperimentFSOps.importAssetsToTempDirectory(currentInstanceDirectory)
        ExperimentFSOps.saveXmlDocType(
          s"$snapshotDirectory/population.xml",
          previousPopXML,
          ExperimentFSOps.PopulationDocType
        )
        val snapshotURI: String = MATSimOps.MATSimRun(snapshotDirectory, StartOfDay, timeGroup.startRange, timeWindow)
        val snapshotXML: xml.Elem = XML.loadFile(snapshotURI)

        // load the road network graph with the flows from the MATSim snapshot
        val snapshotGraph: LocalGraph = LocalGraphOps.readMATSimXML(EdgesWithFlows, networkXML, Some(snapshotXML), BPRCostFunctionType, timeWindow)
        ExperimentFSOps.recursiveDelete(snapshotDirectory)

        // run multiple source shortest paths on this timegroup over the snapshot graph
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
