package cse.fitzgero.sorouting.algorithm.flowestimation.graphx

import cse.fitzgero.sorouting.algorithm.flowestimation._
import cse.fitzgero.sorouting.algorithm.pathsearch.mssp.graphx.simplemssp.ODPaths
import cse.fitzgero.sorouting.roadnetwork.graphx.GraphxRoadNetwork

case class GraphXFWSolverResult (
  paths: ODPaths,
  finalNetwork: GraphxRoadNetwork,
  iterations: Int,
  time: Long,
  relGap: Double = 0D) extends TrafficAssignmentSolution[GraphxRoadNetwork] with Serializable {
  require(relGap >= 0D && relGap <= 1D)
}
