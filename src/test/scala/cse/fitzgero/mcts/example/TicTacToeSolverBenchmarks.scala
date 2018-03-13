package cse.fitzgero.mcts.example

import cse.fitzgero.mcts.tree.MonteCarloTree
import cse.fitzgero.sorouting.SORoutingUnitTestTemplate

class TicTacToeSolverBenchmarks extends SORoutingUnitTestTemplate {
  "Monte Carlo Tree Search" when {
    "Tic Tac Toe Solver" when {
      "Run with different time budgets" should {
        "Improve on average reward" in {
          val durations = List(
              1000L, // 1  sec
              5000L, // 5  sec
             20000L, // 20 sec
             60000L, // 1  min
            120000L, // 2  min
            360000L  // 5  min
          )
          val results: List[(String, String)] = for {
            duration <- durations
          } yield {
            val solver = TicTacToeSolver(duration = duration, seed = 1L, Cp = 0.717D)
            val tree = solver.run()
            val report = s"$duration,${tree.visits},${tree.reward},${tree.reward / tree.visits},${solver.bestGame(tree).mkString(" -> ")}"
//            (report, tree.printBestTree(9, solver.evaluate))
            (report, tree.toString)
          }
          println("milliseconds,iterations,reward,average_reward,moves")
          println(results.map{_._1}.mkString("\n"))
          println("\nbest move trees")
          println(results.map{_._2}.mkString("\n"))
        }
      }
    }
  }
}
