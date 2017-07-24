package cse.fitzgero.sorouting.algorithm.trafficassignment.graphx

import cse.fitzgero.sorouting.algorithm.shortestpath.mssp.graphx.simplemssp._
import cse.fitzgero.sorouting.roadnetwork.graphx.graph._

/**
  * Gradient-based traffic assignment solver
  */

abstract class GraphXTrafficAssignment {
  def solve (graph: RoadNetwork, odPairs: ODPairs, terminationCriteria: TerminationCriteria): TrafficAssignmentResult
}