package cse.fitzgero.sorouting.algorithm.trafficassignment.graphx

import cse.fitzgero.sorouting.algorithm.trafficassignment._
import cse.fitzgero.sorouting.algorithm.pathsearch.mssp.graphx.simplemssp.ODPaths
import cse.fitzgero.sorouting.roadnetwork.graphx.graph.RoadNetwork

case class GraphXFWSolverResult (
  paths: ODPaths,
  finalNetwork: RoadNetwork,
  iterations: Int,
  time: Long,
  relGap: Double = 0D) extends TrafficAssignmentSolution[RoadNetwork] with Serializable {
  require(relGap >= 0D && relGap <= 1D)
}
