package cse.fitzgero.mcts.tree

/**
  * standard Monte Carlo Tree, storing a Reward as a Double.
 *
  * @param action the (optional) action used to create this node
  * @param state the state associated with this node, where action was applied to the parent state to produce the current state
  * @tparam S a state type for our state space search
  * @tparam A an action type for transitions between states
  */
class MonteCarloTree2[S, A] (
  override val action: Option[A],
  override val state: S
)extends MonteCarloTreeTop [S,A,Double,MonteCarloTree2[S, A]]{
  var reward: Double = 0D
  override def update(reward: Double): Unit = updateReward((r: Double) => Some(r + reward))
}

object MonteCarloTree2 {
  def apply[S,A](state: S): MonteCarloTree2[S,A] = new MonteCarloTree2[S,A](state = state, action = None)
  def apply[S,A](state: S, action: Option[A]): MonteCarloTree2[S,A] = {
    new MonteCarloTree2(state = state, action = action)
  }
}