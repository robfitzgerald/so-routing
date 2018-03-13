package cse.fitzgero.mcts.reward.banditpolicy

import cse.fitzgero.mcts.math.Distribution

object SP_UCT {
  def apply(reward: Distribution, childVisits: Long, parentVisits: Long, Cp: Double, D: Double): Double = {

    val exploitation: Double =
      if (childVisits == 0)
        0D
      else
        reward.mean match {
          case None => 0D
          case Some(mean) => mean / childVisits.toDouble
        }

    val exploration: Double =
      if (Cp == 0)
        0D
      else if (childVisits == 0)
        Double.MaxValue
      else
        2 * Cp * math.sqrt((2.0D * math.log(parentVisits)) / childVisits)

    val possibleDeviation: Double = {
      val variance = reward.sampleVariance.getOrElse(0D)
      if (childVisits == 0)
        0D
      else
        math.sqrt(variance + (D / childVisits))
    }

    exploitation + exploration + possibleDeviation
  }
}
