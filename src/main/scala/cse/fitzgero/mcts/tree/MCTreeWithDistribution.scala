package cse.fitzgero.mcts.tree

import cse.fitzgero.mcts.math.Distribution

/**
  * standard Monte Carlo Tree, storing a Reward as a Double.
 *
  * @param action the (optional) action used to create this node
  * @param state the state associated with this node, where action was applied to the parent state to produce the current state
  * @tparam S a state type for our state space search
  * @tparam A an action type for transitions between states
  */
class MCTreeWithDistribution[S, A] (
  override val action: Option[A],
  override val state: S
)extends MonteCarloTree [S,A,Distribution,MCTreeWithDistribution[S, A]]{
  var reward: Distribution = Distribution()
  override def update[T](reward: T): Unit = reward match {
    case x: Double => updateReward((r: Distribution) => Some(r + x))
    case x: Distribution => println("[WARN] attempting to update a Distribution reward with another distribution but Distribution ++ Distribution is not yet defined")
    case _ => ()
  }
}

object MCTreeWithDistribution {
  def apply[S,A](state: S): MCTreeWithDistribution[S,A] = new MCTreeWithDistribution[S,A](state = state, action = None)
  def apply[S,A](state: S, action: Option[A]): MCTreeWithDistribution[S,A] = {
    new MCTreeWithDistribution(state = state, action = action)
  }
}
