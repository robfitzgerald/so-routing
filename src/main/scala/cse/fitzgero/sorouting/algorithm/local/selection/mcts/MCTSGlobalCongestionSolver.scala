package cse.fitzgero.sorouting.algorithm.local.selection.mcts

import java.time.Instant

import scala.collection.{GenMap, GenSeq}

import cse.fitzgero.mcts.core._
import cse.fitzgero.mcts.tree.MCTreeStandardReward
import cse.fitzgero.mcts.variant.StandardMCTS
import cse.fitzgero.sorouting.algorithm.local.selection.mcts.Tag._
import cse.fitzgero.sorouting.model.path.SORoutingPathSegment
import cse.fitzgero.sorouting.model.roadnetwork.local.{LocalGraph, LocalODPair}

class MCTSGlobalCongestionSolver(
                  val graph: LocalGraph,
                  val request: GenMap[LocalODPair, GenSeq[List[SORoutingPathSegment]]],
                  val congestionThreshold: Double,
                  val seed: Long = 0L,
                  val duration: Long = 5000L) extends MCTSSolver {

  override def getSearchCoefficients(tree: Tree): Coefficients = Coefficients(0.707D)

  override def getDecisionCoefficients(tree: Tree): Coefficients = Coefficients(0D)


  val globalCongestion: BigDecimal = (
    for {
      edge <- graph.edges
      cost <- edge._2.attribute.linkCostFlow
    } yield {
      BigDecimal.decimal(cost)
    }
    ).sum

  override def evaluateTerminal(state: AlternatesSet): Double = {
    val costOffset = MCTSHelpers.evaluateCostOffsetBySum(state, globalAlternates, graph)
    val resultGlobalCongestion: BigDecimal = costOffset + globalCongestion
    val difference: BigDecimal = resultGlobalCongestion - globalCongestion
//    val difference: BigDecimal = if (resultGlobalCongestion > globalCongestion) resultGlobalCongestion - globalCongestion else globalCongestion - resultGlobalCongestion
    val percentDifference: Double = (difference / globalCongestion).toDouble
    val reward = 1.0D - percentDifference
    reward
  }
}

object MCTSGlobalCongestionSolver {
  def apply(graph: LocalGraph, request: GenMap[LocalODPair, GenSeq[List[SORoutingPathSegment]]], congestionThreshold: Double, seed: Long, duration: Long): MCTSSolver =
    new MCTSGlobalCongestionSolver(graph, request, congestionThreshold, seed, duration)
  def apply(graph: LocalGraph, request: GenMap[LocalODPair, GenSeq[List[SORoutingPathSegment]]], congestionThreshold: Double): MCTSSolver =
    new MCTSGlobalCongestionSolver(graph, request, congestionThreshold)
}

