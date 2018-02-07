package cse.fitzgero.mcts.example

import java.time.Instant

import cse.fitzgero.mcts.MonteCarloTree
import cse.fitzgero.mcts.core._
import cse.fitzgero.mcts.example.TicTacToe._
import cse.fitzgero.mcts.variant.StandardMCTS


class TicTacToeSolver extends StandardMCTS[Board, Move] {
  val Cp = 0.717D

  override def applyAction(state: Board, action: Move): Board =
    state.applyMove(action)

  override def evaluate(state: Board): Double = ???

  override def generatePossibleActions(state: Board): Seq[Move] =
    Board.possibleMoves(state)

  override def selectAction(monteCarloTree: MonteCarloTree[Board, Move], actions: Seq[Move]): Option[Move] =
    actionSelection.selectAction(monteCarloTree, actions)

  override def stateIsNonTerminal(state: Board): Boolean = ???

  override def random: RandomGenerator = new BuiltInRandomGenerator(Some(0L))
  override val samplingMethod =  UCTSamplingFunction(Cp)
  override val terminationCriterion = TimeTermination(Instant.now, 5000L)
  override val actionSelection = RandomSelection(random, generatePossibleActions)

}
