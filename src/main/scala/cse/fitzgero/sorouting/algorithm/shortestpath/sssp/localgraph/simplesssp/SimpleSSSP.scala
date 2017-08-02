package cse.fitzgero.sorouting.algorithm.shortestpath.sssp.localgraph.simplesssp

import scala.annotation.tailrec
import scala.collection.mutable.PriorityQueue

import cse.fitzgero.sorouting.algorithm.shortestpath._
import cse.fitzgero.sorouting.roadnetwork.localgraph._
import cse.fitzgero.sorouting.roadnetwork.edge._
import cse.fitzgero.sorouting.roadnetwork.vertex._


class SimpleSSSP [G <: LocalGraph[V,E], V <: VertexProperty[_], E <: EdgeProperty] extends SSSP[G, SimpleSSSP_ODPair, SimpleSSSP_ODPath] {
  override def shortestPath (graph: G, od: SimpleSSSP_ODPair): SimpleSSSP_ODPath =
    djikstrasAlgorithm(graph, od)

  def djikstrasAlgorithm(graph: G, od: SimpleSSSP_ODPair): SimpleSSSP_ODPath = {
    val origin = od.srcVertex
    val goal = od.dstVertex
    val DistanceLowerBound = 0D
    val NoPathFound = SimpleSSSP_ODPath(origin, goal, List.empty[EdgeId], List.empty[Double])
    val OriginSearchData = SimpleSSSP_SearchNode(Origin, DistanceLowerBound)
    implicit val tripletOrdering: Ordering[Triplet] = Ordering.by {
      (t: Triplet) => {
        val edge: E =  graph.edgeAttrOf(t.e).get
        val flow: Double = edge.flow
        edge.cost.costFlow(flow)
      }
    }
    val startFrontier: collection.mutable.PriorityQueue[Triplet] = collection.mutable.PriorityQueue()(tripletOrdering.reverse)
    graph
      .neighborTriplets(od.srcVertex)
      .foreach(t => startFrontier.enqueue(t))

    @tailrec
    def _djikstrasAlgorithm(
      solution: Map[VertexId, SimpleSSSP_SearchNode],
      frontier: collection.mutable.PriorityQueue[Triplet],
      visited: Set[EdgeId] = Set.empty[EdgeId]): SimpleSSSP_ODPath = {
      if (frontier.isEmpty) NoPathFound
      else {
        val currentTriplet = frontier.dequeue
        val edgeCostAttr = graph.edgeAttrOf(currentTriplet.e).get
        // evaluate the cost function with the flow + snapshotFlow (implicitly) as input
        val edgeCost: Double = edgeCostAttr.cost.costFlow(edgeCostAttr.flow)
        // check if our current working solution has a path already that terminates at the current triplet's destination
        val nextSolution =
          if (!visited(currentTriplet.e) && !solution.isDefinedAt(currentTriplet.d)) {
            val updatedDistance = solution(currentTriplet.o).d + edgeCost
            solution.updated(currentTriplet.d, SimpleSSSP_SearchNode(π(currentTriplet.e, currentTriplet.o), updatedDistance))
          }
          else {
            solution
          }
        if (currentTriplet.d == goal) {
          val backPropagation: List[(EdgeId, Double)] = _backPropagate(nextSolution)(goal)
          SimpleSSSP_ODPath(origin, goal, backPropagation.map(_._1), backPropagation.map(_._2))
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
    (currentVertex: VertexId, result: List[(EdgeId, Double)] = List.empty[(EdgeId, Double)]): List[(EdgeId, Double)] = {
    val currentNode: SimpleSSSP_SearchNode = searchResults(currentVertex)
    currentNode.π match {
      case Origin => result.reverse
      case π(edge, src) =>
        _backPropagate(searchResults)(src, result :+ (edge, currentNode.d))
    }
  }


}


object SimpleSSSP {
  def apply[G <: LocalGraph[V, E], V <: VertexProperty[_], E <: EdgeProperty](): SimpleSSSP[G, V, E] = new SimpleSSSP[G, V, E]()
}
