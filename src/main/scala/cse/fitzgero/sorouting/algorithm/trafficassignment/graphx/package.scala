package cse.fitzgero.sorouting.algorithm.trafficassignment

import cse.fitzgero.sorouting.algorithm.shortestpath.mssp.graphx.simplemssp.ODPaths
import cse.fitzgero.sorouting.roadnetwork.graphx.graph.RoadNetwork

package object graphx {
  sealed trait TerminationCriteria
  final case class RelativeGapTerminationCriteria (value: Double) extends TerminationCriteria
  final case class IterationTerminationCriteria (value: Int) extends TerminationCriteria
  final case class RunningTimeTerminationCriteria (value: Long) extends TerminationCriteria
  final case class AllTerminationCriteria(relGap: Double, iteration: Int, runTime: Long) extends TerminationCriteria

  abstract class TrafficAssignmentResult
  case class FWSolverResult(paths: ODPaths, finalNetwork: RoadNetwork, iterations: Int, time: Long, relGap: Double = 0D) extends TrafficAssignmentResult {
    require(relGap >= 0D && relGap <= 1D)
  }
}
