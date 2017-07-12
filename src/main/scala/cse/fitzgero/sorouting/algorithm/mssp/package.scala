package cse.fitzgero.sorouting.algorithm

package object mssp {

  abstract class ODPair [V] {
    def srcVertex: V
    def dstVertex: V
  }

  abstract class ODPath [V, E] {
    def srcVertex: V
    def dstVertex: V
    def path: List[E]
  }

  abstract class MSSP [G, A <: ODPair[_], B <: ODPath[_,_]] {
    def shortestPaths (graph: G, odPairs: Seq[A]): Seq[B]
  }
}
