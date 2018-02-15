package cse.fitzgero.sorouting.algorithm.local.selection.mcts

import cse.fitzgero.mcts.MonteCarloTree
import cse.fitzgero.sorouting.SORoutingUnitTestTemplate
import cse.fitzgero.sorouting.algorithm.local.selection.CombinatorialTestAssets

class MCTSSolverTests extends SORoutingUnitTestTemplate {
  "MCTSSolver" when {
    "generatePossibleActions" when {
      "called with a simple alternates set" should {
        "list the alternates" in {

        }
      }
    }
    "run with a simple test case" should {
      "find the solution" in new CombinatorialTestAssets.CombinationSet {
        val solver = MCTSSolver(
          graph = graph,
          request = kspResult,
          seed = 0L,
          duration = 5000L,
          congestionThreshold = 4D,
          Cp = 0.717D
        )
        val tree: MonteCarloTree[Tag.AlternatesSet, Tag] = solver.run()
        println(tree.printTree(printDepth = 3))
        println(solver.bestGame(tree))
        println(solver.unTag(solver.bestGame(tree)))
        // TODO: test that best solution is exactly minimal (requires test case would have only one optimal solution)
      }
    }
  }
}
