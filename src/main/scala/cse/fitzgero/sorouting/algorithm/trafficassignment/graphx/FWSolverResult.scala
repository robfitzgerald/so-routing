package cse.fitzgero.sorouting.algorithm.trafficassignment.graphx

import cse.fitzgero.sorouting.algorithm.trafficassignment._
import cse.fitzgero.sorouting.algorithm.shortestpath.mssp.graphx.simplemssp.ODPaths
import cse.fitzgero.sorouting.roadnetwork.graphx.graph.RoadNetwork

case class FWSolverResult (
  paths: ODPaths,
  finalNetwork: RoadNetwork,
  iterations: Int,
  time: Long,
  relGap: Double = 0D) extends TrafficAssignmentResult[RoadNetwork] {
  require(relGap >= 0D && relGap <= 1D)
}
