package cse.fitzgero.sorouting.algorithm.local.selection

import scala.collection.GenMap

import cse.fitzgero.sorouting.algorithm.local.selection.SelectionMCTSAlgorithm.{MCTSTreeNode, Tag}

object MCTSTestAssets {
  trait SimpleTree {
    lazy val grandChild: MCTSTreeNode = MCTSTreeNode(0, 0, Seq(), None, Some(Tag("grandChild", 1)), () => Some(child))
    lazy val child: MCTSTreeNode = MCTSTreeNode(0, 0, Seq(), Some(GenMap(Tag("child", 1) -> {() => Some(grandChild)})), Some(Tag("b", 2)), () => Some(tree))
    lazy val tree: MCTSTreeNode = MCTSTreeNode(0, 0, Seq(), Some(GenMap(Tag("base", 2) -> {() => Some(child)})), None, () => None)
  }
}
