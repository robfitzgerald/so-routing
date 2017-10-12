package cse.fitzgero.sorouting.algorithm.local.mssp

import java.time.Instant

import cse.fitzgero.graph.service.MultipleShortestPathsService
import cse.fitzgero.sorouting.algorithm.local.sssp.SSSPLocalDijkstrasService
import cse.fitzgero.sorouting.model.roadnetwork.local.LocalODPair

import scala.collection.{GenMap, GenSeq}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

object MSSPLocalDijkstrasService extends MultipleShortestPathsService { service =>
  override type VertexId = SSSPLocalDijkstrasService.VertexId
  override type EdgeId = SSSPLocalDijkstrasService.EdgeId
  override type Graph = SSSPLocalDijkstrasService.Graph
  override type ODPair = SSSPLocalDijkstrasService.ODPair
  type Path = SSSPLocalDijkstrasService.Path
  type PathSegment = SSSPLocalDijkstrasService.PathSegment
  type SSSPAlgorithmResult = SSSPLocalDijkstrasService.ServiceResult
  type MultipleShortestPathsResult = GenMap[ODPair, Path]
  override type LoggingClass = Map[String, Long]
  case class ServiceResult(logs: LoggingClass, result: MultipleShortestPathsResult)

  /**
    * runs a concurrent multiple shortest paths search on a set of origin/destination pairs
    * @param graph road network graph
    * @param odPairs a sequence of origin/destination pairs
    * @return a map from od pair to it's resulting path
    */
  override def runService(graph: Graph, odPairs: GenSeq[LocalODPair]): Future[Option[ServiceResult]] = Future {

    val future: Future[Iterator[Option[SSSPAlgorithmResult]]] =
      Future.sequence(odPairs.iterator.map(SSSPLocalDijkstrasService.runService(graph, _)))

    val resolved = Await.result(future, 10 seconds)
    val result = resolved.flatten.map(r => {
      (r.result.od, r.result.path)
    }).toMap

    val log = Map(
      "algorithm.mssp.local.runtime.total" -> runTime,
      "algorithm.mssp.local.success" -> 1L
    )
    Some(ServiceResult(log, result))
  }
}
