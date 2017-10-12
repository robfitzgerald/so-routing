package cse.fitzgero.sorouting.algorithm.local.sssp

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import cse.fitzgero.graph.algorithm.GraphRoutingAlgorithmService
import cse.fitzgero.sorouting.model.roadnetwork.local._

object SSSPLocalDijkstrasService extends GraphRoutingAlgorithmService {
  type VertexId = String
  type EdgeId = String
  type RequestId = String
  type Graph = LocalGraph
  override type OD = LocalODPair
  override type LoggingClass = Map[String, Long]
  type AlgorithmResult = SSSPLocalDijkstrasAlgorithm.AlgorithmResult
  override type ServiceConfig = Any
  case class ServiceResult(result: AlgorithmResult, logs: LoggingClass)

  /**
    * runs the concurrent shortest path service as a future and with details stored in a log
    *
    * @param graph  the road network graph
    * @param oDPair the origin and destination pair for this search
    * @param config (ignored)
    * @return a shortest path and logging data, or nothing
    */
  override def runService(graph: Graph, oDPair: OD, config: Option[Any] = None): Future[Option[ServiceResult]] = Future {
    SSSPLocalDijkstrasAlgorithm.runAlgorithm(graph, oDPair) match {
      case Some(result) =>
        val log = Map(
          "algorithm.sssp.local.runtime" -> runTime,
          "algorithm.sssp.local.success" -> 1L
        )
        Some(ServiceResult(result, log))
      case None => None
    }
  }
}
