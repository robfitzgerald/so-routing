package cse.fitzgero.sorouting.algorithm.trafficassignment

import cse.fitzgero.sorouting.roadnetwork.graph._
import org.apache.spark.graphx._

/**
  * Gradient-based traffic assignment solver
  */
object TrafficAssignment {
  def solve(graph: RoadNetwork, odPairs: Seq[(VertexId, VertexId)]): RoadNetwork = ???
}