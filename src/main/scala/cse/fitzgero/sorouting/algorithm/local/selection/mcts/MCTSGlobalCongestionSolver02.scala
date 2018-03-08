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

  // these are the global min and max congestion.
  val bounds = MCTSHelpers.graphMinMaxCongestion(graph)

//  val (freeFlowCongestionSum: BigDecimal, unassignedCongestionSum: BigDecimal, freeFlowCongestionProduct: BigDecimal, unassignedCongestionProduct: BigDecimal) = (for {
//      edge <- graph.edges
//      freeFlowCost <- edge._2.attribute.freeFlowCostFlow
//      cost <- edge._2.attribute.linkCostFlow
//    } yield {
//      (BigDecimal.decimal(freeFlowCost), BigDecimal.decimal(freeFlowCost), BigDecimal.decimal(cost), BigDecimal.decimal(cost))
//    }).reduce {
//      (a, b) =>
//        (a._1 + b._1, a._2 + b._2, valueOrOne(a._3) * valueOrOne(b._3), valueOrOne(a._4) * valueOrOne(b._4))
//    }

//  def valueOrOne(n: BigDecimal): BigDecimal =
//    if (n <= BigDecimal.decimal(1)) BigDecimal.decimal(1) else n

//  val globalCongestionNormalized: BigDecimal = if ()unassignedCongestionSum - freeFlowCongestionSum
//  val globalCongestionSumNormalized: BigDecimal = unassignedCongestionSum - freeFlowCongestionSum
//  val globalCongestionProductNormalized: BigDecimal = if (freeFlowCongestionProduct == BigDecimal.decimal(0)) BigDecimal.decimal(1) else unassignedCongestionProduct - freeFlowCongestionProduct
//  val globalCongestionProductNormalized02: BigDecimal = if (globalCongestionSumNormalized <= BigDecimal.decimal(1)) BigDecimal.decimal(1) else globalCongestionSumNormalized
//  val sqrtGlobalCongestion: BigDecimal = BigDecimal.decimal(math.sqrt(unassignedCongestionSum.toDouble))

//  val transform: (BigDecimal) => Double = MCTSGlobalCongestionSolver02.doubleCongestionUpperBoundTransform(unassignedCongestionSum)

  override def evaluate(state: AlternatesSet): Double = {

//    val costOffsetSum = MCTSHelpers.evaluateCostOffsetBySum(state, globalAlternates, graph)
//    val costOffsetProduct = MCTSHelpers.evaluateCostOffsetByProduct(state, globalAlternates, graph)
//    val currentCost = MCTSHelpers.evaluateCostOfCurrentState(state, globalAlternates, graph)
//    val currentCostNormalized = MCTSHelpers.multiplicativeIdentity(currentCost / bounds.min)
//    val maxNormalized = MCTSHelpers.multiplicativeIdentity(bounds.max / bounds.min)
//    val ratio = currentCostNormalized / maxNormalized
//    val percent = ratio * (bounds.max / bounds.min)
//    val percent = bounds.min / costPerMaxCongestion
//    val reward: Double = 1D - percent.toDouble
    val reward: Double = MCTSHelpers.evaluateRewardSumForAll(state, globalAlternates, graph) match {
      case None => 0D
      case Some(r) => r
    }
//    val reward: Double = MCTSHelpers.evaluateRewardSumForChanged(state, globalAlternates, graph) match {
//      case None => 0D
//      case Some(r) => r
//    }
//    val reward: Double = MCTSHelpers.evaluateNegLogRewardSumForChanged(state, globalAlternates, graph) match {
//      case None => 0D
//      case Some(r) => r
//    }

//    val resultGlobalCongestionNormalized: BigDecimal = costOffset + globalCongestionNormalized
//    val resultGlobalCongestion: BigDecimal = costOffset + globalCongestion
//    val difference: BigDecimal = resultGlobalCongestion - globalCongestionNormalized
//    val difference: BigDecimal = if (resultGlobalCongestion > globalCongestion) resultGlobalCongestion - globalCongestion else globalCongestion - resultGlobalCongestion
//    val percentDifference: Double = (difference / globalCongestionNormalized).toDouble
//    val reward: Double = transform(resultGlobalCongestionNormalized)
//    val reward: Double = MCTSGlobalCongestionSolver02.negLogTransform(freeFlowCongestionProduct)(costOffsetProduct * unassignedCongestionProduct)
//    val reward: Double = MCTSGlobalCongestionSolver02.oneOverCongestion(globalCongestionProductNormalized * costOffsetProduct)
//    val reward: Double = MCTSGlobalCongestionSolver02.oneOverCongestion(costOffsetProduct)
//    val reward: Double = MCTSGlobalCongestionSolver02.oneOverNormalizedCongestion(globalCongestionSumNormalized, costOffsetProduct)
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
    val ratio: BigDecimal = currentCongestionOffset/globalCongestion
    val ratioDouble: Double = ratio.toDouble
    val leftTerm: Double = -math.log(ratioDouble)
    val congTransform: Double = leftTerm
    //    val rightTerm: Double = a / (math.log(globalCongestion.toDouble))
//    val congTransform: Double = -math.log((currentCongestionOffset/globalCongestion).toDouble) * a/(math.log(globalCongestion.toDouble))
    val result = if (ratioDouble == 0D) 1D else math.max(Double.MinPositiveValue, math.min(1.0D, congTransform))
    result
  }

  def doubleCongestionUpperBoundTransform(globalCongestion: BigDecimal)(resultGlobalCongestion: BigDecimal): Double = {
    val congTransform: Double = 1D + (-resultGlobalCongestion / globalCongestion).toDouble
    math.max(Double.MinPositiveValue, math.min(1.0D, congTransform))
  }

  def oneOverNormalizedCongestion(globalCongestionNormalized: BigDecimal, costOffset: BigDecimal): Double = {
    (1 / (globalCongestionNormalized + costOffset)).toDouble
  }

  def oneOverCongestion(congestion: BigDecimal): Double = (1 / congestion).toDouble
}

