package cse.fitzgero.sorouting.algorithm.local.sssp

import cse.fitzgero.graph.algorithm.ShortestPathAlgorithm
import cse.fitzgero.sorouting.model.roadnetwork.local._

import scala.annotation.tailrec

class SSSPLocalDijkstrasSearch extends ShortestPathAlgorithm {
  type VertexId = String
  type EdgeId = String
  type RequestId = String
  type Graph = LocalGraph
  type ODPair = LocalODPair

  case class AlgorithmResult(od: ODPair, path: Path) extends ShortestPathResult

  override def runAlgorithm(g: Graph, od: ODPair): Option[AlgorithmResult] = {

    Some(AlgorithmResult(od, Seq(PathSegment("100", None))))
  }

  case class SearchData(edge: LocalEdge, cost: Double = 0D)
  case class BackPropagateData(π: Option[EdgeId], d: Double)

  private def dijkstras (graph: Graph, od: ODPair): Map[VertexId, BackPropagateData] = {

    @tailrec def _dijkstras (
      solution: Map[VertexId, BackPropagateData],
      frontier: collection.mutable.PriorityQueue[SearchData],
      visited: Set[EdgeId] = Set.empty[EdgeId]
    ): Map[VertexId, BackPropagateData] = {
      if (frontier.isEmpty)
        Map() // never found the goal
      else {
        val shortestFrontier = frontier.dequeue

        val solutionUpdate =
          if (!visited(shortestFrontier.edge.id) && !solution.isDefinedAt(shortestFrontier.edge.dst)) {
            val updatedDistance = solution(shortestFrontier.edge.src).d + shortestFrontier.cost
            solution.updated(shortestFrontier.edge.dst, BackPropagateData(Some(shortestFrontier.edge.id), updatedDistance))
          } else
            solution

        if (shortestFrontier.edge.dst == od.dst) {
          solutionUpdate
        } else {

          val frontierUpdate =
            graph
              .outEdges(shortestFrontier.edge.dst)
              .filter(!visited(_))
              .flatMap(e =>
                graph.edgeById(e) match {
                  case Some(localEdge) => localEdge.attribute.linkCostFlow match {
                    case Some(linkCost) => Some(SearchData(localEdge, linkCost))
                    case None => None
                  }
                  case None => None
                }
              ).foldLeft(frontier)((f, e) => {f.enqueue(e); f})

          _dijkstras(solutionUpdate, frontierUpdate, visited + shortestFrontier.edge.id)
        }
      }
    }
    val frontierOrdering: Ordering[SearchData] = Ordering.by (_.cost)
    val startFrontier: collection.mutable.PriorityQueue[SearchData] = collection.mutable.PriorityQueue()(frontierOrdering.reverse)
    graph
      .outEdges(od.src)
      .flatMap(graph.edgeById)
      .foreach(e => {
        val cost = e.attribute.linkCostFlow match {
          case Some(linkCost) => linkCost
          case None => Double.MaxValue  // ***** assumes that a missing cost function value means avoid this link. could be a config-set value.
        }
        startFrontier.enqueue(SearchData(e, cost))
      })
  }
}
