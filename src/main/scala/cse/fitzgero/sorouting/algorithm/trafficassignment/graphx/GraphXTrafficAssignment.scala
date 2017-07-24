package cse.fitzgero.sorouting.algorithm.trafficassignment.graphx

import cse.fitzgero.sorouting.algorithm.trafficassignment._
import cse.fitzgero.sorouting.algorithm.shortestpath.mssp.graphx.simplemssp._
import cse.fitzgero.sorouting.roadnetwork.graphx.graph._

/**
  * Gradient-based traffic assignment solver
  */

abstract class GraphXTrafficAssignment extends TrafficAssignment[RoadNetwork, SimpleMSSP_ODPair]{
  def solve (graph: RoadNetwork, odPairs: Seq[SimpleMSSP_ODPair], terminationCriteria: TerminationCriteria): TrafficAssignmentResult[RoadNetwork]
}