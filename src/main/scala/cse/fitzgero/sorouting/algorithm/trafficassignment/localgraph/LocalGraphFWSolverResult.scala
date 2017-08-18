package cse.fitzgero.sorouting.algorithm.trafficassignment.localgraph

import cse.fitzgero.sorouting.roadnetwork.localgraph._
import cse.fitzgero.sorouting.roadnetwork.edge._
import cse.fitzgero.sorouting.roadnetwork.vertex._
import cse.fitzgero.sorouting.algorithm.trafficassignment._

case class LocalGraphFWSolverResult (
  finalNetwork: LocalGraphMATSim,
  iterations: Int,
  time: Long,
  relGap: Double = 0D) extends TrafficAssignmentSolution[LocalGraphMATSim] {
  require(relGap >= 0D && relGap <= 1D, "LocalGraphFWSolverResult.relGap should be in the range [0.0, 1.0]")
}
