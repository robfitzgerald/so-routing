package cse.fitzgero.sorouting.algorithm.pathsearch.sssp.localgraphsimplesssp

import cse.fitzgero.sorouting.algorithm.pathsearch._
import cse.fitzgero.sorouting.algorithm.pathsearch.od.localgraph._
import cse.fitzgero.sorouting.roadnetwork.edge._
import cse.fitzgero.sorouting.roadnetwork.localgraph._
import cse.fitzgero.sorouting.roadnetwork.vertex._
import cse.fitzgero.sorouting.util.ClassLogging

import scala.annotation.tailrec


class LocalGraphVertexOrientedSSSP [G <: LocalGraph[V,E], V <: VertexProperty[_], E <: EdgeProperty]
  extends SSSP[G, LocalGraphODPairByVertex, LocalGraphODPath] with ClassLogging {

  override def shortestPath (graph: G, od: LocalGraphODPairByVertex): LocalGraphODPath =
    djikstrasAlgorithm(graph, od)

  def djikstrasAlgorithm(graph: G, od: LocalGraphODPairByVertex): LocalGraphODPath = {
    val origin = od.src
    val goal = od.dst
    val DistanceLowerBound = 0D
    val NoPathFound = LocalGraphODPath(od.personId, origin, goal, List.empty[EdgeId], List.empty[Double])
    val OriginSearchData = SimpleSSSP_SearchNode(Origin, DistanceLowerBound)
    implicit val tripletOrdering: Ordering[Triplet] = Ordering.by {
      (t: Triplet) => {
        val edge: E =  graph.edgeAttrOf(t.e).get
        val flow: Double = edge.assignedFlow
        edge.cost.costFlow(flow)
      }
    }
    val startFrontier: collection.mutable.PriorityQueue[Triplet] = collection.mutable.PriorityQueue()(tripletOrdering.reverse)
    graph
      .neighborTriplets(od.src)
      .foreach(t => startFrontier.enqueue(t))

    @tailrec
    def _djikstrasAlgorithm(
      solution: Map[VertexId, SimpleSSSP_SearchNode],
      frontier: collection.mutable.PriorityQueue[Triplet],
      visited: Set[EdgeId] = Set.empty[EdgeId]): LocalGraphODPath = {
      if (frontier.isEmpty) NoPathFound
      else {
        val currentTriplet = frontier.dequeue
        val edgeAttr = graph.edgeAttrOf(currentTriplet.e).get
        val edgeCost: Double = edgeAttr.linkCostFlow

        // check if our current working solution has a path already that terminates at the current triplet's destination
        val nextSolution =
          if (!visited(currentTriplet.e) && !solution.isDefinedAt(currentTriplet.d)) {
            val updatedDistance = solution(currentTriplet.o).d + edgeCost
            solution.updated(currentTriplet.d, SimpleSSSP_SearchNode(π(currentTriplet.e, currentTriplet.o, edgeCost), updatedDistance))
          }
          else {
            solution
          }
        if (currentTriplet.d == goal) {
          val backPropagation: List[(EdgeId, Double)] = _backPropagate(nextSolution)(goal)
          LocalGraphODPath(od.personId, origin, goal, backPropagation.map(_._1), backPropagation.map(_._2))
        }
        else {
          val addToFrontier =
             graph
              .neighborTriplets(currentTriplet.d)
              .filter(triplet => !visited(triplet.e))
          _djikstrasAlgorithm(
            nextSolution,
            frontier ++ addToFrontier,
            visited + currentTriplet.e
          )
        }
      }
    }
    _djikstrasAlgorithm(
      Map[VertexId, SimpleSSSP_SearchNode](origin -> OriginSearchData),
      startFrontier
    )
  }

  @tailrec
  final def _backPropagate
  (searchResults: Map[VertexId, SimpleSSSP_SearchNode])
    (
      currentVertex: VertexId,
      result: List[(EdgeId, Double)] = List.empty[(EdgeId, Double)]
    ): List[(EdgeId, Double)] = {
    val currentNode: SimpleSSSP_SearchNode = searchResults(currentVertex)
    currentNode.π match {
      case Origin => result.reverse
      case π(edge, src, edgeCost) =>
        _backPropagate(searchResults)(src, result :+ (edge, edgeCost))
    }
  }
}


object LocalGraphVertexOrientedSSSP {
  def apply[G <: LocalGraph[V, E], V <: VertexProperty[_], E <: EdgeProperty](): LocalGraphVertexOrientedSSSP[G, V, E] = new LocalGraphVertexOrientedSSSP[G, V, E]()
}
