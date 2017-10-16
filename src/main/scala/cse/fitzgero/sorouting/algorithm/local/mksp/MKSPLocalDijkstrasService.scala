package cse.fitzgero.sorouting.algorithm.local.mksp

import cse.fitzgero.graph.algorithm.GraphRoutingAlgorithmService
import cse.fitzgero.sorouting.algorithm.local.ksp.{KSPLocalDijkstrasAlgorithm, KSPLocalDijkstrasConfig, KSPLocalDijkstrasService}
import cse.fitzgero.sorouting.model.roadnetwork.local.{LocalODBatch, LocalODPair}

import scala.collection.{GenMap, GenSeq}
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

object MKSPLocalDijkstrasService extends GraphRoutingAlgorithmService {
  // KSP Algorithm Types
  override type VertexId = KSPLocalDijkstrasAlgorithm.VertexId
  override type EdgeId = KSPLocalDijkstrasAlgorithm.EdgeId
  override type Graph = KSPLocalDijkstrasAlgorithm.Graph
  type Path = KSPLocalDijkstrasAlgorithm.Path
  type PathSegment = KSPLocalDijkstrasAlgorithm.PathSegment
  type KSPResult = KSPLocalDijkstrasService.ServiceResult


  // types for MKSP service
  override type ServiceRequest = LocalODBatch
  override type LoggingClass = Map[String, Long]
  type KSPMap = GenMap[LocalODPair, GenSeq[Path]]
  case class ServiceResult(result: KSPMap, logs: LoggingClass)
  override type ServiceConfig = KSPLocalDijkstrasConfig

  /**
    *
    * @param graph underlying graph structure
    * @param request a batch request
    * @param config an object that states the number of alternate paths, the stopping criteria, and any dissimilarity requirements
    * @return a future resolving to an optional service result
    */
  override def runService(graph: Graph, request: ServiceRequest, config: Option[KSPLocalDijkstrasConfig]): Future[Option[ServiceResult]] = Future {
    val future: Future[Iterator[Option[KSPResult]]] =
      Future.sequence(request.ods.iterator.map(KSPLocalDijkstrasService.runService(graph, _, config)))

    val resolved: Seq[KSPResult] = Await.result(future, 60 seconds).flatten.toSeq

    val result: GenMap[KSPLocalDijkstrasService.ServiceRequest, GenSeq[Path]] =
      resolved
        .map(kspResult => {
          (kspResult.result.od, kspResult.result.paths)
        }).toMap

    val kRequested: Long = resolved.map(_.logs("algorithm.ksp.local.k.requested")).sum
    val kProduced: Long = resolved.map(_.logs("algorithm.ksp.local.k.produced")).sum

    val logs = Map[String, Long](
      "algorithm.mksp.local.runtime.total" -> runTime,
      "algorithm.mksp.local.batch.request.size" -> request.ods.size,
      "algorithm.mksp.local.batch.completed" -> result.size,
      "algorithm.mksp.local.k" -> config.get.k,
      "algorithm.mksp.local.k.requested" -> kRequested,
      "algorithm.mksp.local.k.produced" -> kProduced,
      "algorithm.mksp.local.success" -> 1L
    )

    Some(ServiceResult(result, logs))
  }
}
