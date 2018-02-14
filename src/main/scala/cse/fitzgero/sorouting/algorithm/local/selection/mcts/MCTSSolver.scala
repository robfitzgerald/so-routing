package cse.fitzgero.sorouting.algorithm.local.selection.mcts

import java.time.Instant

import scala.collection.{GenMap, GenSeq}

import cse.fitzgero.mcts.core._
import cse.fitzgero.mcts.variant.StandardMCTS
import cse.fitzgero.sorouting.algorithm.local.selection.mcts.AltPathSelectionProblem._
import cse.fitzgero.sorouting.model.path.SORoutingPathSegment
import cse.fitzgero.sorouting.model.roadnetwork.local.LocalODPair

class MCTSSolver(
  request: GenMap[LocalODPair, GenSeq[List[SORoutingPathSegment]]],
  seed: Long = 0L,
  duration: Long = 5000L,
  val Cp: Double = 0.717D) extends StandardMCTS[AlternatesSet, Tag] {

  val globalAlternates: AltPathSelectionProblem.GlobalAlternates = AltPathSelectionProblem.repackage(request)

  override def evaluate(state: AlternatesSet): Double = ???

  override def generatePossibleActions(state: AlternatesSet): Seq[Tag] = ???

  override def stateIsNonTerminal(state: AlternatesSet): Boolean = ???

  override def applyAction(state: AlternatesSet, action: Tag): AlternatesSet = state :+ action
  override def selectAction(actions: Seq[Tag]): Option[Tag] = actionSelection.selectAction(actions)

  // utilities
  override def startState: AlternatesSet = Seq()
  override def random: RandomGenerator = new BuiltInRandomGenerator(Some(seed))
  override def samplingMethod: SamplingFunction = UCTSamplingFunction()
  override def terminationCriterion: TerminationCriterion = TimeTermination(Instant.now, duration)
  override def actionSelection: ActionSelection[AlternatesSet, Tag] = RandomSelection(random, generatePossibleActions)
}

object MCTSSolver {
  def apply(): MCTSSolver = new MCTSSolver()
  def apply(seed: Long, duration: Long, Cp: Double): MCTSSolver =
    new MCTSSolver(seed, duration, Cp)
}