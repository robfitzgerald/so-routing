package cse.fitzgero.mcts

object Actions {
  trait ActionSelection {
    def selectAction[S,A](monteCarloTree: MonteCarloTree[S,A], actions: Seq[A]): Option[A]
  }
  trait RandomSelection extends ActionSelection {
    self: {
      def random: scala.util.Random
      def generatePossibleActions[S, A](state: S): Seq[A]
    } =>

    def selectAction[S, A](monteCarloTree: MonteCarloTree[S, A], actions: Seq[A]): Option[A] = {
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
}
