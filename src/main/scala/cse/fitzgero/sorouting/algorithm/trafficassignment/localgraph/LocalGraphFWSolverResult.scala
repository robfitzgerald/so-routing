package cse.fitzgero.sorouting.algorithm.trafficassignment.localgraph

import cse.fitzgero.sorouting.roadnetwork.localgraph._
import cse.fitzgero.sorouting.roadnetwork.localgraph.edge._
import cse.fitzgero.sorouting.roadnetwork.localgraph.vertex._
import cse.fitzgero.sorouting.algorithm.trafficassignment._

case class LocalGraphFWSolverResult (
  finalNetwork: LocalGraph[CoordinateVertexProperty, MacroscopicEdgeProperty],
  iterations: Int,
  time: Long,
  relGap: Double = 0D) extends TrafficAssignmentSolution[LocalGraph[CoordinateVertexProperty, MacroscopicEdgeProperty]] {
  require(relGap >= 0D && relGap <= 1D)
}
