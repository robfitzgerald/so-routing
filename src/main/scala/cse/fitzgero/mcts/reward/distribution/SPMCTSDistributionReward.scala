package cse.fitzgero.mcts.reward.distribution

import cse.fitzgero.mcts.reward.MCTSAlgorithm
import cse.fitzgero.mcts.reward.banditpolicy.SP_UCT
import cse.fitzgero.mcts.tree.MCTreeWithDistribution

class SPMCTSDistributionReward[S,A] extends MCTSAlgorithm[MCTreeWithDistribution[S,A], SPMCTSDistributionReward.Coefficients] {

  /**
    * Upper Confidence Bound For Trees sampling method
    * @param node the node to evaluate
    * @return
    */
  def evaluate(node: MCTreeWithDistribution[S,A], c: SPMCTSDistributionReward.Coefficients): Double = {
    val parentVisits: Long = node.parent() match {
      case None => 0L
      case Some(parent) => parent.visits
    }
    SP_UCT(
      node.reward,
      node.visits,
      parentVisits,
      c.Cp,
      c.D
    )
  }
}

object SPMCTSDistributionReward {
  case class Coefficients(Cp: Double, D: Double)
  def apply[S,A](): SPMCTSDistributionReward[S,A] = new SPMCTSDistributionReward[S,A]
}