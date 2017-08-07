package cse.fitzgero.sorouting.algorithm.pathsearch.ksp.localgraph.simpleksp

import cse.fitzgero.sorouting.algorithm.pathsearch.KSP
import cse.fitzgero.sorouting.algorithm.pathsearch.sssp.localgraph.simplesssp._
import cse.fitzgero.sorouting.roadnetwork.edge.{EdgeProperty, MacroscopicEdgeProperty}
import cse.fitzgero.sorouting.roadnetwork.localgraph._
import cse.fitzgero.sorouting.roadnetwork.vertex.VertexProperty

import scala.annotation.tailrec
import scala.collection.{GenMap, GenSeq}

/**
  * a sequential implementation of k shortest paths built on using a priority queue for the solution set.
  * it follows EQV Martins (2002) by "removing" (set to infinity) edges of the true shortest path, starting with the
  * final edge, and moving backward stepwise with this mutation until k paths have been acquired or edges of the
  * true shortest path have been exhausted.
  * @tparam G a road network data structure type
  * @tparam V vertex type
  * @tparam E edge type
  */
class SimpleKSP [G <: LocalGraph[V,E], V <: VertexProperty[_], E <: EdgeProperty] extends KSP[G, SimpleKSP_ODPair, SimpleKSP_ODPath] {

  val sssp: SimpleSSSP[G,V,E] = SimpleSSSP[G,V,E]()

  implicit val simpleKSPOrdering: Ordering[SimpleKSP_ODPath] = Ordering.by {
    (odPath: SimpleKSP_ODPath) =>
      odPath.cost.sum
  }.reverse

  case class ReversePathData(path: List[EdgeId], cost: List[Double])

  override def kShortestPaths(graph: G, od: SimpleKSP_ODPair, k: Int): GenSeq[SimpleKSP_ODPath] = {

    // find the true shortest path
    val trueShortestPath: SimpleSSSP_ODPath = sssp.shortestPath(graph, SimpleSSSP_ODPair(od.srcVertex, od.dstVertex))
    // a way to lookup source vertex ids from an edge id
    val srcVerticesLookup: GenMap[EdgeId, VertexId] = graph.srcVerticesMap
    // our solution, a ranked list of paths
    val solution = scala.collection.mutable.PriorityQueue[SimpleKSP_ODPath]()
    solution.enqueue(SimpleKSP_ODPath.from(trueShortestPath))

    @tailrec
    def _kShortestPaths(
      walkBack: ReversePathData,
      previousGraph: G,
      iteration: Int = 1): GenSeq[SimpleKSP_ODPath] = {
      if (walkBack.path.isEmpty || iteration >= k)
        solution.dequeueAll[SimpleKSP_ODPath, Seq[SimpleKSP_ODPath]].take(k)
      else {
        // grab the current edge
        val thisEdge: EdgeId = walkBack.path.head

        // set this edge to infinity on a copy of the graph
        val blockedGraph: G = previousGraph.updateEdge(thisEdge, previousGraph.edgeAttrOf(thisEdge).get.copy(flowUpdate = Double.MaxValue).asInstanceOf[E]).asInstanceOf[G]
        val spurSourceVertex: VertexId = srcVerticesLookup(thisEdge)

        // find source vertex of this edge, run a new shortest paths search from there to end
        val alternatePathSpur: SimpleSSSP_ODPath = sssp.shortestPath(blockedGraph, SimpleSSSP_ODPair(spurSourceVertex, od.dstVertex))

        // combine spur with prefix (trueSPRev.tail.reverse) and add to solution
        val alternativePath: List[EdgeId] = walkBack.path.tail.reverse ::: alternatePathSpur.path
        val alternativePathCosts: List[Double] = walkBack.cost.tail.reverse ::: alternatePathSpur.cost
        val alterativeODPath: SimpleKSP_ODPath = SimpleKSP_ODPath(od.srcVertex, od.dstVertex, alternativePath, alternativePathCosts)
        solution.enqueue(alterativeODPath)

        // take a step back and repeat
        val remainingPathData: ReversePathData = ReversePathData(walkBack.path.tail, walkBack.cost.tail)
        // recurse on trueSPRev.tail, add SimpleKSP_ODPath to solution, iteration + 1
        _kShortestPaths(remainingPathData, blockedGraph, iteration + 1)
      }
    }

    val walkBackResult: ReversePathData = ReversePathData(trueShortestPath.path.reverse, trueShortestPath.cost.reverse)
    _kShortestPaths(walkBackResult, graph)
  }
}


object SimpleKSP {
  def apply[G <: LocalGraph[V, E], V <: VertexProperty[_], E <: EdgeProperty](): SimpleKSP[G, V, E] = new SimpleKSP[G, V, E]()
}
