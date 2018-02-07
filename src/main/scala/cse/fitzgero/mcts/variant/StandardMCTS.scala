package cse.fitzgero.mcts.variant
import scala.annotation.tailrec

import cse.fitzgero.mcts.{MonteCarloTree, MonteCarloTreeSearch}

trait StandardMCTS[S,A] extends MonteCarloTreeSearch[S,A] {

  override final def treePolicy(node: MonteCarloTree[S, A]): MonteCarloTree[S, A] = {
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
  override final def defaultPolicy(monteCarloTree: MonteCarloTree[S,A]): Double = {
    monteCarloTree.state match {
      case None => Double.MaxValue
      case Some(startState) =>
        if (stateIsNonTerminal(startState)) {

          // simulate moves until a terminal game state is found, then evaluate
          @tailrec
          def _defaultPolicy(state: S): Double = {
            if (stateIsNonTerminal(state)) {
              selectAction(monteCarloTree, generatePossibleActions(state)) map {
                action => applyAction(state,action)
              } match {
                case None => Double.MaxValue
                case Some(nextState) =>
                  _defaultPolicy(nextState)
              }
            } else {
              evaluate(state)
            }
          }

          _defaultPolicy(startState)
        } else {
          // return the evaluation of this state
          evaluate(startState)
        }
    }
  }

  override final def backup(node: MonteCarloTree[S, A], delta: Double): MonteCarloTree[S, A] = {
    node.parent() match {
      case None =>
        node.updateReward(delta)
      case Some(parent) =>
        // v has a parent, so we want to update v and recurse on parent
        node.updateReward(delta)
        backup(parent, delta)
    }
  }

  /**
    * find the best child of a parent node based on the selection policy of this MCTS algorithm
    * @param node the parent node
    * @return the best child, based on the evaluate function provided by the user
    */
  override final def bestChild(node: MonteCarloTree[S,A]): Option[MonteCarloTree[S,A]] = {
    if (node.hasNoChildren) { None }
    else {
      node.children map {
        _.flatMap {
          // produce a tuple for each valid child that is (cost, child)
          _._2() flatMap { child =>
            child.state map {
              state => (evaluate(state), child)
            }
          }
        }.maxBy{_._1}
          // take the child associated with the tuple that has evaluates with the maximal reward
          ._2
      }
    }
  }

  /**
    * chooses a child to expand via a provided selection method and attaches that new node to the tree
    * @param node the parent node we are expanding from
    * @return the new node of the tree
    */
  override final def expand(node: MonteCarloTree[S,A]): Option[MonteCarloTree[S,A]] = {
    for {
      state <- node.state
      action <- actionSelection.selectAction(node, generatePossibleActions(state))
    } yield {
      val newState = applyAction(state, action)
      val newNode = MonteCarloTree(Some(newState), Some(action), None, () => Some(node))
      node.addChild(action, newNode)
      newNode
    }
  }
}

