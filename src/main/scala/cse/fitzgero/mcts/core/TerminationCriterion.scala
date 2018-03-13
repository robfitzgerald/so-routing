package cse.fitzgero.mcts.core

import java.time.Instant

import cse.fitzgero.mcts.tree._

trait TerminationCriterion {
  def terminationCheck[S,A,N <: MonteCarloTree[S,A,_,_]](monteCarloTree: N): Boolean
}

class TimeTermination (
  // TODO: rewrite using scala.concurrent.duration .fromNow, hasTimeLeft methods
  val startTime: Instant,
  val computationTimeBudget: Long
) extends TerminationCriterion {
  def terminationCheck[S,A,N <: MonteCarloTree[S,A,_,_]](monteCarloTree: N): Boolean =
    Instant.now.toEpochMilli - startTime.toEpochMilli < computationTimeBudget
}

object TimeTermination {
  def apply(startTime: Instant, computationTimeBudget: Long): TimeTermination =
    new TimeTermination(startTime, computationTimeBudget)
}