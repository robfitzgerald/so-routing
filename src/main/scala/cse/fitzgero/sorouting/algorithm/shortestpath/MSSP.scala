package cse.fitzgero.sorouting.algorithm.shortestpath

abstract class MSSP [G, A <: ODPair[_], B <: ODPath[_,_]] {
  def shortestPaths (graph: G, odPairs: Seq[A]): Seq[B]
}