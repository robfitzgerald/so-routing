package cse.fitzgero.mcts.example

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate

class TicTacToeSolverTests extends SORoutingUnitTestTemplate {
  "TicTacToeSolver" when {
    "run with defaults" should {
      "find the full tree and optimal moves for both players" in {
        val tree = TicTacToeSolver().run()
        println(tree)
      }
    }
  }
}
