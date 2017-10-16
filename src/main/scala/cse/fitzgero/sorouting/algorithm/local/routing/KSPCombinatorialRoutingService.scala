package cse.fitzgero.sorouting.algorithm.local.routing

import cse.fitzgero.graph.algorithm.GraphRoutingAlgorithmService
import cse.fitzgero.sorouting.algorithm.local.ksp._
import cse.fitzgero.sorouting.algorithm.local.mksp._
import cse.fitzgero.sorouting.algorithm.local.selection._
import cse.fitzgero.sorouting.model.roadnetwork.local._

import scala.concurrent.{Future, Promise}
import scala.concurrent.ExecutionContext.Implicits.global

object KSPCombinatorialRoutingService extends GraphRoutingAlgorithmService {
  // types taken from SSSP
  override type VertexId = KSPLocalDijkstrasAlgorithm.VertexId
  override type EdgeId = KSPLocalDijkstrasAlgorithm.EdgeId
  override type Graph = KSPLocalDijkstrasAlgorithm.Graph
  type AlgorithmResult = SelectionLocalCombinatorialAlgorithm.AlgorithmResult

  // types for Routing Service
  override type ServiceRequest = LocalODBatch
  override type LoggingClass = Map[String, Long]
  case class ServiceResult(result: AlgorithmResult, logs: LoggingClass)
  override type ServiceConfig = KSPLocalDijkstrasConfig

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
        SelectionLocalCombinatorialService.runService(graph, ksp.result) map {
          case Some(selection) =>
            promise.success(Some(ServiceResult(selection.result, ksp.logs ++ selection.logs)))
          case None =>
            promise.success(None)
        }
      case None =>
        promise.success(None)
    }
    promise.future
  }
}
