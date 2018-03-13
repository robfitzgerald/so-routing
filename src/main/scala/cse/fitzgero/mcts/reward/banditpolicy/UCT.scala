package cse.fitzgero.mcts.reward.banditpolicy

object UCT {
  def apply(reward: Double, childVisits: Long, parentVisits: Long, Cp: Double): Double = {
    val exploitation: Double = if (childVisits == 0) 0D else reward / childVisits.toDouble
    val exploration: Double =
      if (Cp == 0)
        0D
      else if (childVisits == 0)
        Double.MaxValue
      else
        2 * Cp * math.sqrt((2.0D * math.log(parentVisits)) / childVisits)

    exploitation + exploration
  }
}
