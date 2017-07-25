package cse.fitzgero.sorouting.algorithm.trafficassignment.graphx

import cse.fitzgero.sorouting.algorithm.trafficassignment._
import cse.fitzgero.sorouting.algorithm.shortestpath.mssp.graphx.simplemssp._
import cse.fitzgero.sorouting.roadnetwork.graphx.graph._

abstract class GraphXTrafficAssignment extends TrafficAssignment[RoadNetwork, SimpleMSSP_ODPair] with Serializable {
  // TODO: solve now returns base sealed trait which includes a NoSolution type. change the graphx implementation to support the broader set of categories
  def solve (graph: RoadNetwork, odPairs: Seq[SimpleMSSP_ODPair], terminationCriteria: TerminationCriteria): TrafficAssignmentResult
}