package cse.fitzgero.sorouting.algorithm.pathsearch.ksp

import cse.fitzgero.sorouting.algorithm.pathsearch.od.ODPath

import scala.collection.GenSeq


sealed trait KSPResult

/**
  * if no solution exists, it is sufficient to only return the runtime spent
  * @param time duration in milliseconds which was spent finding no solution
  */
case class NoKSPSolution (time: Long = 0L) extends KSPResult

/**
  * a result of a k-shortest paths algorithm should fit these requirements
  * @tparam O origin-destination pairs with paths and costs
  */
abstract class KSPSolution [O <: ODPath[_,_]] extends KSPResult {
  /**
    * the k paths, sorted in ascending order
    */
  def paths: GenSeq[O]

  /**
    * requested number of alternate paths
    */
  def kRequested: Int

  /**
    * the n paths, n >= k, of which the k-shortest are a subset
    */
  def nExplored: Int

  /**
    * algorithm duration, milliseconds
    */
  def runTime: Long

  /**
    * the selected number of alternate paths, which can be less than or equal to $kRequested
    */
  def kSelected: Int = paths.size

  require(0 <= kRequested)
  require(0 <= kSelected)
  require(0 <= nExplored)
  require(0 <= runTime)
}