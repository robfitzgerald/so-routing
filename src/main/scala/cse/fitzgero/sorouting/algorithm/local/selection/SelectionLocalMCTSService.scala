package cse.fitzgero.sorouting.algorithm.local.selection

import scala.collection.{GenMap, GenSeq}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

import cse.fitzgero.graph.algorithm.GraphService
import cse.fitzgero.sorouting.algorithm.local.mssp.{MSSPLocalDijkstrasService, MSSPLocalDijkstsasAlgorithmOps}
import cse.fitzgero.sorouting.model.path.SORoutingPathSegment
import cse.fitzgero.sorouting.model.population.{LocalRequest, LocalResponse}
import cse.fitzgero.sorouting.model.roadnetwork.local.{LocalEdge, LocalGraph, LocalODPair}

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

  /**
    * run the combinatorial selection algorithm as a concurrent service
    * @param graph underlying graph structure
    * @param request a map from requests to their sets of alternate paths as found by an alternate paths solver
    * @param config (unused)
    * @return a future resolving to an optional set of optimal paths
    */
  override def runService(graph: Graph, request: ServiceRequest, config: Option[ServiceConfig] = None): Future[Option[ServiceResult]] = {
    val promise: Promise[Option[ServiceResult]] = Promise()
    val algRequest = request.map(req => (req._1.od, req._2))
    SelectionLocalMCTSAlgorithm.runAlgorithm(graph, algRequest, config) match {
      case Some(mctsResult) =>
        if (mctsResult.result.size == request.size) {
          // we got to a terminal node and we have a complete mctsResult
          println(s"[MCTS] finished with complete solution")
          promise.success(processResults(graph, request, mctsResult.result, mctsResult.result, mctsResult.embarrassinglySolvable))
        } else {
          // we didn't get to a terminal node; we need to fill in the remaining requests with shortest paths
          println(s"[MCTS] finished but incomplete solution. calling MSSP for ${request.size - mctsResult.result.size} of ${request.size} unhandled requests.")
          // for each edge, the contribution from the mcts group in edge flows
          val edgesAndFlows: GenMap[String, Int] =
            mctsResult.result.flatMap {
              req =>
                req._2.map(_.edgeId)
            }.groupBy(identity)
            .mapValues(_.size)

          // update graph from MCTS mctsResult
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
          // TODO: remove Await and instead map these futures together for the 'mctsResult' or use a promise
          val remaining = request.filter { req => !mctsResult.result.isDefinedAt(req._1.od) }
          MSSPLocalDijkstrasService.runService(updatedGraph, remaining.toSeq.map{ req => req._1 }) onComplete {
            case Success(extrasResult) =>
              println(s"[MCTS] MSSP completed for remaining ${remaining.size} unrouted vehicles")
              // merge and return
              extrasResult match {
                case None =>
                  promise.success(processResults(graph, request, mctsResult.result, mctsResult.result, mctsResult.embarrassinglySolvable))
                case Some(extrasResolved) =>
                  val finalResult = mctsResult.result ++ extrasResolved.result.map{ res => (res.request.od, res.path)}
                  promise.success(processResults(graph, request, mctsResult.result, finalResult, mctsResult.embarrassinglySolvable))
              }
            case Failure(e) =>
              promise.failure(e)
          }
        }
      case None => // the algorithm could not find an optimal solution, so we should fall back to selfish routing
        val returnAsMSSP =
          request.map {
            req =>
              LocalResponse(req._1, req._2.head)
          }.toSeq
        val costEffect: Long = MSSPLocalDijkstsasAlgorithmOps.calculateAddedCost(graph, returnAsMSSP).toLong
        val log = Map[String, Long](
          "algorithm.selection.local.called" -> 1L,
          "algorithm.selection.local.runtime" -> runTime,
          "algorithm.selection.local.cost.effect" -> costEffect,
          "algorithm.selection.local.mcts.selfish.matches.optimal" -> request.size
        )
        println(s"[MCTS] no solution found, returning selfish solution for ${returnAsMSSP.size} requests")
        promise.success(Some(ServiceResult(returnAsMSSP, log)))
    }
    promise.future
  }

  /**
    * takes a request, (possibly partial) mcts result, and any final processed result, and packages it as a service result
    * @param graph the underlying graph structure
    * @param request the request and a set of alternate paths sorted by cost ascending
    * @param mctsResult the result of the mcts algorithm, which may be a partial routing solution
    * @param finalResult in the case of a partial mcts solution, it is the full set of requests filled in with selfish routing
    * @return
    */
  def processResults(graph: LocalGraph, request: ServiceRequest, mctsResult: GenMap[LocalODPair, Path], finalResult: GenMap[LocalODPair, Path], embarrassinglySolvable: Boolean): Option[ServiceResult] = {
    // format response
    val repackagedResponses: GenSeq[LocalResponse] =
      request.flatMap(req =>
        if (finalResult.isDefinedAt(req._1.od))
          Some(LocalResponse(req._1, finalResult(req._1.od)))
        else
          None
      ).toSeq

    // preparing analytics
    val trueShortestPathEdges: GenMap[EdgeId, Int] =
      request
        .flatMap {
          req =>
            if (req._2.isEmpty) None
            else req._2.flatMap(_.map(_.edgeId))
        }
        .groupBy(identity)
        .mapValues(_.size)
    val optimalEdges: GenMap[EdgeId, Int] =
      mctsResult
        .flatMap { _._2.map(_.edgeId) }
        .groupBy(identity)
        .mapValues(_.size)
    val costEffect: Long = MSSPLocalDijkstsasAlgorithmOps.calculateAddedCost(graph, repackagedResponses).toLong
    val completeSolutionFromMCTS: Long = if (mctsResult.size == request.size) 1L else 0L
    val trueShortestPathsHadOverlap: Boolean = trueShortestPathEdges.count(_._2 > 1) > 0
    val optimalPathsHadOverlap: Boolean = optimalEdges.count(_._2 > 1) > 0
    val overlapCountInTrueShortestPaths: Long = trueShortestPathEdges.values.sum
    val overlapCountInSolutionPaths: Long = optimalEdges.values.sum
    val solutionRoutesEqualSelfishRoutes: Boolean = {
      val requestLookup = request.values.toVector
      mctsResult.forall(requestLookup.contains)
    }

    val log = Map[String, Long](
      "algorithm.selection.local.runtime" -> runTime,
      "algorithm.selection.local.cost.effect" -> costEffect,
      "algorithm.selection.local.called" -> 1L,
      "algorithm.selection.local.mcts.solution.complete" -> completeSolutionFromMCTS,
      "algorithm.selection.local.mcts.true.shortest.paths.had.overlap" -> (if (trueShortestPathsHadOverlap) mctsResult.size else 0L),
      "algorithm.selection.local.mcts.optimal.paths.had.overlap" -> (if (optimalPathsHadOverlap) mctsResult.size else 0L),
      "algorithm.selection.local.mcts.solution.route.count" -> mctsResult.size,
      "algorithm.selection.local.mcts.overlap.count.selfish" -> overlapCountInTrueShortestPaths,
      "algorithm.selection.local.mcts.overlap.count.optimal" -> overlapCountInSolutionPaths,
      "algorithm.selection.local.mcts.selfish.matches.optimal" -> (if (solutionRoutesEqualSelfishRoutes) mctsResult.size else 0L),
      "algorithm.selection.local.mcts.solution.meaningful" -> (if (!embarrassinglySolvable) mctsResult.size else 0L)
    )
    Some(ServiceResult(repackagedResponses, log))
  }
    
}
