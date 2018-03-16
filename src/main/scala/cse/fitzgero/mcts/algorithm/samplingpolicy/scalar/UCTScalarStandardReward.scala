package cse.fitzgero.mcts.algorithm.samplingpolicy.scalar

import cse.fitzgero.mcts.MonteCarloTreeSearch
import cse.fitzgero.mcts.algorithm.samplingpolicy.banditfunction.UCT
import cse.fitzgero.mcts.tree.MCTreeStandardReward

trait UCTScalarStandardReward[S,A] extends MonteCarloTreeSearch[S,A] {
  self: {
    type Reward = Double
  } =>

  /**
    * coefficient for the standard UCT algorithm
    * @param Cp exploration parameter, typically in the range [0,1] for rewards in the same range
    */
  case class Coefficients (Cp: Double)

  /**
    * Upper Confidence Bound For Trees sampling method
    * @param node the node to evaluate
    * @return
    */
  def evaluateBranch(node: Tree, c: Coefficients): Reward = {
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


//
//object UCTScalarStandardReward {
//  case class Coefficients(Cp: Double)
//  def apply[S,A](): UCTScalarStandardReward[S,A] = new UCTScalarStandardReward[S,A]
//}