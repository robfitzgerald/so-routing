package cse.fitzgero.sorouting.algorithm.pathsearch.ksp.localgraphsimpleksp

import cse.fitzgero.sorouting.algorithm.pathsearch.ksp.KSPSolution
import cse.fitzgero.sorouting.algorithm.pathsearch.od.localgraph.LocalGraphODPath

import scala.collection.GenSeq

case class KSPLocalGraphResult (
  paths: GenSeq[LocalGraphODPath],
  kRequested: Int,
  nExplored: Int,
  runTime: Long
) extends KSPSolution[LocalGraphODPath]