package cse.fitzgero.sorouting.algorithm.local.mksp

import scala.collection.{GenMap, GenSeq}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

import cse.fitzgero.graph.algorithm.GraphBatchRoutingAlgorithmService
import cse.fitzgero.graph.config.KSPBounds
import cse.fitzgero.sorouting.algorithm.local.ksp.{KSPLocalDijkstrasAlgorithm, KSPLocalDijkstrasService}
import cse.fitzgero.sorouting.model.population.LocalRequest

object MKSPLocalDijkstrasService extends GraphBatchRoutingAlgorithmService {
  // KSP Algorithm Types
  override type VertexId = KSPLocalDijkstrasAlgorithm.VertexId
  override type EdgeId = KSPLocalDijkstrasAlgorithm.EdgeId
  override type Graph = KSPLocalDijkstrasAlgorithm.Graph
  type Path = KSPLocalDijkstrasAlgorithm.Path
  type KSPResult = KSPLocalDijkstrasService.ServiceResult


  // types for MKSP service
  override type ServiceRequest = GenSeq[LocalRequest]
  override type LoggingClass = Map[String, Long]
  type KSPMap = GenMap[LocalRequest, GenSeq[Path]]
  case class ServiceResult(request: ServiceRequest, result: KSPMap, logs: LoggingClass)
  override type ServiceConfig = {
    def k: Int
    def kspBounds: Option[KSPBounds]
    def overlapThreshold: Double
  }

  /**
    * run multiple k-shortest paths algorithms as a batch service
    * @param graph underlying graph structure
    * @param request a batch request
    * @param config an object that states the number of alternate paths, the stopping criteria, and any dissimilarity requirements
    * @return a future resolving to an optional service result
    */
  override def runService(graph: Graph, request: ServiceRequest, config: Option[ServiceConfig]): Future[Option[ServiceResult]] = {
    if (request.isEmpty)
      Future{ None }
      // TODO: handle the size==1 case. would we return 1 shortest path, or is it incorrect for mksp to provide that result?
    else {
      val p: Promise[Option[ServiceResult]] = Promise()
      val future: Future[Iterator[Option[KSPResult]]] =
        Future.sequence(request.iterator.map(KSPLocalDijkstrasService.runService(graph, _, config)))
      println(s"[MKSP] running ${request.size} KSP services")

      future onComplete {
        case Success(mkspResult) =>
          val resolved = mkspResult.flatten.toSeq
          val result: KSPMap =
            resolved
              .map(kspResult => {
                (kspResult.request, kspResult.response.paths)
              }).toMap

          val kRequested: Long = resolved.map(_.logs("algorithm.ksp.local.k.requested")).sum
          val kProduced: Long = resolved.map(_.logs("algorithm.ksp.local.k.produced")).sum
          val hasAlternates: Int = result.count(_._2.size > 1)

          val logs = Map[String, Long](
            "algorithm.mksp.local.runtime.total" -> runTime,
            "algorithm.mksp.local.request.size" -> request.size,
            "algorithm.mksp.local.result.size" -> result.size,
            "algorithm.mksp.local.hasalternates" -> hasAlternates,
            "algorithm.mksp.local.k.requested" -> kRequested,
            "algorithm.mksp.local.k.produced" -> kProduced,
            "algorithm.mksp.local.success" -> 1L
          )
          println(s"[MKSP] completed with ${result.size} results")
//          Some(ServiceResult(request, result, logs))
          p.success(Some(ServiceResult(request, result, logs)))
        case Failure(e) =>
          println(s"[MKSP] failure due to: ${e.getMessage}")
          p.failure(e)
      }
      p.future
    }
  }
}
