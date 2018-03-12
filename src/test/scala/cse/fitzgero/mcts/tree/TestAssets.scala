package cse.fitzgero.mcts.tree

object TestAssets {
  trait SimpleTree {
    type State = String
    type Action = String
    type Reward = Double

    class Tree(override val state: State, override val action: Option[Action], override var reward: Reward) extends MonteCarloTreeTop[State,Action,Reward,Tree] {
      def update(other: Reward): Unit = {
        this.updateReward(
          (myReward: Double) =>
            if (myReward < other) Some(other) else None
        )
      }
    }

    val tree = new Tree("born", None, 10D)
    val newChild = new Tree("a skier", Some("goes skiing"), 2000D)
  }
}
