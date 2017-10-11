package cse.fitzgero.sorouting.algorithm.local.mssp

import java.time.Instant

import cse.fitzgero.graph.algorithm.{GraphAlgorithm, MultipleShortestPathsAlgorithm}
import cse.fitzgero.graph.service.MultipleShortestPathsService
import cse.fitzgero.sorouting.algorithm.local.sssp.SSSPLocalDijkstrasService
import cse.fitzgero.sorouting.model.roadnetwork.local.{LocalGraph, LocalODPair}

import scala.collection.{GenMap, GenSeq}
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object MSSPLocalDijkstrasService extends MultipleShortestPathsService { service =>
  override type VertexId = SSSPLocalDijkstrasService.VertexId
  override type EdgeId = SSSPLocalDijkstrasService.EdgeId
  override type Graph = SSSPLocalDijkstrasService.Graph
  override type ODPair = SSSPLocalDijkstrasService.ODPair
  type Path = SSSPLocalDijkstrasService.Path
  type PathSegment = SSSPLocalDijkstrasService.PathSegment
  type AlgorithmResult = SSSPLocalDijkstrasService.SSSPLocalDijkstrasServiceResult
//  case class MultipleShortestPathsResult(ods: GenMap[ODPair, Path]) extends MultipleShortestPathsResult
  type MultipleShortestPathsResult = GenMap[ODPair, Path]
  case class MultipleShortestPathServiceResult(logs: LoggingClass, result: MultipleShortestPathsResult)

  override type LoggingClass = Map[String, Long]
  // service:
  // run algorithm, spit out runTime

  override def runService(graph: Graph, odPairs: GenSeq[LocalODPair]): Future[Option[MultipleShortestPathServiceResult]] = Future {
    val startTime = Instant.now.toEpochMilli
    def callSSSP(od: ODPair): Future[Option[AlgorithmResult]] =
      SSSPLocalDijkstrasService.runService(graph, od)

    val future: Future[Iterator[Option[AlgorithmResult]]] = Future.sequence(odPairs.iterator map callSSSP)
    val result = Await.result(future, 10 seconds).flatten.map(r => {
      (r.result.od, r.result.path)
    }).toMap

    val runTime = Instant.now.toEpochMilli - startTime
    val log = Map(
      "algorithm.mssp.local.runtime.total" -> runTime,
      "algorithm.mssp.local.success" -> 1L
    )
    Some(MultipleShortestPathServiceResult(log, result))
  }
}
