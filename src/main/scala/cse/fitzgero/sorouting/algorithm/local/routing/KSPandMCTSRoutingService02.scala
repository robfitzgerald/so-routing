package cse.fitzgero.sorouting.algorithm.local.routing

import scala.collection.GenSeq
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}

import cse.fitzgero.graph.algorithm.GraphBatchRoutingAlgorithmService
import cse.fitzgero.graph.config.KSPBounds
import cse.fitzgero.sorouting.algorithm.local.ksp._
import cse.fitzgero.sorouting.algorithm.local.mksp._
import cse.fitzgero.sorouting.algorithm.local.selection._
import cse.fitzgero.sorouting.algorithm.local.selection.mcts.standardmcts.StandardMCTSService
import cse.fitzgero.sorouting.model.population.{LocalRequest, LocalResponse}
import cse.fitzgero.sorouting.model.roadnetwork.local._

object KSPandMCTSRoutingService02 extends GraphBatchRoutingAlgorithmService {
  // types taken from SSSP
  override type VertexId = KSPLocalDijkstrasAlgorithm.VertexId
  override type EdgeId = KSPLocalDijkstrasAlgorithm.EdgeId
  override type Graph = KSPLocalDijkstrasAlgorithm.Graph
  type AlgorithmResult = SelectionLocalCombinatorialAlgorithm.AlgorithmResult

  // types for Routing Service
  override type ServiceRequest = GenSeq[LocalRequest]
  override type LoggingClass = Map[String, Long]
  case class ServiceResult(result: GenSeq[LocalResponse], logs: LoggingClass)
  override type ServiceConfig = {
    def k: Int
    def kspBounds: Option[KSPBounds]
    def overlapThreshold: Double
    def coefficientCp: Double // 0 means flat mon
    def congestionRatioThreshold: Double
    def computationalLimit: Long // ms.
    def randomSeed: Long
  }

  /**
    * run the combinatorial k-shortest-paths optimal routing algorithm as a concurrent service
    * @param graph underlying graph structure
    * @param request a single request or a batch request
    * @param config an object that states the number of alternate paths, the stopping criteria, and any dissimilarity requirements for the ksp algorithm
    * @return a future resolving to a list of true optimal paths from a combinatorial search
    */
  override def runService(graph: LocalGraph, request: ServiceRequest, config: Option[ServiceConfig]): Future[Option[ServiceResult]] = {
    val promise = Promise[Option[ServiceResult]]()
    MKSPLocalDijkstrasService.runService(graph, request, config) map {
      case Some(ksp) =>
        println(s"[SelectionMCTS] finished ksp, now calling mcts")
        StandardMCTSService.runService(graph, ksp.result, config) map {
          case Some(selection) =>
            val logs = Map[String, Long](
              "algorithm.routing.local.success" -> 1L,
              "algorithm.routing.local.batch.request.size" -> request.size,
              "algorithm.routing.local.batch.completed" -> selection.result.size
            )
            println(s"[SelectionMCTS] completed and requests.size == result.size is ${request.size == selection.result.size}")
            promise.success(Some(ServiceResult(selection.result, logs ++ ksp.logs ++ selection.logs)))
          case None =>
            println(s"[SelectionMCTS] mcts returned None")
            promise.success(None)
        }
      case None =>
        println(s"[SelectionMCTS] ksp returned None")
        promise.success(None)
    }
    promise.future
  }
}
