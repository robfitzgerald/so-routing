package cse.fitzgero.sorouting.algorithm.local.mssp


import scala.collection.GenSeq
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future, Promise}
import scala.util.{Failure, Success}

import cse.fitzgero.graph.algorithm.GraphBatchRoutingAlgorithmService
import cse.fitzgero.sorouting.algorithm.local.sssp.{SSSPLocalDijkstrasAlgorithm, SSSPLocalDijkstrasService}
import cse.fitzgero.sorouting.model.population.{LocalRequest, LocalResponse}
import scala.concurrent.duration._

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
  override type ServiceConfig = {
    def blockSize: Int
  }

  val DefaultBlockSize: Int = 8

  case class ServiceResult(request: ServiceRequest, result: MultipleShortestPathsResult, logs: LoggingClass)


  /**
    * runs a concurrent multiple shortest paths search on a set of origin/destination pairs
    * @param graph road network graph
    * @param request a sequence of origin/destination pairs
    * @param config (ignored)
    * @return a map from od pairs to their resulting paths
    */
  override def runService(graph: Graph, request: ServiceRequest, config: Option[ServiceConfig] = None): Future[Option[ServiceResult]] = {

    val blockSize: Int = config match {
      case Some(conf) => conf.blockSize
      case None => DefaultBlockSize
    }

    def groupByParallelBlockSize[T](xs: GenSeq[T], blockSize: Int): List[List[T]] = xs.toList.sliding(blockSize, blockSize).toList

    def runBlocks(blocks: List[List[LocalRequest]], solution: List[SSSPAlgorithmResult] = List()): Future[List[SSSPAlgorithmResult]] =
      if (blocks.isEmpty) Future(solution)
      else {
        Future.sequence(blocks.head.iterator.map(SSSPLocalDijkstrasService.runService(graph, _))) flatMap {
          blockSolved =>
            runBlocks(blocks.tail, solution ++ blockSolved.flatten)
        }
      }

    if (request.isEmpty) {
      Future { None }
    } else {
      val p: Promise[Option[ServiceResult]] = Promise()

      runBlocks(groupByParallelBlockSize(request, blockSize)) onComplete {
        case Success(resolved) =>

          val result = resolved.map(r => {
            LocalResponse(r.request, r.response.path)
          })

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
