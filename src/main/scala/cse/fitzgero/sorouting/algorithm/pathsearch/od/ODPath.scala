package cse.fitzgero.sorouting.algorithm.pathsearch.od

abstract class ODPath [V, E] {
  def personId: String
  def srcVertex: V
  def dstVertex: V
  def path: List[E]
  def cost: List[Double]
}