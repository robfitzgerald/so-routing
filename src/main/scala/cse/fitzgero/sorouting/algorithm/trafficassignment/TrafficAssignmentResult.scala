package cse.fitzgero.sorouting.algorithm.trafficassignment

import cse.fitzgero.sorouting.roadnetwork.RoadNetwork

/**
  * abstract base class for any result of a traffic assignment algorithm
  */
sealed trait TrafficAssignmentResult {}

/**
  * abstract class for a result with a solution, which is defined with the type of traffic assignment algorithm
  */
abstract class TrafficAssignmentSolution [G] extends TrafficAssignmentResult {
  def finalNetwork: G
  def iterations: Int
  def time: Long
}

/**
  * universal tuple for a no solution result, in place of the TrafficAssignmentSolution subclass
  * @param iterations number of iterations run
  * @param time time run
  */
case class NoTrafficAssignmentSolution (
  iterations: Int = 0,
  time: Long = 0L
) extends TrafficAssignmentResult