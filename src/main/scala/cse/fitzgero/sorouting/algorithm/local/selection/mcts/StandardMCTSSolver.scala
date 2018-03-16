package cse.fitzgero.sorouting.algorithm.local.selection.mcts

import java.time.Instant

import scala.collection.{GenMap, GenSeq}

import cse.fitzgero.mcts.core._
import cse.fitzgero.mcts.variant.StandardMCTS
import cse.fitzgero.sorouting.algorithm.local.selection.mcts.Tag._
import cse.fitzgero.sorouting.model.path.SORoutingPathSegment
import cse.fitzgero.sorouting.model.roadnetwork.local.{LocalGraph, LocalODPair}

class StandardMCTSSolver(
                  val graph: LocalGraph,
                  val request: GenMap[LocalODPair, GenSeq[List[SORoutingPathSegment]]],
                  val congestionThreshold: Double,
                  val seed: Long = 0L,
                  val duration: Long = 5000L) extends MCTSSolver {

  override def getSearchCoefficients(tree: Tree): Coefficients = Coefficients(0.707D)

  override def getDecisionCoefficients(tree: Tree): Coefficients = Coefficients(0D)

  override def evaluateTerminal(state: AlternatesSet): Double = {
    val fn: MCTSHelpers.EvaluationFunction = MCTSHelpers.meanCostDiff(congestionThreshold)
    fn(MCTSHelpers.produceEvaluationTuples(state, globalAlternates, graph))
  }
}

object StandardMCTSSolver {
  def apply(graph: LocalGraph, request: GenMap[LocalODPair, GenSeq[List[SORoutingPathSegment]]], congestionThreshold: Double, seed: Long, duration: Long): MCTSSolver =
    new StandardMCTSSolver(graph, request, congestionThreshold, seed, duration)
  def apply(graph: LocalGraph, request: GenMap[LocalODPair, GenSeq[List[SORoutingPathSegment]]], congestionThreshold: Double): MCTSSolver =
    new StandardMCTSSolver(graph, request, congestionThreshold)
}