package cse.fitzgero.mcts.core

trait ActionSelection[S,A] {
  def selectAction(actions: Seq[A]): Option[A]
}

class RandomSelection[S,A](
  random: RandomGenerator,
  generatePossibleActions: (S) => Seq[A]
) extends ActionSelection[S,A] {
  def selectAction(actions: Seq[A]): Option[A] = {
//    val explored = monteCarloTree.children match {
//      case None => Seq[A]()
//      case Some(children) =>
//
//    }

    // TODO: am i generating valid actions twice? what is the "actions" argument here for agian?
//    val validActions = children.keys.toList.diff(generatePossibleActions(monteCarloTree.state))
//    validActions(random.nextInt(validActions.size))
    actions match {
      case Nil => None
      case xs => Some(actions(random.nextInt(actions.size)))
    }
  }
}

object RandomSelection {
  def apply[S,A](random: RandomGenerator, generatePossibleActions: (S) => Seq[A]): RandomSelection[S,A] =
    new RandomSelection(random, generatePossibleActions)
}