package cse.fitzgero.mcts.tree

import cse.fitzgero.mcts.algorithm.samplingpolicy.scalar.UCTScalarStandardReward

/**
  * standard Monte Carlo Tree, storing a Reward as a Double.
 *
  * @param action the (optional) action used to create this node
  * @param state the state associated with this node, where action was applied to the parent state to produce the current state
  * @tparam S a state type for our state space search
  * @tparam A an action type for transitions between states
  */
class MCTreeStandardReward[S, A] (
  override val action: Option[A],
  override val state: S
)extends MonteCarloTree [S,A,Double,MCTreeStandardReward[S, A]] {
  var reward: Double = 0D
  override def update[T](reward: T): Unit = reward match {
    case x: Double => updateReward((r: Double) => Some(r + x))
    case _ => ()
  }
}

object MCTreeStandardReward {
  def apply[S,A](state: S): MCTreeStandardReward[S,A] = new MCTreeStandardReward[S,A](state = state, action = None)
  def apply[S,A](state: S, action: Option[A]): MCTreeStandardReward[S,A] = {
    new MCTreeStandardReward(state = state, action = action)
  }
}