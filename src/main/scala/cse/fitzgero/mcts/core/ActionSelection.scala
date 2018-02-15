package cse.fitzgero.mcts.core

trait ActionSelection[S,A] {
  def selectAction(actions: Seq[A]): Option[A]
}

class RandomSelection[S,A](
  random: RandomGenerator,
  generatePossibleActions: (S) => Seq[A]
) extends ActionSelection[S,A] {
  def selectAction(actions: Seq[A]): Option[A] = {
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