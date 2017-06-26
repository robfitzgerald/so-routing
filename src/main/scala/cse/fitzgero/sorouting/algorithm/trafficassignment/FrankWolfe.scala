package cse.fitzgero.sorouting.algorithm.trafficassignment
import cse.fitzgero.sorouting.algorithm.shortestpath.Path
import cse.fitzgero.sorouting.roadnetwork.graph.RoadNetwork
import cse.fitzgero.sorouting.algorithm.shortestpath._
import org.apache.spark.graphx.VertexId
import java.time._

object FrankWolfe extends TrafficAssignment {
  case class TerminationData(time: Long, iterations: Int, relGap: Double)
  override def solve(initialGraph: RoadNetwork, odPairs: Seq[(VertexId, VertexId)], terminationCriteria: TerminationCriteria): Seq[Path] = {
//    terminationCriteria match {
//      case RelativeGapTerminationCriteria(relGapThresh) =>
//    }
//      i <- Iterator.iterate(1)(_+1).takeWhile(thisI => terminationTest(TerminationData(Instant.now().toEpochMilli, thisI, x)))

    // while terminationCriteria is not met, recurse on solving this
    // should be tailrec
    def _solve(previousGraph: RoadNetwork, currentGraph: RoadNetwork): (RoadNetwork, Seq[Path]) = ???

//    plop this guy into _solve and run it for the data in scope in each _solve recursion
    def testTermination(): Boolean = ???
    val AONGraph: RoadNetwork = Assignment(initialGraph, odPairs, AONFlow())
    val firstAssignment: RoadNetwork = Assignment(initialGraph, odPairs, CostFlow())

//    maybe we don't need to store these bundles
    val startingData = TerminationData(Instant.now().toEpochMilli, 0, 1.0D)

    _solve(initialGraph, firstAssignment)
    Seq.empty[List[String]]
  }
  def Assignment (graph: RoadNetwork, odPairs: Seq[(VertexId, VertexId)], assignmentType: CostMethod): RoadNetwork = {
    val pathsToFlows: Map[String, Int] = GraphXShortestPaths.shortestPaths(graph, odPairs, assignmentType).flatMap(_._3).groupBy(identity).mapValues(_.size).map(identity)
    graph.mapEdges(edge => if (pathsToFlows.isDefinedAt(edge.attr.id)) edge.attr.copy(flow = edge.attr.flow + pathsToFlows(edge.attr.id)) else edge.attr)
  }
}
