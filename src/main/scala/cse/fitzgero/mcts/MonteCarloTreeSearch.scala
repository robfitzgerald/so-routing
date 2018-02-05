package cse.fitzgero.mcts

import scala.collection.GenMap

trait MonteCarloTreeSearch {
  // provided MCTS algorithms
  def samplingMethod: SamplingFunctions.SamplingFunction
  def terminationCriteria: TerminationCriteria.TerminationCriterion
  def childSelectionMethod: ChildSelectionMethods.SelectionMethod

  // to be implemented by the user
  def generatePossibleActions[S,A](monteCarloTree: MonteCarloTree[S,A]): Seq[A]
  def applyAction[S,A](monteCarloTree: MonteCarloTree[S,A], action: A): MonteCarloTree[S,A]

  def run[S,A](): MonteCarloTree[S,A] = {
    val root: MonteCarloTree[S,A] = MonteCarloTree[S,A]()

    while (terminationCriteria.terminationCheck(root)) {
      val v_t = treePolicy(root)
      // the symbol for delta (∆) created an issue when default policy went from one result to a tuple result
      val (delta, newBest) = defaultPolicy(graph, v_t, globalAlts, util.random, best, util.meanCostDiff)
      best = newBest
      backup(v_t, delta)
    }

    root
  }

  def treePolicy[S,A](node: MonteCarloTree[S,A]): MonteCarloTree[S,A] = {
    if (node.hasNoChildren) {
      node
    } else if (node.hasUnexploredChildren) {
      expand(node)
    } else {
      // recurse via bestchild
      bestChild(node) match {
        case None => node
        case Some(bestChild) =>
          treePolicy(bestChild)
      }
    }
  }

  def bestChild[S,A](node: MonteCarloTree[S,A]): Option[MonteCarloTree[S,A]] = {
    // TODO: continue here
    //   add bestChild, finishes treePolicy
    //   backup, defaultPolicy todo
  }

  /**
    * chooses a child to expand via a provided selection method and attaches that new node to the tree
    * @param node the parent node we are expanding from
    * @return the new node of the tree
    */
  def expand[S,A](node: MonteCarloTree[S,A]): Option[MonteCarloTree[S,A]] = {
    for {
      children <- node.children
      action <- childSelectionMethod.select(node)
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

