package cse.fitzgero.sorouting.algorithm.pathsearch

import cse.fitzgero.sorouting.algorithm.pathsearch.od.{ODPair, ODPath}

abstract class MSSP [G, A <: ODPair[_], B <: ODPath[_,_]] {
  def shortestPaths (graph: G, odPairs: Seq[A]): Seq[B]
}