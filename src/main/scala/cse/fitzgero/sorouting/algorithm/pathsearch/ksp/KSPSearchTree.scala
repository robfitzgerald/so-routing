package cse.fitzgero.sorouting.algorithm.pathsearch.ksp

import cse.fitzgero.sorouting.algorithm.pathsearch.od.ODPath

import scala.collection.{GenMap, GenSeq}

// a sealed tree hierarchy for building and traversing multiple shortest paths with a common root
// note: there are k different leaf nodes for the k different paths, but, they represent only one
// vertex in the real graph.
//
//                                               KSPSearchTree     -- KSPEmptySearchTree(o)
//                                              /             \
//                                            /                \
//                       KSPEmptyType[Nothing]                    KSPSearchNode[E]
//                     /        |                               /        |        \
//                   /          |                             /          |         \
//   KSPSearchLeaf(o)   KSPInvalidNode(o)     KSPSearchLeaf(o)   KSPSearchRoot[E]   KSPSearchBranch[E]
//

trait KSPSearchTree

case object KSPEmptySearchTree extends KSPSearchTree
abstract class KSPSearchNode[E] extends KSPSearchTree {
  def children: GenMap[E, (Double, KSPSearchTree)]

  def traverse(label: E): KSPSearchTree = {
    if (children.isDefinedAt(label))
      children(label)._2
    else
      KSPInvalidNode
  }

  def printChildren(tabs: String = ""): String =
    children.map(child => s"$tabs-[id:${child._1} cost: ${child._2._1}]->\n$tabs${child._2._2.toString}").mkString("\n")
}
case class KSPSearchRoot[V,E] (children: GenMap[E, (Double, KSPSearchTree)], srcVertex: V, dstVertex: V) extends KSPSearchNode[E] {
  override def toString: String =
    s"root of ksp from $srcVertex to $dstVertex\n${printChildren()}"
}
case class KSPSearchBranch[E] (children: GenMap[E, (Double, KSPSearchTree)], depth: Int = 1) extends KSPSearchNode[E] {
  override def toString: String = {
    val tabs: String = (0 until depth).map(n=>"  ").mkString("")
    s"${tabs}branch of ksp\n${printChildren(tabs)}"
  }
}

abstract class KSPEmptyNode extends KSPSearchNode[Nothing] {
  def children: GenMap[Nothing, (Double, KSPSearchTree)] = GenMap.empty[Nothing, (Double, KSPSearchTree)]
}
case object KSPInvalidNode extends KSPEmptyNode
case object KSPSearchLeaf extends KSPEmptyNode

case class TreeBuildData [V,E] (srcVertex: V, dstVertex: V, path: List[E], cost: List[Double])

object KSPSearchTree {

  def buildTree [O <: ODPath[V,E], V, E] (paths: GenSeq[O]): KSPSearchTree = {
    def _buildTree(subPaths: GenSeq[TreeBuildData[V,E]], depth: Int = 1): GenMap[E, (Double, KSPSearchTree)] = {
      if (subPaths.isEmpty)
        Map()
      else {
        discoverAlternatives(subPaths)
          .map(tup => (tup._1, tup._2, stepIntoPaths(filterAlternatesBy(tup._1, subPaths))))
          .map(tup => tup._3 match {
            case y: GenSeq[TreeBuildData[V,E]] if y.forall(_.path.isEmpty) =>
              (tup._1, (tup._2, KSPSearchLeaf))
            case x: GenSeq[TreeBuildData[V,E]] =>
              (tup._1, (tup._2, KSPSearchBranch(_buildTree(x, depth + 1), depth)))
          }).toMap
      }
    }

    val inputData = odToTreeData[O,V,E](paths)
    paths match {
      case x if x.nonEmpty =>
        KSPSearchRoot(_buildTree(inputData), x.head.srcVertex, x.head.dstVertex)
      case _ =>
        KSPEmptySearchTree
    }
  }

  def odToTreeData [O <: ODPath[V,E],V,E] (kspResult: GenSeq[O]): GenSeq[TreeBuildData[V, E]] = {
    kspResult.map(od =>
      TreeBuildData[V,E](od.srcVertex, od.dstVertex, od.path, od.cost)
    )
  }

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

