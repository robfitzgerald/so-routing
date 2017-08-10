package cse.fitzgero.sorouting.algorithm.pathsearch.ksp

import cse.fitzgero.sorouting.algorithm.pathsearch.od.ODPath

import scala.collection.GenSeq

//                     KSPResultTree
//                  /                   \
//  KSPSearchLeaf                           KSPSearchNode[E]
//                                      /                      \
//                    KSPSearchRoot[E]                             KSPSearchTree
//
// constructChildren should be accessible for both KSPSearchNode child classes, right?
// start with root building. get the types so they work there and we are building the children set of the root.
// trait composition is blowing my mind up. is this even a trait composition situation?
// case class Branch extends KSPSearchNode with Children
// where KSPSearchNode doesn't do shit?  what am i thinking right now..? probably should just try again.

sealed trait KSPSearchTree

case object KSPSearchLeaf extends KSPSearchTree
case object KSPEmptySearchTree extends KSPSearchTree
abstract class KSPSearchNode[E] extends KSPSearchTree {
  def children: GenSeq[(E, Double, KSPSearchTree)]
}
case class KSPSearchRoot[V,E] (children: GenSeq[(E, Double, KSPSearchTree)], srcVertex: V, dstVertex: V) extends KSPSearchNode[E]
case class KSPSearchBranch[E] (children: GenSeq[(E, Double, KSPSearchTree)]) extends KSPSearchNode[E]

case class TreeBuildData [V,E] (srcVertex: V, dstVertex: V, path: List[E], cost: List[Double])

object KSPSearchTree {

  def buildTree [O <: ODPath[V,E], V, E] (paths: GenSeq[O]): KSPSearchTree = {
    def _buildTree(subPaths: GenSeq[TreeBuildData[V,E]]): GenSeq[(E, Double, KSPSearchTree)] = {
      if (subPaths.isEmpty)
        List()
      else {
        discoverAlternatives(subPaths)
          .map(tup=> (tup._1, tup._2, stepIntoPaths(filterAlternatesBy(tup._1, subPaths))))
          .map(tup => tup._3 match {
            case Nil =>
              (tup._1, tup._2, KSPSearchLeaf)
            case x: GenSeq[TreeBuildData[V,E]] =>
              (tup._1, tup._2, KSPSearchBranch(_buildTree(x)))
          })
      }
    }

    val inputData = odToTreeData[O,V,E](paths)
    paths match {
      case x :: xs =>
        KSPSearchRoot(_buildTree(inputData), x.srcVertex, x.dstVertex)
      case _ =>
        KSPEmptySearchTree
    }
  }

  def odToTreeData [O <: ODPath[V,E],V,E] (kspResult: GenSeq[O]): GenSeq[TreeBuildData[V, E]] = {
    kspResult.map(od =>
      TreeBuildData[V,E](od.srcVertex, od.dstVertex, od.path, od.cost)
    )
  }

//  def discoverAlternatives2 [V, E] (paths: GenSeq[TreeBuildData[V,E]]): GenSeq[E] =
//    paths
//      .flatMap(_.path.headOption)
//      .distinct

  def discoverAlternatives [V, E] (paths: GenSeq[TreeBuildData[V,E]]): GenSeq[(E,Double)] =
    paths
      .flatMap(p => p.path.zip(p.cost).headOption)
      .distinct

  def filterAlternatesBy [V, E] (step: E, paths: GenSeq[TreeBuildData[V,E]]): GenSeq[TreeBuildData[V,E]] = {
    paths
      .filter(_.path.headOption match {
        case Some(x) => x == step
        case None => false
      })
  }

  def stepIntoPaths [V, E] (paths: GenSeq[TreeBuildData[V,E]]): GenSeq[TreeBuildData[V,E]] = {
    paths
      .flatMap(od => {
        od.path match {
          case x :: xs => od.cost match {
            case y :: ys =>
              Some(od.copy(path = xs, cost = ys))
            case _ => None
          }
          case _ => None
        }
      })
  }

}

