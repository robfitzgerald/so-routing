package cse.fitzgero.sorouting.algorithm.pathselection

import cse.fitzgero.sorouting.algorithm.pathsearch.od.ODPath

import scala.collection.GenSeq

sealed trait PathSelectionResult
abstract class PathSelectionFound [O <: ODPath[_,_]] extends PathSelectionResult {
  def paths: GenSeq[O]
  def runTime: Long
}
object PathSelectionNotFound extends PathSelectionResult