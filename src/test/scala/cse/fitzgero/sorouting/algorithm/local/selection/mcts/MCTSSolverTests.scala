package cse.fitzgero.sorouting.algorithm.local.selection.mcts

import cse.fitzgero.mcts.tree.MonteCarloTree
import cse.fitzgero.sorouting.SORoutingUnitTestTemplate
import cse.fitzgero.sorouting.algorithm.local.selection.CombinatorialTestAssets

class MCTSSolverTests extends SORoutingUnitTestTemplate {
  "MCTSSolver" when {
    "run with a simple test case" should {
      "find the solution" in new CombinatorialTestAssets.CombinationSet {
        val solver = MCTSGlobalCongestionSolver(
          graph = graph,
          request = kspResult,
          seed = 0L,
          duration = 1000L,
          congestionThreshold = 3D
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
