package cse.fitzgero.mcts.variant

import scala.annotation.tailrec

import cse.fitzgero.mcts.MonteCarloTreeSearch
import cse.fitzgero.mcts.tree._
import cse.fitzgero.mcts.reward.scalar.UCTScalarStandardReward._

trait StandardMCTS[S,A] extends MonteCarloTreeSearch[S,A,Double,Coefficients] {

  override type Tree = MCTreeStandardReward[S,A]

  override def startNode(s: S): MCTreeStandardReward[S, A] = MCTreeStandardReward(s)

  @tailrec
  override protected final def treePolicy(node: Tree, Cp: Double): Tree = {
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
    } else /* terminal board state */ {
      node
    }
  }


  override protected final def defaultPolicy(monteCarloTree: Tree): Double = {
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
  override protected final def backup(node: Tree, delta: Double): Tree = {
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

  override protected final def bestChild(node: Tree, Cp: Double): Option[Tree] = {
    if (node.hasNoChildren) { None }
    else {
      node.children map {
        _.map {
          tuple =>
          // produce a tuple for each valid child that is (cost, child)
          val child: Tree = tuple._2()
          (samplingMethod.evaluate(child, Coefficients(Cp)), child)
        }.maxBy{_._1}
          // take the child associated with the tuple that has evaluates with the maximal reward
          ._2
      }
    }
  }

  override protected final def expand(node: Tree): Option[Tree] = {
    for {
      action <- actionSelection.selectAction(generatePossibleActions(node.state))
    } yield {
      val newState = applyAction(node.state, action)
      val newNode = MCTreeStandardReward(newState, Some(action))
      node.addChild(newNode)
      newNode
    }
  }
}

