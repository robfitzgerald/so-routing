package cse.fitzgero.sorouting.algorithm.shortestpath

abstract class ODPath [V, E] {
  def srcVertex: V
  def dstVertex: V
  def path: List[E]
}
