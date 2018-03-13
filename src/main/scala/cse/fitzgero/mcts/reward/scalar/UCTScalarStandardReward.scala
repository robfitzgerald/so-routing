package cse.fitzgero.mcts.reward.scalar

import cse.fitzgero.mcts.reward.MCTSAlgorithm
import cse.fitzgero.mcts.reward.banditpolicy.UCT
import cse.fitzgero.mcts.tree.MCTreeStandardReward

class UCTScalarStandardReward[S,A] extends MCTSAlgorithm[MCTreeStandardReward[S,A], UCTScalarStandardReward.Coefficients] {

  /**
    * Upper Confidence Bound For Trees sampling method
    * @param node the node to evaluate
    * @return
    */
  def evaluate(node: MCTreeStandardReward[S,A], c: UCTScalarStandardReward.Coefficients): Double = {
    val parentVisits: Long = node.parent() match {
      case None => 0L
      case Some(parent) => parent.visits
    }
    UCT(
      node.reward,
      node.visits,
      parentVisits,
      c.Cp)
  }
}

object UCTScalarStandardReward {
  case class Coefficients(Cp: Double)
  def apply[S,A](): UCTScalarStandardReward[S,A] = new UCTScalarStandardReward[S,A]
}