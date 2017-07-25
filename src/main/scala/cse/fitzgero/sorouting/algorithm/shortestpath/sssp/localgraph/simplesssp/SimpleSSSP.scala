package cse.fitzgero.sorouting.algorithm.shortestpath.sssp.localgraph.simplesssp

import cse.fitzgero.sorouting.algorithm.shortestpath._
import cse.fitzgero.sorouting.roadnetwork.localgraph._
import cse.fitzgero.sorouting.roadnetwork.localgraph.edge._
import cse.fitzgero.sorouting.roadnetwork.localgraph.vertex._

import scala.annotation.tailrec

class SimpleSSSP [V <: VertexProperty[_], E <: EdgeProperty] extends SSSP[LocalGraph[V, E], SimpleSSSP_ODPair, SimpleSSSP_ODPath] {
  override def shortestPath (graph: LocalGraph[V, E], od: SimpleSSSP_ODPair): SimpleSSSP_ODPath =
    djikstrasAlgorithm(graph, od)

  def djikstrasAlgorithm(graph: LocalGraph[V, E], od: SimpleSSSP_ODPair): SimpleSSSP_ODPath = {
    val origin = od.srcVertex
    val goal = od.dstVertex
    val DistanceLowerBound = 0D
    val NoPathFound = SimpleSSSP_ODPath(origin, goal, List.empty[EdgeId], List.empty[Double])
    val OriginSearchData = SimpleSSSP_SearchNode(Origin, DistanceLowerBound)

    @tailrec
    def _djikstrasAlgorithm(solution: Map[VertexId, SimpleSSSP_SearchNode], frontier: Iterator[graph.Triplet]): SimpleSSSP_ODPath = {
      if (frontier.isEmpty) NoPathFound
      else {
        val currentTriplet = frontier.next
        val edgeCostAttr = graph.edgeAttrOf(currentTriplet.e).get
        val edgeCost: Double = edgeCostAttr.cost.costFlow(edgeCostAttr.flow)  // @TODO: decide on cost function needs!  revise class heirarchy?
        val nextSolution =
          if (!solution.isDefinedAt(currentTriplet.d))
            solution.updated(currentTriplet.d, SimpleSSSP_SearchNode(π(currentTriplet.e, currentTriplet.o), edgeCost))
          else solution
        if (currentTriplet.d == goal) {
          val backPropagation: List[(EdgeId, Double)] = _backPropagate(nextSolution)(goal)
          SimpleSSSP_ODPath(origin, goal, backPropagation.map(_._1), backPropagation.map(_._2))
        }
        else _djikstrasAlgorithm(
          nextSolution,
          frontier ++ graph.neighborTriplets(currentTriplet.d)
        )
      }
    }
    _djikstrasAlgorithm(
      Map[VertexId, SimpleSSSP_SearchNode](origin -> OriginSearchData),
      graph.neighborTriplets(od.srcVertex)
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
  def apply[V <: VertexProperty[_], E <: EdgeProperty](): SimpleSSSP[V, E] = new SimpleSSSP[V, E]()
}
