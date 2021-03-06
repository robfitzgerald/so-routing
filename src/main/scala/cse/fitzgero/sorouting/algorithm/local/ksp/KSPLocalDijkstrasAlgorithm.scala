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
  override type AlgorithmConfig = {
    def k: Int
    def kspBounds: Option[KSPBounds]
    def overlapThreshold: Double
  }

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
    * @param config the ksp ration, such as the value 'k' and the search bounds
    * @return the optional algorithm result
    */
  override def runAlgorithm(graph: LocalGraph, request: AlgorithmRequest, config: Option[AlgorithmConfig] = Some(KSPLocalDijkstrasConfig())): Option[AlgorithmResult] = {
//    println(s"[KSP-ALG] beginning config section for request ${request.id}")
    // setup
    val startTime = Instant.now.toEpochMilli

    val k: Int = config match {
      case Some(conf) => conf.k
      case None => 1
    }

    val kspBounds: KSPBounds = config match {
      case Some(conf) =>
        conf.kspBounds match {
          case Some(ksp) => ksp
          case None => KSPBounds.Iteration(1)
        }
      case None => KSPBounds.Iteration(1)
    }

    val overlapThreshold: Double = config match {
      case Some(conf) => conf.overlapThreshold
      case None => 1.0D
    }

//    println(s"[KSP-ALG] finished config section for request ${request.id}")

    SSSPLocalDijkstrasAlgorithm.runAlgorithm(graph, request) match {
      case None =>
//        println("[KSP-ALG] SSSP for true shortest path had None result, halting KSP with None")
        // no path between these vertices
        None
      case Some(trueShortestPath) =>
        // initialize the solution with the true shortest path, and
        // reverse that path to produce our backtracking "walkback" sequence
        val solution = scala.collection.mutable.PriorityQueue[Path]()
        solution.enqueue(trueShortestPath.path)
        val reversedPath: Path = trueShortestPath.path.reverse

        @tailrec
        def kShortestPaths(walkback: Path, previousGraph: Graph, iteration: Int = 1): Option[AlgorithmResult] = {
//          println(s"[KSP-ALG] running request ${request.id} iteration $iteration")

          val failedBoundsTest: Boolean =
            kspBounds match {
              case KSPBounds.Iteration(i) => iteration > i
              case KSPBounds.PathsFound(p) => solution.size > p
              case KSPBounds.Time(t) => Instant.now.toEpochMilli - startTime > t
              case KSPBounds.IterationOrTime(i, t) => iteration > i || Instant.now.toEpochMilli - startTime > t
            }


          // base case
          if (failedBoundsTest || walkback.isEmpty) {
            if (solution.isEmpty) {
              // should never reach here since trueShortestPath was found!
//              println("[KSP] landed in illegal state - no paths in solution after shortest path was found")
              None
            } else {
              val paths: Seq[Path] = solution.dequeueAll.take(k)
              Some(AlgorithmResult(request, paths))
            }
          } else {

            // find the leading edge in the walkback and remove it from the graph, and
            // re-run a shortest paths algorithm to generate an alternate path spur
            val thisEdgeId: EdgeId = walkback.head.edgeId

            graph.edgeById(thisEdgeId) match {
              case Some(edge) =>
                val spurSourceVertex: VertexId = edge.src
                val spurPrefix: Path = walkback.tail.reverse
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

                      val reasonablyDissimilar: Boolean = dissimilarityValue <= overlapThreshold

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
              case None =>
                println(s"[KSP] spur edge not found in graph: $thisEdgeId")
                kShortestPaths(walkback.tail, previousGraph, iteration + 1)
            }
          }
        }
//        println(s"[KSP-ALG] finished setup and calling first recursion for request ${request.id}")
        kShortestPaths(reversedPath, graph)
    }
  }
}
