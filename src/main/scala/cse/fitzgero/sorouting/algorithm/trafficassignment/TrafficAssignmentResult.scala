package cse.fitzgero.sorouting.algorithm.trafficassignment

sealed trait TrafficAssignmentResult

abstract class TrafficAssignmentSolution [G] extends TrafficAssignmentResult {
  def finalNetwork: G
  def iterations: Int
  def time: Long
}

case class NoTrafficAssignmentSolution(
  iterations: Int = 0,
  time: Long = 0L
) extends TrafficAssignmentResult