package cse.fitzgero.mcts.algorithm.treepolicy

import scala.annotation.tailrec

import cse.fitzgero.mcts.MonteCarloTreeSearch

trait StandardTreePolicy[S,A] extends MonteCarloTreeSearch[S,A] {

  @tailrec
  override protected final def treePolicy(node: Tree, coefficients: Coefficients)(implicit ordering: Ordering[Reward]): Tree = {
    if (stateIsNonTerminal(node.state)) {
      if (hasUnexploredActions(node)) {
        expand(node) match {
          case None => node
          case Some(newChild) => newChild
        }
      } else {
        bestChild(node, coefficients) match {
          case None => node
          case Some(bestChild) =>
            treePolicy(bestChild, coefficients)
        }
      }
    } else /* terminal board state */ {
      node
    }
  }
}
