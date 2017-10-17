package cse.fitzgero.sorouting.algorithm.local.mssp


import cse.fitzgero.graph.algorithm.GraphBatchRoutingAlgorithmService
import cse.fitzgero.sorouting.algorithm.local.sssp.{SSSPLocalDijkstrasAlgorithm, SSSPLocalDijkstrasService}
import cse.fitzgero.sorouting.model.population.{LocalRequest, LocalResponse}

import scala.collection.{GenMap, GenSeq}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

object MSSPLocalDijkstrasService extends GraphBatchRoutingAlgorithmService { service =>
  // types taken from SSSP
  override type VertexId = SSSPLocalDijkstrasService.VertexId
  override type EdgeId = SSSPLocalDijkstrasService.EdgeId
  override type Graph = SSSPLocalDijkstrasService.Graph
  type Path = SSSPLocalDijkstrasAlgorithm.Path
  type PathSegment = SSSPLocalDijkstrasAlgorithm.PathSegment
  type SSSPAlgorithmResult = SSSPLocalDijkstrasService.ServiceResult
  type MultipleShortestPathsResult = GenSeq[LocalResponse]
  // MSSP types
  override type ServiceRequest = GenSeq[LocalRequest]
  override type LoggingClass = Map[String, Long]
  override type ServiceConfig = Any
  case class ServiceResult(request: ServiceRequest, result: MultipleShortestPathsResult, logs: LoggingClass)


  /**
    * runs a concurrent multiple shortest paths search on a set of origin/destination pairs
    * @param graph road network graph
    * @param request a sequence of origin/destination pairs
    * @param config (ignored)
    * @return a map from od pair to it's resulting path
    */
  override def runService(graph: Graph, request: ServiceRequest, config: Option[Any] = None): Future[Option[ServiceResult]] = Future {

    val future: Future[Iterator[Option[SSSPAlgorithmResult]]] =
      Future.sequence(request.iterator.map(SSSPLocalDijkstrasService.runService(graph, _)))

    val resolved = Await.result(future, 60 seconds)
    val result = resolved.flatten.map(r => {
      LocalResponse(r.request, r.response.path)
    }).toSeq

    val costEffect: Long = MSSPLocalDijkstsasAlgorithmOps.calculateAddedCost(graph, result).toLong

    val log = Map[String, Long](
      "algorithm.mssp.local.runtime.total" -> runTime,
      "algorithm.mssp.local.batch.request.size" -> request.size,
      "algorithm.mssp.local.batch.completed" -> result.size,
      "algorithm.mssp.local.cost.effect" -> costEffect,
      "algorithm.mssp.local.success" -> 1L
    )
    Some(ServiceResult(request, result, log))
  }
}
