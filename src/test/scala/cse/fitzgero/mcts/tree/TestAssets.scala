package cse.fitzgero.mcts.tree

object TestAssets {
  trait SimpleTree {
    type State = String
    type Action = String
    type Reward = Double

    class Tree(override val state: State, override val action: Option[Action], override var reward: Reward) extends MonteCarloTree[State,Action,Reward,Tree] {
      def update[T](other: T): Unit = other match {
        case x: Double =>
          this.updateReward(
            (myReward: Double) =>
              if (myReward < x) Some(x) else None
          )
        case _ => ()
      }
    }

    val tree = new Tree("born", None, 10D)
    val newChild = new Tree("a skier", Some("goes skiing"), 2000D)
  }
}
