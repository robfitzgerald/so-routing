package cse.fitzgero.mcts

import java.time.Instant

object TerminationCriteria {
  trait TerminationCriterion {
    def terminationCheck[S,A](monteCarloTree: MonteCarloTree[S,A]): Boolean
  }
  trait TimeTermination extends TerminationCriterion {
    self: {
      def startTime: Instant
      def computationTimeBudget: Long
    } =>
    def terminationCheck[S,A](monteCarloTree: MonteCarloTree[S,A]): Boolean =
      Instant.now.toEpochMilli - startTime.toEpochMilli < computationTimeBudget
  }
}
