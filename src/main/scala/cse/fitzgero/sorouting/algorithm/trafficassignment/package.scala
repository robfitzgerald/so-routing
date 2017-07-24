package cse.fitzgero.sorouting.algorithm

import cse.fitzgero.sorouting.algorithm.shortestpath._

package object trafficassignment {
  abstract class TrafficAssignmentResult [G]{
    def paths: Seq[ODPath[_,_]]
    def finalNetwork: G
    def iterations: Int
    def time: Long
  }
}
