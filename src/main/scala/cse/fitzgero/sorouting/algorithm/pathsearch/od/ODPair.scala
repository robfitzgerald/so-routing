package cse.fitzgero.sorouting.algorithm.pathsearch.od

abstract class ODPair [V] {
  def personId: String
  def srcVertex: V
  def dstVertex: V
}