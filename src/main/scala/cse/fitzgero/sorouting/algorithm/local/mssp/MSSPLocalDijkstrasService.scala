package cse.fitzgero.sorouting.algorithm.local.mssp


import scala.collection.GenSeq
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

import cse.fitzgero.graph.algorithm.GraphBatchRoutingAlgorithmService
import cse.fitzgero.sorouting.algorithm.local.sssp.{SSSPLocalDijkstrasAlgorithm, SSSPLocalDijkstrasService}
import cse.fitzgero.sorouting.model.population.{LocalRequest, LocalResponse}

object MSSPLocalDijkstrasService extends GraphBatchRoutingAlgorithmService {
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
  override type ServiceConfig = Nothing
  case class ServiceResult(request: ServiceRequest, result: MultipleShortestPathsResult, logs: LoggingClass)


  /**
    * runs a concurrent multiple shortest paths search on a set of origin/destination pairs
    * @param graph road network graph
    * @param request a sequence of origin/destination pairs
    * @param config (ignored)
    * @return a map from od pairs to their resulting paths
    */
  override def runService(graph: Graph, request: ServiceRequest, config: Option[Nothing] = None): Future[Option[ServiceResult]] = {

    if (request.isEmpty) {
      Future { None }
    } else {
      val p: Promise[Option[ServiceResult]] = Promise()

      val future: Future[Iterator[Option[SSSPAlgorithmResult]]] =
        Future.sequence(request.iterator.map(SSSPLocalDijkstrasService.runService(graph, _)))

      future onComplete {
        case Success(resolved) =>

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

          println(s"[MSSP] completed with ${result.size} results")
          p.success(Some(ServiceResult(request, result, log)))
        case Failure(e) =>
          println(s"[MSSP] batch of SSSP failed for request(s): ${request.map(_.id).mkString(", ")}")
          p.success(None)
      }

      p.future
    }
  }
}
