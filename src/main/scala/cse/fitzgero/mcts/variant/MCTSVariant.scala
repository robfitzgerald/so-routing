package cse.fitzgero.mcts.variant

import cse.fitzgero.mcts.MonteCarloTree

trait MCTSVariant {
  def defaultPolicy[S,A](node: MonteCarloTree[S,A]): Double
  def treePolicy[S,A](node: MonteCarloTree[S,A]): MonteCarloTree[S,A]
  def backup[S,A](node: MonteCarloTree[S,A], delta: Double): MonteCarloTree[S,A]
  def expand[S,A](node: MonteCarloTree[S,A]): Option[MonteCarloTree[S,A]]
  def bestChild[S,A](node: MonteCarloTree[S,A]): Option[MonteCarloTree[S,A]]
}
