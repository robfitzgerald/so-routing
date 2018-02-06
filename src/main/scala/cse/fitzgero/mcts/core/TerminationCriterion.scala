package cse.fitzgero.mcts.core

import java.time.Instant

import cse.fitzgero.mcts.MonteCarloTree

trait TerminationCriterion {
  def terminationCheck[S,A](monteCarloTree: MonteCarloTree[S,A]): Boolean
}

class TimeTermination (
  val startTime: Instant,
  val computationTimeBudget: Long
) extends TerminationCriterion {
  def terminationCheck[S,A](monteCarloTree: MonteCarloTree[S,A]): Boolean =
    Instant.now.toEpochMilli - startTime.toEpochMilli < computationTimeBudget
}

object TimeTermination {
  def apply(startTime: Instant, computationTimeBudget: Long): TimeTermination =
    new TimeTermination(startTime, computationTimeBudget)
}