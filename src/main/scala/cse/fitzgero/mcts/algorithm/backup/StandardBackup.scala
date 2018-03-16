package cse.fitzgero.mcts.algorithm.backup

import scala.annotation.tailrec

import cse.fitzgero.mcts.MonteCarloTreeSearch

trait StandardBackup[S,A] extends MonteCarloTreeSearch[S,A] {
  @tailrec
  override protected final def backup(node: Tree, delta: Reward): Tree = {
    node.parent() match {
      case None =>
        node.update(delta)
        node
      case Some(parent) =>
        // v has a parent, so we want to update v and recurse on parent
        node.update(delta)
        backup(parent, delta)
    }
  }
}
