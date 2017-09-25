package cse.fitzgero.sorouting.algorithm.pathselection.localgraph

import cse.fitzgero.sorouting.algorithm.pathsearch.od.localgraph.LocalGraphODPath
import cse.fitzgero.sorouting.algorithm.pathselection.PathSelectionFound

import scala.collection.GenSeq

case class LocalGraphPathSelectionResult (paths: GenSeq[LocalGraphODPath], runTime: Long) extends PathSelectionFound[LocalGraphODPath]
