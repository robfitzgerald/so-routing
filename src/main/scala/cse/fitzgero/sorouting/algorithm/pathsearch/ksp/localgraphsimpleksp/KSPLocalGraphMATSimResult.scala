package cse.fitzgero.sorouting.algorithm.pathsearch.ksp.localgraphsimpleksp

import cse.fitzgero.sorouting.algorithm.pathsearch.ksp.KSPSolution
import cse.fitzgero.sorouting.algorithm.pathsearch.od.localgraph.LocalGraphODPath

import scala.collection.GenSeq

/**
  * contains data associated with a successful k-shortest path run in the MATSim LocalGraph scenario
  * @param paths the k paths, sorted in ascending (cost) order
  * @param kRequested the requested number of unique paths, which may be greater than the kFound paths if that number is not possible for this OD pair
  * @param nExplored the value |n| of the path set n, |n| >= |k|, which are a superset of the k shortest paths
  * @param runTime the duration the ksp algorithm ran for, in milliseconds
  */
case class KSPLocalGraphMATSimResult (
  paths: GenSeq[LocalGraphODPath],
  kRequested: Int,
  nExplored: Int,
  runTime: Long,
) extends KSPSolution[LocalGraphODPath]
