package cse.fitzgero.sorouting.algorithm.pathsearch

abstract class ODPair [V] {
  def srcVertex: V
  def dstVertex: V
}