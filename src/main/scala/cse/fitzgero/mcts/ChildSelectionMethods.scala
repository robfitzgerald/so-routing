package cse.fitzgero.mcts

object ChildSelectionMethods {
  trait SelectionMethod {
    def select[S,A](monteCarloTree: MonteCarloTree[S,A]): Option[A]
  }
  trait RandomSelection {
    self: {
      def random: scala.util.Random
    } =>
    /**
      * a helper that selects a random child in the Expand step
      * @param monteCarloTree the node we are selecting children of
      * @return one child selected by a random process
      */
    def select[S,A](monteCarloTree: MonteCarloTree[S,A]): Option[A] = {
      monteCarloTree.children map {
        children =>
          val remainingAlts = children.filter(_._2().isEmpty).toVector
          val tuple = remainingAlts(random.nextInt(remainingAlts.size))
          tuple._1
      }
    }
  }
}
