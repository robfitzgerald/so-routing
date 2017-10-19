package cse.fitzgero.sorouting.algorithm.local.mksp

import cse.fitzgero.graph.algorithm.GraphBatchRoutingAlgorithmService
import cse.fitzgero.sorouting.algorithm.local.ksp.{KSPLocalDijkstrasAlgorithm, KSPLocalDijkstrasConfig, KSPLocalDijkstrasService}
import cse.fitzgero.sorouting.model.population.LocalRequest

import scala.collection.{GenMap, GenSeq}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

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
  override type ServiceConfig = KSPLocalDijkstrasConfig

  /**
    * run multiple k-shortest paths algorithms as a batch service
    * @param graph underlying graph structure
    * @param request a batch request
    * @param config an object that states the number of alternate paths, the stopping criteria, and any dissimilarity requirements
    * @return a future resolving to an optional service result
    */
  override def runService(graph: Graph, request: ServiceRequest, config: Option[KSPLocalDijkstrasConfig]): Future[Option[ServiceResult]] = Future {
    val future: Future[Iterator[Option[KSPResult]]] =
      Future.sequence(request.iterator.map(KSPLocalDijkstrasService.runService(graph, _, config)))

    val resolved: Seq[KSPResult] = Await.result(future, 60 seconds).flatten.toSeq

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

    Some(ServiceResult(request, result, logs))
  }
}
