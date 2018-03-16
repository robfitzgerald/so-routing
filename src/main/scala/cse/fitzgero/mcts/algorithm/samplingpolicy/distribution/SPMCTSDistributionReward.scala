package cse.fitzgero.mcts.algorithm.samplingpolicy.distribution

import cse.fitzgero.mcts.MonteCarloTreeSearch
import cse.fitzgero.mcts.algorithm.samplingpolicy.banditfunction.SP_UCT
import cse.fitzgero.mcts.math.Distribution
import cse.fitzgero.mcts.tree.MCTreeWithDistribution

trait SPMCTSDistributionReward[S,A] extends MonteCarloTreeSearch[S,A] {
  self: {
    type Reward = Distribution
  } =>

  /**
    * coefficients for the SP_UCT algorithm
    * @param Cp exploration parameter, typically 1 over root 2, exploitation around 0.1, exploration around 1
    * @param D variance parameter, an integer. paper suggests exploitation value of 32, exploration value of 20000
    */
  case class Coefficients (Cp: Double, D: Double)

  /**
    * Upper Confidence Bound For Trees sampling method
    * @param node the node to evaluate
    * @return
    */
  final override def evaluateBranch(node: Tree, coefficients: Coefficients): Reward = {
    val parentVisits: Long = node.parent() match {
      case None => 0L
      case Some(parent) => parent.visits
    }
    SP_UCT(
      node.reward,
      node.visits,
      parentVisits,
      coefficients.Cp,
      coefficients.D
    )
  }
}

//object SPMCTSDistributionReward {
//  case class Coefficients(Cp: Double, D: Double)
//  def apply[S,A](): SPMCTSDistributionReward[S,A] = new SPMCTSDistributionReward[S,A]
//}