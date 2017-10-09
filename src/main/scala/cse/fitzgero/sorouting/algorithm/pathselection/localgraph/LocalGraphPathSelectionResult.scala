package cse.fitzgero.sorouting.algorithm.pathselection.localgraph

import cse.fitzgero.sorouting.algorithm.pathsearch.ksp.KSPResult
import cse.fitzgero.sorouting.algorithm.pathsearch.od.localgraph.LocalGraphODPath
import cse.fitzgero.sorouting.algorithm.pathselection.PathSelectionFound

import scala.collection.GenSeq

case class LocalGraphPathSelectionResult (paths: GenSeq[LocalGraphODPath], originals: GenSeq[KSPResult], runTime: Long) extends PathSelectionFound[LocalGraphODPath]
