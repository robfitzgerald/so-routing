package cse.fitzgero.sorouting.algorithm.shortestpath

abstract class SSSP [G, A <: ODPair[_], B <: ODPath[_,_]] {
  def shortestPath (graph: G, od: A): B
}