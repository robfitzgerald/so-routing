package cse.fitzgero.mcts.variant
import cse.fitzgero.mcts.MonteCarloTree
import cse.fitzgero.mcts

trait UCT extends MCTSVariant {
  self: {
    def stateIsNonTerminal[S](state: S): Boolean
    def selectAction[S,A](monteCarloTree: MonteCarloTree[S,A], actions: Seq[A]): Option[A]
    def generatePossibleActions[S,A](state: S): Seq[A]
    def applyAction[S,A](state: S, action: A): S
  } =>

  override def treePolicy[S, A](node: MonteCarloTree[S, A]): MonteCarloTree[S, A] = {
    if (node.hasNoChildren) {
      node
    } else if (node.hasUnexploredChildren) {
      expand(node) match {
        case None => node
        case Some(newChild) => newChild
      }
    } else {
      // recurse via bestchild
      bestChild(node) match {
        case None => node
        case Some(bestChild) =>
          treePolicy(bestChild)
      }
    }
  }


  // TODO: default policy, tree policy, and backup will want to be abstracted out
  // pull them together into a trait that should be supplied
  // then we can have UCT, UCT_with_AMAF, etc.
  override def defaultPolicy[S,A](monteCarloTree: MonteCarloTree[S,A]): Double = {
    monteCarloTree.state match {
      case None => Double.MaxValue
      case Some(state) =>
        if (stateIsNonTerminal(state)) {
          // choose action at random

          selectAction(state)
          // apply to current state
          // recurse
        } else {
          // return the evaluation of this state
        }
    }
  }

  override def backup[S, A](node: MonteCarloTree[S, A], delta: Double): MonteCarloTree[S, A] = ???
}

