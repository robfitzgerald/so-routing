package cse.fitzgero.sorouting.algorithm.local.selection.mcts

import scala.collection.{GenMap, GenSeq}

import cse.fitzgero.sorouting.algorithm.local.selection.mcts.Tag._
import cse.fitzgero.sorouting.model.path.SORoutingPathSegment
import cse.fitzgero.sorouting.model.roadnetwork.local.{LocalGraph, LocalODPair}

class MCTSGlobalCongestionSolver02(
                  val graph: LocalGraph,
                  val request: GenMap[LocalODPair, GenSeq[List[SORoutingPathSegment]]],
                  val congestionThreshold: Double,
                  val seed: Long = 0L,
                  val duration: Long = 5000L) extends MCTSSolver {

  // for any road network, we can always calculate the cost-flow of the entire network with all flows at 0, and it will be a lower bounds for any possible congestion state.
  // for our reward value, we could look at something like
  // (added batch congestion - lower bounds) - (current congestion - lower bounds) / (current congestion - lower bounds)
  // which would still be global but could scale better to the range [0, 1]

  val (globalFreeFlowCost: BigDecimal, globalCongestion: BigDecimal) = (for {
      edge <- graph.edges
      freeFlowCost <- edge._2.attribute.freeFlowCostFlow
      cost <- edge._2.attribute.linkCostFlow
    } yield {
      (BigDecimal.decimal(freeFlowCost), BigDecimal.decimal(cost))
    }).reduce {
      (a, b) =>
        (a._1 + b._1, a._2 + b._2)
    }

  val globalCongestionNormalized: BigDecimal = globalCongestion - globalFreeFlowCost
  val sqrtGlobalCongestion: BigDecimal = BigDecimal.decimal(math.sqrt(globalCongestion.toDouble))

  val transform: (BigDecimal) => Double = MCTSGlobalCongestionSolver02.doubleCongestionUpperBoundTransform(globalCongestion)

  override def evaluate(state: AlternatesSet): Double = {
    val costOffset = MCTSHelpers.evaluateCostOffset(state, globalAlternates, graph)
//    val resultGlobalCongestionNormalized: BigDecimal = costOffset + globalCongestionNormalized
//    val resultGlobalCongestion: BigDecimal = costOffset + globalCongestion
//    val difference: BigDecimal = resultGlobalCongestion - globalCongestionNormalized
//    val difference: BigDecimal = if (resultGlobalCongestion > globalCongestion) resultGlobalCongestion - globalCongestion else globalCongestion - resultGlobalCongestion
//    val percentDifference: Double = (difference / globalCongestionNormalized).toDouble
//    val reward: Double = transform(resultGlobalCongestionNormalized)
    val reward: Double = MCTSGlobalCongestionSolver02.negLogTransform(sqrtGlobalCongestion)(costOffset)
    //    val reward = 1.0D - percentDifference
    reward
  }
}

object MCTSGlobalCongestionSolver02 {
  def apply(graph: LocalGraph, request: GenMap[LocalODPair, GenSeq[List[SORoutingPathSegment]]], congestionThreshold: Double, seed: Long, duration: Long): MCTSSolver =
    new MCTSGlobalCongestionSolver02(graph, request, congestionThreshold, seed, duration)
  def apply(graph: LocalGraph, request: GenMap[LocalODPair, GenSeq[List[SORoutingPathSegment]]], congestionThreshold: Double): MCTSSolver =
    new MCTSGlobalCongestionSolver02(graph, request, congestionThreshold)

  def negLogTransform(globalCongestion: BigDecimal)(currentCongestionOffset: BigDecimal, a: Double = 2D): Double = {
    val congTransform: Double = -math.log((currentCongestionOffset/globalCongestion).toDouble) * a/(math.log(globalCongestion.toDouble))
    math.max(Double.MinPositiveValue, math.min(1.0D, congTransform))
  }

  def doubleCongestionUpperBoundTransform(globalCongestion: BigDecimal)(resultGlobalCongestion: BigDecimal): Double = {
    val congTransform: Double = 1D + (-resultGlobalCongestion / globalCongestion).toDouble
    math.max(Double.MinPositiveValue, math.min(1.0D, congTransform))
  }
}

