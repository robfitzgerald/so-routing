package cse.fitzgero.sorouting.algorithm.pathsearch

import cse.fitzgero.sorouting.algorithm.pathsearch.ksp.{KSPBounds, KSPResult}
import cse.fitzgero.sorouting.algorithm.pathsearch.od.{ODPair, ODPath}
import cse.fitzgero.sorouting.roadnetwork.RoadNetwork

abstract class KSP [G <: RoadNetwork, A <: ODPair[_], B <: ODPath[_,_]] {
  def kShortestPaths (graph: G, od: A, k: Int, bounds: KSPBounds): KSPResult
}