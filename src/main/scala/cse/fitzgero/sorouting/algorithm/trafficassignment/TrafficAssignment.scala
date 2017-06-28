package cse.fitzgero.sorouting.algorithm.trafficassignment

import cse.fitzgero.sorouting.algorithm.shortestpath._
//import cse.fitzgero.sorouting.algorithm.trafficassignment.TerminationCondition._
import cse.fitzgero.sorouting.roadnetwork.graph._
import org.apache.spark.graphx._

/**
  * Gradient-based traffic assignment solver
  */

abstract class TrafficAssignment {
  def solve (graph: RoadNetwork, odPairs: Seq[(VertexId, VertexId)], terminationCriteria: TerminationCriteria): TrafficAssignmentResult
}