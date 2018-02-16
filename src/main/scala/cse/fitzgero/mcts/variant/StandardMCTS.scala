package cse.fitzgero.mcts.variant
import scala.annotation.tailrec

import cse.fitzgero.mcts.{MonteCarloTree, MonteCarloTreeSearch}

trait StandardMCTS[S,A] extends MonteCarloTreeSearch[S,A] {

  @tailrec
  override protected final def treePolicy(node: MonteCarloTree[S, A], Cp: Double): MonteCarloTree[S, A] = {
    if (stateIsNonTerminal(node.state)) {
      if (hasUnexploredActions(node)) {
        expand(node) match {
          case None => node
          case Some(newChild) => newChild
        }
      } else {
        bestChild(node, Cp) match {
          case None => node
          case Some(bestChild) =>
            treePolicy(bestChild, Cp)
        }
      }
    } else {
      node
    }
  }


  override protected final def defaultPolicy(monteCarloTree: MonteCarloTree[S,A]): Double = {
    if (stateIsNonTerminal(monteCarloTree.state)) {

      // simulate moves until a terminal game state is found, then evaluate
      @tailrec
      def _defaultPolicy(state: S): Double = {
        if (stateIsNonTerminal(state)) {
          selectAction(generatePossibleActions(state)) map { applyAction(state,_) } match {
            case None =>
              // should never reach this line if State and Actions are well defined
              Double.MaxValue
            case Some(nextState) =>
              _defaultPolicy(nextState)
          }
        } else {
          evaluate(state)
        }
      }

      _defaultPolicy(monteCarloTree.state)
    } else {
      evaluate(monteCarloTree.state)
    }
  }

  @tailrec
  override protected final def backup(node: MonteCarloTree[S, A], delta: Double): MonteCarloTree[S, A] = {
    node.parent() match {
      case None =>
        node.updateReward(delta)
      case Some(parent) =>
        // v has a parent, so we want to update v and recurse on parent
        node.updateReward(delta)
        backup(parent, delta)
    }
  }

  override protected final def bestChild(node: MonteCarloTree[S,A], Cp: Double): Option[MonteCarloTree[S,A]] = {
    if (node.hasNoChildren) { None }
    else {
      node.children map {
        _.map {
          tuple =>
          // produce a tuple for each valid child that is (cost, child)
          val child: MonteCarloTree[S,A] = tuple._2()
          (samplingMethod.evaluate(child, Cp), child)
        }.maxBy{_._1}
          // take the child associated with the tuple that has evaluates with the maximal reward
          ._2
      }
    }
  }

  override protected final def expand(node: MonteCarloTree[S,A]): Option[MonteCarloTree[S,A]] = {
    for {
      action <- actionSelection.selectAction(generatePossibleActions(node.state))
    } yield {
      val newState = applyAction(node.state, action)
      val newNode = MonteCarloTree(newState, Some(action), None, node)
      node.addChild(action, newNode)
      newNode
    }
  }
}

