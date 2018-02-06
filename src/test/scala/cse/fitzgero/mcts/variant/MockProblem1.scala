package cse.fitzgero.mcts.variant
import java.time.Instant

import cse.fitzgero.mcts.MonteCarloTree
import cse.fitzgero.mcts.core._

sealed trait Piece
case object X extends Piece
case object O extends Piece
case class Board()
sealed trait Move

class MockProblem1 extends StandardMCTS[Board, Move] {
  val Cp = 0.717D
  type S = Board
  type A = Move

  override def applyAction(state: S, action: A): S = ???

  override def evaluate(state: S): Double = ???

  override def generatePossibleActions(state: S): Seq[A] = ???

  override def selectAction(monteCarloTree: MonteCarloTree[S, A], actions: Seq[A]): Option[A] = ???

  override def stateIsNonTerminal(state: S): Boolean = ???

  override def random: RandomGenerator = new BuiltInRandomGenerator(Some(0L))
  override val samplingMethod =  UCTSamplingFunction(Cp)
  override val terminationCriterion = TimeTermination(Instant.now, 5000L)
  override val actionSelection = RandomSelection(random, generatePossibleActions)

}
