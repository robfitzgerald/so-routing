package cse.fitzgero.sorouting.algorithm.trafficassignment

import cse.fitzgero.sorouting.algorithm.mssp.graphx.simplemssp._
//import cse.fitzgero.sorouting.algorithm.trafficassignment.TerminationCondition._
import cse.fitzgero.sorouting.roadnetwork.graph._
import org.apache.spark.graphx._

/**
  * Gradient-based traffic assignment solver
  */

abstract class TrafficAssignment {
  def solve (graph: RoadNetwork, odPairs: ODPairs, terminationCriteria: TerminationCriteria): TrafficAssignmentResult
}