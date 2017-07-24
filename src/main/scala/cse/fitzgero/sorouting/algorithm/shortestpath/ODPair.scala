package cse.fitzgero.sorouting.algorithm.shortestpath

abstract class ODPair [V] {
  def srcVertex: V
  def dstVertex: V
}