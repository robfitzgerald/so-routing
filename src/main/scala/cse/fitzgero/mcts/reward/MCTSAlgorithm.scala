package cse.fitzgero.mcts.reward

trait MCTSAlgorithm[Tree, Coef] {
//  type Coefficients
  def evaluate(monteCarloTree: Tree, coefficients: Coef): Double
}