package cse.fitzgero.sorouting.algorithm.trafficassignment

import cse.fitzgero.sorouting.algorithm.shortestpath._

abstract class TrafficAssignmentResult [G]{
  def paths: Seq[ODPath[_,_]]
  def finalNetwork: G
  def iterations: Int
  def time: Long
}