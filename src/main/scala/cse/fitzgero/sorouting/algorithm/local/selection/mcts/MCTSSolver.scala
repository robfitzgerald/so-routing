package cse.fitzgero.sorouting.algorithm.local.selection.mcts

import java.time.Instant

import scala.collection.{GenMap, GenSeq}

import cse.fitzgero.mcts.core._
import cse.fitzgero.mcts.variant.StandardMCTS
import cse.fitzgero.sorouting.algorithm.local.selection.mcts.Tag._
import cse.fitzgero.sorouting.model.path.SORoutingPathSegment
import cse.fitzgero.sorouting.model.roadnetwork.local.{LocalGraph, LocalODPair}

trait MCTSSolver extends StandardMCTS[AlternatesSet, Tag] {

  def graph: LocalGraph
  def request: GenMap[LocalODPair, GenSeq[List[SORoutingPathSegment]]]
  def congestionThreshold: Double
  def seed: Long
  def duration: Long

  val globalAlternates: Tag.GlobalAlternates = Tag.repackage(request)

  /**
    * helper that transforms tags back into OD requests and corresponding paths
    * @param solution a sequence of unique alternate path tags
    * @return
    */
  def unTag(solution: AlternatesSet): GenMap[LocalODPair, List[SORoutingPathSegment]] = Tag.unTagAlternates(solution, globalAlternates)

  override def generatePossibleActions(state: AlternatesSet): Seq[Tag] = {
    val noPersonRepeats = globalAlternates.filterNot {
      person => state.exists {
        tag => tag.personId == person._1
      }
    }
    val allAlternates = noPersonRepeats.flatMap {
      person => person._2 map {
        alt => Tag(person._1, alt._1)
      }
    }
    allAlternates.toList
  }

  override def stateIsNonTerminal(state: AlternatesSet): Boolean =
    state.size < globalAlternates.size

  override def applyAction(state: AlternatesSet, action: Tag): AlternatesSet = state :+ action
  override def selectAction(actions: Seq[Tag]): Option[Tag] = actionSelection.selectAction(actions)

  // utilities
  override def startState: AlternatesSet = Seq()
  override def samplingMethod: SamplingFunction = UCTSamplingFunction()
  override def actionSelection: ActionSelection[AlternatesSet, Tag] = RandomSelection(random, generatePossibleActions)
  override val random: RandomGenerator = new BuiltInRandomGenerator(Some(seed))
  override val terminationCriterion: TerminationCriterion = TimeTermination(Instant.now, duration)
}