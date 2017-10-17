package cse.fitzgero.sorouting.algorithm.local.ksp

import java.time.Instant

import cse.fitzgero.graph.algorithm.GraphRoutingAlgorithm
import cse.fitzgero.graph.config.KSPBounds
import cse.fitzgero.sorouting.algorithm.local.sssp.SSSPLocalDijkstrasAlgorithm
import cse.fitzgero.sorouting.model.roadnetwork.local.{LocalGraph, LocalODPair}

import scala.annotation.tailrec
import scala.collection.{GenSeq, GenSet}

object KSPLocalDijkstrasAlgorithm extends GraphRoutingAlgorithm {
  override type VertexId = SSSPLocalDijkstrasAlgorithm.VertexId
  override type EdgeId = SSSPLocalDijkstrasAlgorithm.EdgeId
  override type Graph = SSSPLocalDijkstrasAlgorithm.Graph
  override type Path = SSSPLocalDijkstrasAlgorithm.Path
  override type AlgorithmRequest = LocalODPair
  type PathSegment = SSSPLocalDijkstrasAlgorithm.PathSegment
  type SSSPAlgorithmResult = SSSPLocalDijkstrasAlgorithm.AlgorithmResult
  override type AlgorithmConfig = KSPLocalDijkstrasConfig

  case class AlgorithmResult(od: AlgorithmRequest, paths: GenSeq[Path])

  /**
    * ordering for the priority queue used by the KSP algorithm to compare path alternatives
    */
  implicit val simpleKSPOrdering: Ordering[Path] =
    Ordering.by {
      (odPath: Path) =>
        odPath.map(_.cost match {
          case Some(seqOfCosts) => seqOfCosts.sum
          case None => 0D
        }).sum
    }.reverse

  /**
    * run a k-shortest path algorithm that internally calls a shortest path algorithm from incremental vertices of the true shortest path
    * @param graph the underlying graph structure
    * @param request a single request or a batch request
    * @param configOption the ksp configuration, such as the value 'k' and the search bounds
    * @return the optional algorithm result
    */
  override def runAlgorithm(graph: LocalGraph, request: AlgorithmRequest, configOption: Option[AlgorithmConfig] = Some(KSPLocalDijkstrasConfig())): Option[AlgorithmResult] = {
    // setup
    val startTime = Instant.now.toEpochMilli
    val config = configOption.get

    SSSPLocalDijkstrasAlgorithm.runAlgorithm(graph, request) match {
      case None => None
      case Some(trueShortestPath) =>
        // initialize the solution with the true shortest path, and
        // reverse that path to produce our backtracking "walkback" sequence
        val solution = scala.collection.mutable.PriorityQueue[Path]()
        solution.enqueue(trueShortestPath.path)
        val reversedPath: Path = trueShortestPath.path.reverse

        @tailrec
        def kShortestPaths(walkback: Path, previousGraph: Graph, iteration: Int = 1): Option[AlgorithmResult] = {

          val failedBoundsTest: Boolean = config.kSPBounds match {
            case None => false // default - only stopping condition is reaching the terminus of the walkback
            case Some(kspBounds) => kspBounds match {
              case KSPBounds.Iteration(i) => iteration > i
              case KSPBounds.PathsFound(p) => solution.size > p
              case KSPBounds.Time(t) => Instant.now.toEpochMilli - startTime > t
            }
          }

          // base case
          if (failedBoundsTest || walkback.isEmpty) {
            if (solution.isEmpty)
              None
            else {
              val paths: Seq[Path] = solution.dequeueAll.take(config.k)
              Some(AlgorithmResult(request, paths))
            }
          } else {

            // find the leading edge in the walkback and remove it from the graph, and
            // re-run a shortest paths algorithm to generate an alternate path spur
            val thisEdgeId: EdgeId = walkback.head.edgeId
            val spurPrefix: Path = walkback.tail.reverse
            val spurSourceVertex: VertexId = graph.edgeById(thisEdgeId).get.src
            val blockedGraph: Graph = previousGraph.removeEdge(thisEdgeId)
            val spurAlternatives = blockedGraph.outEdges(spurSourceVertex)
            if (spurAlternatives.isEmpty)
              kShortestPaths(walkback.tail, blockedGraph, iteration + 1)
            else {
              SSSPLocalDijkstrasAlgorithm.runAlgorithm(blockedGraph, LocalODPair(request.id, spurSourceVertex, request.dst)) match {
                case None =>
                  kShortestPaths(walkback.tail, blockedGraph, iteration + 1)
                case Some(pathSpur) =>

                  // given a path spur, connect it to the remaining walkback to produce a path
                  val alternativePath: Path = spurPrefix ++ pathSpur.path

                  // test for dissimilarity from current solution paths
                  val alternativePathLabels: Seq[EdgeId] = alternativePath.map(_.edgeId)
                  val solutionLabels: GenSet[EdgeId] =
                    solution
                      .flatMap(_.map(_.edgeId))
                      .toSet
                      .par

                  val dissimilarityValue: Double =
                    solutionLabels
                      .count(alternativePathLabels.contains(_)).toDouble / solutionLabels.size

                  val reasonablyDissimilar: Boolean = dissimilarityValue <= config.overlapThreshold

                  // modify the solution, graph and walkback based on the dissimilarity result
                  val (graphToRecurse, nextWalkback) = if (reasonablyDissimilar) {
                    solution.enqueue(alternativePath)
                    (blockedGraph, walkback.tail)
                  } else {
                    if (pathSpur.path.nonEmpty)
                      (blockedGraph.removeEdge(pathSpur.path.head.edgeId), walkback)
                    else
                      (blockedGraph, walkback)
                  }

                  kShortestPaths(nextWalkback, graphToRecurse, iteration + 1)
              }
            }
          }
        }
        kShortestPaths(reversedPath, graph)
    }
  }
}
