package cse.fitzgero.sorouting.algorithm.pathsearch.ksp.localgraphsimpleksp

import java.time.Instant

import cse.fitzgero.sorouting.algorithm.pathsearch.KSP
import cse.fitzgero.sorouting.algorithm.pathsearch.ksp._
import cse.fitzgero.sorouting.algorithm.pathsearch.od.localgraph._
import cse.fitzgero.sorouting.algorithm.pathsearch.sssp.localgraphsimplesssp.LocalGraphSimpleSSSP
import cse.fitzgero.sorouting.roadnetwork.edge.EdgeProperty
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
class SimpleKSP [G <: LocalGraph[V,E], V <: VertexProperty[_], E <: EdgeProperty] extends KSP[G, LocalGraphODPair, LocalGraphODPath] {

  val sssp: LocalGraphSimpleSSSP[G,V,E] = LocalGraphSimpleSSSP[G,V,E]()

  implicit val simpleKSPOrdering: Ordering[LocalGraphODPath] = Ordering.by {
    (odPath: LocalGraphODPath) =>
      odPath.cost.sum
  }.reverse

  case class ReversePathData(path: List[EdgeId], cost: List[Double])

  override def kShortestPaths(graph: G, od: LocalGraphODPair, k: Int = 1, boundsTestParam: KSPBounds = NoKSPBounds): GenSeq[LocalGraphODPath] = {

    // allows for k to be the upper bounds on number of iterations by default
    val boundsTest = boundsTestParam match {
      case NoKSPBounds => PathsFoundBounds(k)
      case _ => boundsTestParam
    }
    val startTime = Instant.now().toEpochMilli

    // find the true shortest path
    val trueShortestPath: LocalGraphODPath = sssp.shortestPath(graph, LocalGraphODPair(od.srcVertex, od.dstVertex))
    // a way to lookup source vertex ids from an edge id
    val srcVerticesLookup: GenMap[EdgeId, VertexId] = graph.srcVerticesMap
    // our solution, a ranked list of paths
    val solution = scala.collection.mutable.PriorityQueue[LocalGraphODPath]()
    solution.enqueue(trueShortestPath)

    @tailrec
    def _kShortestPaths(
      walkBack: ReversePathData,
      previousGraph: G,
      iteration: Int = 1): GenSeq[LocalGraphODPath] = {

      val currentTime = Instant.now().toEpochMilli
      val kspBoundsData = KSPBoundsData(currentTime - startTime, iteration)

      if (walkBack.path.isEmpty || boundsTest.test(kspBoundsData))
        solution.dequeueAll[LocalGraphODPath, Seq[LocalGraphODPath]].take(k)
      else {
        // grab the current edge
        val thisEdge: EdgeId = walkBack.path.head

        // set this edge to infinity on a copy of the graph
        val blockedGraph: G = previousGraph.updateEdge(thisEdge, previousGraph.edgeAttrOf(thisEdge).get.copy(flowUpdate = Double.MaxValue).asInstanceOf[E]).asInstanceOf[G]
        val spurSourceVertex: VertexId = srcVerticesLookup(thisEdge)

        // find source vertex of this edge, run a new shortest paths search from there to end
        val alternatePathSpur: LocalGraphODPath = sssp.shortestPath(blockedGraph, LocalGraphODPair(spurSourceVertex, od.dstVertex))

        // combine spur with prefix (trueSPRev.tail.reverse) and add to solution
        val alternativePath: List[EdgeId] = walkBack.path.tail.reverse ::: alternatePathSpur.path
        val alternativePathCosts: List[Double] = walkBack.cost.tail.reverse ::: alternatePathSpur.cost
        val alterativeODPath: LocalGraphODPath = LocalGraphODPath(od.srcVertex, od.dstVertex, alternativePath, alternativePathCosts)
        solution.enqueue(alterativeODPath)

        // take a step back and repeat
        val remainingPathData: ReversePathData = ReversePathData(walkBack.path.tail, walkBack.cost.tail)
        // recurse on trueSPRev.tail, add LocalGraphODPath to solution, iteration + 1
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
