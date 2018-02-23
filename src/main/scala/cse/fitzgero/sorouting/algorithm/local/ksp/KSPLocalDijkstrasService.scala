package cse.fitzgero.sorouting.algorithm.local.ksp

import cse.fitzgero.graph.algorithm.GraphRoutingAlgorithmService
import cse.fitzgero.sorouting.algorithm.local.sssp.SSSPLocalDijkstrasAlgorithm
import cse.fitzgero.sorouting.model.population.LocalRequest
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import cse.fitzgero.graph.config.KSPBounds

object KSPLocalDijkstrasService extends GraphRoutingAlgorithmService {
  // types taken from SSSP
  override type VertexId = SSSPLocalDijkstrasAlgorithm.VertexId
  override type EdgeId = SSSPLocalDijkstrasAlgorithm.EdgeId
  override type Graph = SSSPLocalDijkstrasAlgorithm.Graph

  // types for KSP service
  override type ServiceRequest = LocalRequest
  override type LoggingClass = Map[String, Long]
  case class ServiceResult(request: LocalRequest, response: KSPLocalDijkstrasAlgorithm.AlgorithmResult, logs: LoggingClass)
  override type ServiceConfig = {
    def k: Int
    def kspBounds: Option[KSPBounds]
    def overlapThreshold: Double
  }

  /**
    * run the k-shortest paths algorithm as a concurrent service
    * @param graph underlying graph structure
    * @param request a single request
    * @param config an object that states the number of alternate paths, the stopping criteria, and any dissimilarity requirements
    * @return a future resolving to an optional service result
    */
  override def runService(graph: Graph, request: ServiceRequest, config: Option[ServiceConfig]): Future[Option[ServiceResult]] = Future {
    KSPLocalDijkstrasAlgorithm.runAlgorithm(graph, request.od, config) match {
      case Some(result) =>
        val log = Map[String, Long](
          "algorithm.ksp.local.runtime" -> this.runTime,
          "algorithm.ksp.local.success" -> 1L,
          "algorithm.ksp.local.k.requested" -> config.get.k,
          "algorithm.ksp.local.k.produced" -> result.paths.size
        )
        val serviceResult = ServiceResult(request, result, log)
//        println(s"[KSP] ksp service halting with Some result for request ${request.id}")
        Some(serviceResult)
      case None =>
//        println(s"[KSP] ksp service halting with None result for request ${request.id}")
        None
    }
  }
}
