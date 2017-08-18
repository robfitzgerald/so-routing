package cse.fitzgero.sorouting.algorithm.pathsearch

import cse.fitzgero.sorouting.algorithm.pathsearch.od.{ODPair, ODPath}
import cse.fitzgero.sorouting.roadnetwork._

abstract class SSSP [G <: RoadNetwork, A <: ODPair[_], B <: ODPath[_,_]] {
  def shortestPath (graph: G, od: A): B
}