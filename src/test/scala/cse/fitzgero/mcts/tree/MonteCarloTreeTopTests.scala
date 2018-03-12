package cse.fitzgero.mcts.tree

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate

class MonteCarloTreeTopTests extends SORoutingUnitTestTemplate {
  "MonteCarloTreeTop" when {

    "called with simple State, Action, and Reward types" should {

      "properly constructed" in new TestAssets.SimpleTree {
        println(tree)
      }

      "properly update" in new TestAssets.SimpleTree {
        val myNewReward: Double = 100D
        tree.update(myNewReward)
        tree.reward should equal (myNewReward)
        tree.visits should equal (1)
      }

      "properly add a child" in new TestAssets.SimpleTree {
        newChild.depth should equal (0)
        newChild.parent() should equal (None)
        tree.addChild(newChild)
        tree.children match {
          case None => fail()
          case Some(children) =>
            children.size should equal (1)
            children(newChild.action.get)() should equal (newChild)
            newChild.parent() should equal (Some(tree))
            tree.parent() should equal (None)
            newChild.depth should equal (1)
        }
      }
    }
  }
}
