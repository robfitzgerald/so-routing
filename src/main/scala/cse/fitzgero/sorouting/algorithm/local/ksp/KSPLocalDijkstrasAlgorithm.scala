package cse.fitzgero.sorouting.algorithm.local.ksp

import java.time.Instant

import cse.fitzgero.graph.algorithm.GraphRoutingAlgorithm
import cse.fitzgero.graph.config.KSPBounds
import cse.fitzgero.sorouting.algorithm.local.sssp.SSSPLocalDijkstrasAlgorithm
import cse.fitzgero.sorouting.model.roadnetwork.local.{LocalEdge, LocalGraph, LocalODPair}
import monocle.macros.GenLens

import scala.annotation.tailrec
import scala.collection.GenSeq

object KSPLocalDijkstrasAlgorithm extends GraphRoutingAlgorithm {
  override type VertexId = SSSPLocalDijkstrasAlgorithm.VertexId
  override type EdgeId = SSSPLocalDijkstrasAlgorithm.EdgeId
  override type Graph = SSSPLocalDijkstrasAlgorithm.Graph
  override type Path = SSSPLocalDijkstrasAlgorithm.Path
  override type OD = LocalODPair
  type PathSegment = SSSPLocalDijkstrasAlgorithm.PathSegment
  type SSSPAlgorithmResult = SSSPLocalDijkstrasAlgorithm.AlgorithmResult
  override type AlgorithmConfig = KSPLocalDijkstrasConfig

  case class AlgorithmResult(od: OD, genSeq: GenSeq[Path])

  implicit val simpleKSPOrdering: Ordering[SSSPAlgorithmResult] =
    Ordering.by {
      (odPath: SSSPAlgorithmResult) =>
        odPath.path.map(_.cost match {
          case Some(seqOfCosts) => seqOfCosts.sum
          case None => 0D
        }).sum
    }.reverse

  override def runAlgorithm(graph: LocalGraph, request: OD, configOption: Option[AlgorithmConfig] = Some(KSPLocalDijkstrasConfig())): Option[AlgorithmResult] = {
    val startTime = Instant.now.toEpochMilli
    val config = configOption.get
    val k = config.k
    val kspBounds = config.kSPBounds.get

    SSSPLocalDijkstrasAlgorithm.runAlgorithm(graph, request) match {
      case None => None
      case Some(trueShortestPath) =>
        val solution = scala.collection.mutable.PriorityQueue[SSSPAlgorithmResult]()
        solution.enqueue(trueShortestPath)
        val reversedPath: Path = trueShortestPath.path.reverse

//        @tailrec
        def kShortestPaths(walkback: Path, previousGraph: Graph, iteration: Int = 1): Option[AlgorithmResult] = {

          val failedBoundsTest: Boolean = config.kSPBounds.get match {
            case KSPBounds.Iteration(i) => iteration > i
            case KSPBounds.PathsFound(p) => solution.size > p
            case KSPBounds.Time(t) => Instant.now.toEpochMilli - startTime > t
          }

          if (failedBoundsTest || walkback.isEmpty) {
            if (solution.isEmpty)
              None
            else {
              val paths: Seq[Path] = solution.dequeueAll.take(k).map(_.path)
              Some(AlgorithmResult(request, paths))
            }
          } else {
            val thisEdgeId: EdgeId = walkback.head.e
            val thisEdge: LocalEdge = previousGraph.edgeById(thisEdgeId).get
            val blockedGraph: Graph = previousGraph.removeEdge(thisEdgeId)
            val spurAlternatives = blockedGraph.outEdges(thisEdge.src)
            None
          }
        }

        kShortestPaths(reversedPath, graph)
    }
  }
}
