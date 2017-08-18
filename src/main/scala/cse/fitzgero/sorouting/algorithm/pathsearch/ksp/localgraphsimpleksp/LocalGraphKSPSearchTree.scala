package cse.fitzgero.sorouting.algorithm.pathsearch.ksp.localgraphsimpleksp

import cse.fitzgero.sorouting.algorithm.pathsearch.ksp.KSPSearchTree
import cse.fitzgero.sorouting.algorithm.pathsearch.od.localgraph.LocalGraphODPath
import cse.fitzgero.sorouting.roadnetwork.localgraph._

import scala.collection.GenSeq

object LocalGraphKSPSearchTree {
  def apply (kspResult: GenSeq[LocalGraphODPath]): KSPSearchTree =
    KSPSearchTree.buildTree[LocalGraphODPath, VertexId, String](kspResult)
}