package cse.fitzgero.sorouting.algorithm.local.mssp


import cse.fitzgero.graph.algorithm.GraphRoutingAlgorithmService
import cse.fitzgero.graph.basicgraph.BasicODBatch
import cse.fitzgero.sorouting.algorithm.local.sssp.{SSSPLocalDijkstrasAlgorithm, SSSPLocalDijkstrasService}
import cse.fitzgero.sorouting.model.roadnetwork.local.LocalODBatch

import scala.collection.{GenMap, GenSeq}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

object MSSPLocalDijkstrasService extends GraphRoutingAlgorithmService { service =>
  // types taken from SSSP
  override type VertexId = SSSPLocalDijkstrasService.VertexId
  override type EdgeId = SSSPLocalDijkstrasService.EdgeId
  override type Graph = SSSPLocalDijkstrasService.Graph
  type Path = SSSPLocalDijkstrasAlgorithm.Path
  type PathSegment = SSSPLocalDijkstrasAlgorithm.PathSegment
  type SSSPAlgorithmResult = SSSPLocalDijkstrasService.ServiceResult
  type MultipleShortestPathsResult = GenMap[SSSPLocalDijkstrasService.OD, Path]
  // MSSP types
  override type OD = LocalODBatch
  override type LoggingClass = Map[String, Long]
  override type ServiceConfig = Any
  case class ServiceResult(logs: LoggingClass, result: MultipleShortestPathsResult)


  /**
    * runs a concurrent multiple shortest paths search on a set of origin/destination pairs
    * @param graph road network graph
    * @param request a sequence of origin/destination pairs
    * @param config (ignored)
    * @return a map from od pair to it's resulting path
    */
  override def runService(graph: Graph, request: OD, config: Option[Any] = None): Future[Option[ServiceResult]] = Future {

    val future: Future[Iterator[Option[SSSPAlgorithmResult]]] =
      Future.sequence(request.ods.iterator.map(SSSPLocalDijkstrasService.runService(graph, _)))

    val resolved = Await.result(future, 10 seconds)
    val result = resolved.flatten.map(r => {
      (r.result.od, r.result.path)
    }).toMap

    val log = Map(
      "algorithm.mssp.local.runtime.total" -> runTime,
      "algorithm.mssp.local.batch.requested" -> request.ods.size,
      "algorithm.mssp.local.batch.completed" -> result.size,
      "algorithm.mssp.local.success" -> 1L
    )
    Some(ServiceResult(log, result))
  }
}
