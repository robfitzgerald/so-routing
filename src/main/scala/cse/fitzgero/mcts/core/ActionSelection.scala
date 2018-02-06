package cse.fitzgero.mcts.core

import cse.fitzgero.mcts.MonteCarloTree

trait ActionSelection[S,A] {
  def selectAction(monteCarloTree: MonteCarloTree[S,A], actions: Seq[A]): Option[A]
}

class RandomSelection[S,A](
  random: RandomGenerator,
  generatePossibleActions: (S) => Seq[A]
) extends ActionSelection[S,A] {
  def selectAction(monteCarloTree: MonteCarloTree[S, A], actions: Seq[A]): Option[A] = {
    monteCarloTree.children flatMap {
      children =>
        monteCarloTree.state map {
          state =>
            val validActions = children.keys.toList.diff(generatePossibleActions(state))
            validActions(random.nextInt(validActions.size))
        }
    }
  }
}

object RandomSelection {
  def apply[S,A](random: RandomGenerator, generatePossibleActions: (S) => Seq[A]): RandomSelection[S,A] =
    new RandomSelection(random, generatePossibleActions)
}