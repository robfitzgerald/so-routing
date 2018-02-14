package cse.fitzgero.mcts.core

import cse.fitzgero.mcts.MonteCarloTree

trait SamplingFunction {
  def evaluate[S,A](monteCarloTree: MonteCarloTree[S,A], Cp: Double): Double
}

class UCTSamplingFunction extends SamplingFunction {
  /**
    * Upper Confidence Bound For Trees sampling method
    * @param node the node to evaluate
    * @return
    */
  def evaluate[S,A](node: MonteCarloTree[S,A], Cp: Double): Double = {
    val parentVisits: Long = node.parent() match {
      case None => 0L
      case Some(parent) => parent.visits
    }
    val exploitation: Double = if (node.visits == 0) 0D else node.reward / node.visits.toDouble
    val exploration: Double =
      if (Cp == 0)
        0D
      else if (node.visits == 0)
        Double.MaxValue
      else
        2 * Cp * math.sqrt(
          (2.0D * math.log(parentVisits)) /
            node.visits
        )

    exploitation + exploration
  }
}

object UCTSamplingFunction {
  def apply(): UCTSamplingFunction = new UCTSamplingFunction
}