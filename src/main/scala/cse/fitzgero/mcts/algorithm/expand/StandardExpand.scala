package cse.fitzgero.mcts.algorithm.expand

import cse.fitzgero.mcts.MonteCarloTreeSearch

trait StandardExpand[S,A] extends MonteCarloTreeSearch[S,A] {
  override protected final def expand(node: Tree): Option[Tree] = {
    for {
      action <- actionSelection.selectAction(generatePossibleActions(node.state))
    } yield {
      val newState = applyAction(node.state, action)
      val newNode = createNewNode(newState, Some(action))
      node.addChild(newNode)
      newNode
    }
  }
}
