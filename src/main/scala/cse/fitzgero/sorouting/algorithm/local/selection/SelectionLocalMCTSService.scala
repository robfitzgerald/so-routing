package cse.fitzgero.sorouting.algorithm.local.selection

import scala.collection.{GenMap, GenSeq}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

import cse.fitzgero.graph.algorithm.GraphService
import cse.fitzgero.sorouting.algorithm.local.mssp.{MSSPLocalDijkstrasService, MSSPLocalDijkstsasAlgorithmOps}
import cse.fitzgero.sorouting.model.path.SORoutingPathSegment
import cse.fitzgero.sorouting.model.population.{LocalRequest, LocalResponse}
import cse.fitzgero.sorouting.model.roadnetwork.local.{LocalEdge, LocalGraph}

object SelectionLocalMCTSService extends GraphService {
  override type VertexId = SelectionLocalCombinatorialAlgorithm.VertexId
  override type EdgeId = SelectionLocalCombinatorialAlgorithm.EdgeId
  override type Graph = SelectionLocalCombinatorialAlgorithm.Graph
  type Path = List[SORoutingPathSegment]
  type AlgorithmResult = SelectionLocalCombinatorialAlgorithm.AlgorithmResult

  // types for KSP service
  override type ServiceRequest = GenMap[LocalRequest, GenSeq[Path]]
  override type LoggingClass = Map[String, Long]
  case class ServiceResult(result: GenSeq[LocalResponse], logs: LoggingClass)
  override type ServiceConfig = {
    def coefficientCp: Double // 0 means flat mon
    def congestionRatioThreshold: Double
    def computationalLimit: Long // ms.
  }

  val MSSPComputationalLimit: Duration = 10 minutes

  /**
    * run the combinatorial selection algorithm as a concurrent service
    * @param graph underlying graph structure
    * @param request a map from requests to their sets of alternate paths as found by an alternate paths solver
    * @param config (unused)
    * @return a future resolving to an optional set of optimal paths
    */
  override def runService(graph: Graph, request: ServiceRequest, config: Option[ServiceConfig] = None): Future[Option[ServiceResult]] = Future {
    val algRequest = request.map(req => (req._1.od, req._2))
    SelectionLocalMCTSAlgorithm.runAlgorithm(graph, algRequest, config) match {
      case Some(result) =>

        val response: AlgorithmResult =
          if (result.size == request.size) {
            // we got to a terminal node and we have a complete result
            println(s"[MCTS] completed and requests.size == result.size is true")
            result
          } else {
            // we didn't get to a terminal node; we need to fill in the remaining requests with shortest paths
            println(s"[MCTS] completed and requests.size == result.size is false: calling MSSP for remaining")
            // for each edge, the contribution from the mcts group in edge flows
            val edgesAndFlows: GenMap[String, Int] =
              result.flatMap {
                req =>
                  req._2.map(_.edgeId)
              }.groupBy(identity)
              .mapValues(_.size)

            // update graph from MCTS result
            val updatedGraph: LocalGraph =
              edgesAndFlows.foldLeft(graph){
                (acc, mctsEdgeData) =>
                  acc.edgeById(mctsEdgeData._1) match {
                    case None => acc
                    case Some(edgeToUpdate) =>
                      val updatedEdge = LocalEdge.modifyFlow(edgeToUpdate, mctsEdgeData._2)
                      acc.updateEdge(mctsEdgeData._1, updatedEdge)
                  }
              }

            // run MSSP with remaining requests on updated graph
            val remaining = request.filter { req => !result.isDefinedAt(req._1.od) }
            val future = MSSPLocalDijkstrasService.runService(updatedGraph, remaining.toSeq.map{ req => req._1 })
            val extrasResult = Await.result(future, MSSPComputationalLimit)

            println(s"[MCTS] MSSP completed for remaining ${remaining.size} unrouted vehicles")
            // merge and return
            // result: GenMap[LocalODPair, Path]
            // extrasResult: case class ServiceResult(request: ServiceRequest, result: GenSeq[LocalResponse], logs: LoggingClass)
            //   where case class LocalResponse(request: LocalRequest, path: List[SORoutingPathSegment])
            extrasResult match {
              case None =>
                result
              case Some(extrasResolved) =>
                result ++ extrasResolved.result.map{ res => (res.request.od, res.path)}
            }
          }

        val repackagedResponses: GenSeq[LocalResponse] =
          request.flatMap(req =>
            if (response.isDefinedAt(req._1.od))
              Some(LocalResponse(req._1, response(req._1.od)))
            else
              None
          ).toSeq

        val costEffect: Long = MSSPLocalDijkstsasAlgorithmOps.calculateAddedCost(graph, repackagedResponses).toLong
        val completeSolutionFromMCTS: Long = if (result.size == request.size) 1L else 0L

        val log = Map[String, Long](
          "algorithm.selection.local.runtime" -> runTime,
          "algorithm.selection.local.cost.effect" -> costEffect,
          "algorithm.selection.local.success" -> 1L,
          "algorithm.selection.local.mcts.solution.complete" -> completeSolutionFromMCTS
        )
        Some(ServiceResult(repackagedResponses, log))
      case None => None
    }
  }
}
