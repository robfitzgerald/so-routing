package cse.fitzgero.sorouting.algorithm.local.selection

import scala.collection.{GenMap, GenSeq}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import org.apache.spark.SparkContext

import cse.fitzgero.graph.algorithm.GraphService
import cse.fitzgero.sorouting.algorithm.local.mssp.MSSPLocalDijkstsasAlgorithmOps
import cse.fitzgero.sorouting.model.path.SORoutingPathSegment
import cse.fitzgero.sorouting.model.population.{LocalRequest, LocalResponse}

object SelectionSparkCombinatorialService extends GraphService {
  override type VertexId = SelectionSparkCombinatorialAlgorithm.VertexId
  override type EdgeId = SelectionSparkCombinatorialAlgorithm.EdgeId
  override type Graph = SelectionSparkCombinatorialAlgorithm.Graph
  type Path = List[SORoutingPathSegment]
  type AlgorithmResult = SelectionSparkCombinatorialAlgorithm.AlgorithmResult

  // types for KSP service
  override type ServiceRequest = GenMap[LocalRequest, GenSeq[Path]]
  override type LoggingClass = Map[String, Long]
  case class ServiceResult(result: GenSeq[LocalResponse], logs: LoggingClass)
  override type ServiceConfig = SparkContext

  /**
    * run the combinatorial selection algorithm as a concurrent service
    * @param graph underlying graph structure
    * @param request a map from requests to their sets of alternate paths as found by an alternate paths solver
    * @param config (unused)
    * @return a future resolving to an optional set of optimal paths
    */
  override def runService(graph: Graph, request: ServiceRequest, config: Option[SparkContext]): Future[Option[ServiceResult]] = Future {
    val algRequest = request.map(req => (req._1.od, req._2))
    SelectionSparkCombinatorialAlgorithm.runAlgorithm(graph, algRequest, config) match {
      case Some(result) =>

        val repackagedResponses: GenSeq[LocalResponse] =
          request.flatMap(req =>
            if (result.isDefinedAt(req._1.od))
              Some(LocalResponse(req._1, result(req._1.od)))
            else
              None
          ).toSeq

        val expectedCombinationCount: Long = request.map(_._2.size.toLong).product
        val costEffect: Long = MSSPLocalDijkstsasAlgorithmOps.calculateAddedCost(graph, repackagedResponses).toLong

        val log = Map[String, Long](
          "algorithm.selection.local.runtime" -> runTime,
          "algorithm.selection.local.combinations" -> expectedCombinationCount,
          "algorithm.selection.local.cost.effect" -> costEffect,
          "algorithm.selection.local.success" -> 1L
        )
        Some(ServiceResult(repackagedResponses, log))
      case None => None
    }
  }
}
