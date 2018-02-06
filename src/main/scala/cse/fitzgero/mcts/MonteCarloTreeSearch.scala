package cse.fitzgero.mcts

import cse.fitzgero.mcts.Actions.ActionSelection
import cse.fitzgero.mcts.variant.MCTSVariant

trait MonteCarloTreeSearch extends MCTSVariant {
  // provided MCTS algorithms

  def samplingMethod: SamplingFunctions.SamplingFunction
  def terminationCriteria: TerminationCriteria.TerminationCriterion
  def actionSelection: ActionSelection

  // to be implemented by the user
  def random: scala.util.Random
  def generatePossibleActions[S,A](state: S): Seq[A]
  def applyAction[S,A](state: S, action: A): S
  def evaluate[S,A](monteCarloTree: MonteCarloTree[S,A]): Double
  def stateIsNonTerminal[S](state: S): Boolean

  /**
    * run this Monte Carlo Tree Search
    * @tparam S a user-provided type that represents the state of the program
    * @tparam A a user-provided type that represents an action that can be performed on the state
    * @return the tree at the end of the search
    */
  def run[S,A](): MonteCarloTree[S,A] = {

    val root: MonteCarloTree[S,A] = MonteCarloTree[S,A]()

    while (terminationCriteria.terminationCheck(root)) {
      val v_t = treePolicy(root)
      // the symbol for delta (∆) created an issue when default policy went from one result to a tuple result
      val ∆ = defaultPolicy(v_t)
      backup(v_t, ∆)
    }

    root
  }

  /**
    * find the best child of a parent node based on the selection policy of this MCTS algorithm
    * @param node the parent node
    * @return the best child, based on the evaluate function provided by the user
    */
  def bestChild[S,A](node: MonteCarloTree[S,A]): Option[MonteCarloTree[S,A]] = {
    if (node.hasNoChildren) { None }
    else {
      node.children map {
        _.flatMap {
          // produce a tuple for each valid child that is (cost, child)
          _._2() map { child => (evaluate(child), child)}
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
  def expand[S,A](node: MonteCarloTree[S,A]): Option[MonteCarloTree[S,A]] = {
    for {
      state <- node.state
      validActions <- generatePossibleActions(state)
      action <- actionSelection.selectAction(node, validActions)
      newState <- applyAction(node, action)
    } yield {
      val childsChildren: Seq[(A, () => Option[MonteCarloTree[S,A]])] =
        generatePossibleActions(newState) map {
          action: A =>
            (action, () => Option.empty[MonteCarloTree[S,A]])
        }
      val newNode = MonteCarloTree(Some(newState), Some(action), Some(childsChildren.toMap), () => Some(node))
      node.addChild(action, newNode)
      newNode
    }
  }
}

