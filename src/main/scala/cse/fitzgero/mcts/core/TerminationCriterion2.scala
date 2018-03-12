package cse.fitzgero.mcts.core

import java.time.Instant

import cse.fitzgero.mcts.tree._

trait TerminationCriterion2 {
  def terminationCheck[S,A,N <: MonteCarloTreeTop[S,A,_,_]](monteCarloTree: N): Boolean
}

class TimeTermination2 (
  // TODO: rewrite using scala.concurrent.duration .fromNow, hasTimeLeft methods
  val startTime: Instant,
  val computationTimeBudget: Long
) extends TerminationCriterion2 {
  def terminationCheck[S,A,N <: MonteCarloTreeTop[S,A,_,_]](monteCarloTree: N): Boolean =
    Instant.now.toEpochMilli - startTime.toEpochMilli < computationTimeBudget
}

object TimeTermination2 {
  def apply(startTime: Instant, computationTimeBudget: Long): TimeTermination2 =
    new TimeTermination2(startTime, computationTimeBudget)
}