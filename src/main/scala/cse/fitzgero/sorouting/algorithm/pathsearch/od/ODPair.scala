package cse.fitzgero.sorouting.algorithm.pathsearch.od

abstract class ODPair [I] {
  def personId: String
  def src: I
  def dst: I
}