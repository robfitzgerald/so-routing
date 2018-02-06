package cse.fitzgero.mcts.variant

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate

class StandardMCTSTests extends SORoutingUnitTestTemplate {
  "StandardMCTS" when {
    "given a mock problem" should {
      "solve it" in new MockProblem1 {

      }
    }
  }
}
