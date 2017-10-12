package cse.fitzgero.sorouting.algorithm.local.ksp

import cse.fitzgero.graph.algorithm.GraphRoutingAlgorithmService
import cse.fitzgero.sorouting.algorithm.local.sssp.SSSPLocalDijkstrasAlgorithm
import cse.fitzgero.sorouting.model.roadnetwork.local.LocalODPair

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object KSPLocalDijkstrasService extends GraphRoutingAlgorithmService {
  // types taken from SSSP
  override type VertexId = SSSPLocalDijkstrasAlgorithm.VertexId
  override type EdgeId = SSSPLocalDijkstrasAlgorithm.EdgeId
  override type Graph = SSSPLocalDijkstrasAlgorithm.Graph
  type Path = SSSPLocalDijkstrasAlgorithm.Path
  type PathSegment = SSSPLocalDijkstrasAlgorithm.PathSegment
  type AlgorithmResult = KSPLocalDijkstrasAlgorithm.AlgorithmResult

  override type OD = LocalODPair
  override type LoggingClass = Map[String, Long]
  case class ServiceResult(result: AlgorithmResult, logs: LoggingClass)
  override type ServiceConfig = KSPLocalDijkstrasConfig

  override def runService(graph: Graph, request: OD, config: Option[KSPLocalDijkstrasConfig]): Future[Option[ServiceResult]] = Future {
    KSPLocalDijkstrasAlgorithm.runAlgorithm(graph, request) match {
      case Some(result) =>
        val log = Map(
          "algorithm.ksp.local.runtime" -> runTime,
          "algorithm.ksp.local.success" -> 1L
        )
        Some(ServiceResult(result, log))
      case None => None
    }
  }
}
