package cse.fitzgero.sorouting.algorithm.pathsearch

import cse.fitzgero.sorouting.roadnetwork.RoadNetwork

import scala.collection.GenSeq

abstract class KSP [G <: RoadNetwork, A <: ODPair[_], B <: ODPath[_,_]] {
  def kShortestPaths (graph: G, od: A, k: Int): GenSeq[B]
}