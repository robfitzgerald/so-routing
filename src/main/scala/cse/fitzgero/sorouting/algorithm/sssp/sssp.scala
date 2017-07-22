package cse.fitzgero.sorouting.algorithm

package object sssp {

  abstract class ODPair [V] {
    def srcVertex: V
    def dstVertex: V
  }

  abstract class ODPath [V, E] {
    def srcVertex: V
    def dstVertex: V
    def path: List[E]
  }

  abstract class SSSP [G, A <: ODPair[_], B <: ODPath[_,_]] {
    def shortestPath (graph: G, od: A): B
  }
}
