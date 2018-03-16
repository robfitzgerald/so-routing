package cse.fitzgero.mcts.algorithm.bestchild

import cse.fitzgero.mcts.MonteCarloTreeSearch

trait StandardBestChild[S,A] extends MonteCarloTreeSearch[S,A] {
  override protected final def bestChild(node: Tree, coefficients: Coefficients)(implicit ordering: Ordering[Reward]): Option[Tree] = {
    if (node.hasNoChildren) { None }
    else {
      val children = node.childrenNodes.values map {
        tree: Tree => (evaluateBranch(tree, coefficients), tree)
      }
      val bestChild = children.maxBy{_._1}._2
      Some(bestChild)
    }
  }
}
