package cse.fitzgero.sorouting.algorithm.shortestpath

import cse.fitzgero.sorouting.roadnetwork._

abstract class SSSP [G <: RoadNetwork, A <: ODPair[_], B <: ODPath[_,_]] {
  def shortestPath (graph: G, od: A): B
}