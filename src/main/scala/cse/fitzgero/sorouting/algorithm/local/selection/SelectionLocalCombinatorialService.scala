package cse.fitzgero.sorouting.algorithm.local.selection

import cse.fitzgero.graph.algorithm.GraphService
import cse.fitzgero.sorouting.algorithm.local.sssp.SSSPLocalDijkstsasAlgorithmOps
import cse.fitzgero.sorouting.model.roadnetwork.local.LocalODPair

import scala.collection.{GenMap, GenSeq}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object SelectionLocalCombinatorialService extends GraphService {
  override type VertexId = SelectionLocalCombinatorialAlgorithm.VertexId
  override type EdgeId = SelectionLocalCombinatorialAlgorithm.EdgeId
  override type Graph = SelectionLocalCombinatorialAlgorithm.Graph
  type Path = SelectionLocalCombinatorialAlgorithm.Path
  type PathSegment = SelectionLocalCombinatorialAlgorithm.PathSegment
  type AlgorithmResult = SelectionLocalCombinatorialAlgorithm.AlgorithmResult

  // types for KSP service
  override type ServiceRequest = GenMap[LocalODPair, GenSeq[Path]]
  override type LoggingClass = Map[String, Long]
  case class ServiceResult(result: AlgorithmResult, logs: LoggingClass)
  override type ServiceConfig = Nothing

  override def runService(graph: Graph, request: ServiceRequest, config: Option[Nothing] = None): Future[Option[ServiceResult]] = Future {
    SelectionLocalCombinatorialAlgorithm.runAlgorithm(graph, request) match {
      case Some(result) =>

        val combinationCount = request.map(_._2.size.toLong).product
        val costEffect: Long = SSSPLocalDijkstsasAlgorithmOps.calculateAddedCost(graph, result.values).toLong

        val log = Map[String, Long](
          "algorithm.selection.local.runtime" -> runTime,
          "algorithm.selection.local.combinations" -> combinationCount,
          "algorithm.selection.local.cost.effect" -> costEffect,
          "algorithm.selection.local.success" -> 1L
        )
        Some(ServiceResult(result, log))
      case None => None
    }
  }
}
