package cse.fitzgero.sorouting.algorithm.local.sssp

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import cse.fitzgero.graph.algorithm.GraphRoutingAlgorithmService
import cse.fitzgero.sorouting.model.population.{LocalRequest, LocalResponse}
import cse.fitzgero.sorouting.model.roadnetwork.local._

object SSSPLocalDijkstrasService extends GraphRoutingAlgorithmService {
  type VertexId = String
  type EdgeId = String
  type RequestId = String
  type Graph = LocalGraph
  override type ServiceRequest = LocalRequest
  override type LoggingClass = Map[String, Long]
  override type ServiceConfig = Nothing
  case class ServiceResult(request: LocalRequest, response: LocalResponse, logs: LoggingClass)

  /**
    * runs the concurrent shortest path service as a future and with details stored in a log
    * @param graph  the road network graph
    * @param request the origin and destination pair for this search
    * @param config (ignored)
    * @return a shortest path and logging data, or nothing
    */
  override def runService(graph: Graph, request: ServiceRequest, config: Option[Nothing] = None): Future[Option[ServiceResult]] = Future {
    SSSPLocalDijkstrasAlgorithm.runAlgorithm(graph, request.od) match {
      case Some(result) =>
        val log = Map(
          "algorithm.sssp.local.runtime" -> runTime,
          "algorithm.sssp.local.success" -> 1L
        )
        Some(ServiceResult(request, LocalResponse(request, result.path), log))
      case None => None
    }
  }
}
