package cse.fitzgero.sorouting.algorithm.local.selection.mcts

import cse.fitzgero.sorouting.algorithm.local.selection.mcts.Tag._
import cse.fitzgero.sorouting.model.path.SORoutingPathSegment
import cse.fitzgero.sorouting.model.roadnetwork.local.{LocalGraph, LocalODPair}

object MCTSHelpers {

  case class EvaluationTuple(id: SORoutingPathSegment.EdgeId, origFlow: Double, thisFlow: Double, origCost: Double, thisCost: Double)

  def allPathSegmentsIn(theseAlts: AlternatesSet, globalAlts: GlobalAlternates): Seq[SORoutingPathSegment] = {
    for {
      alt <- theseAlts
      original <- Tag.grabOriginalsAssociatedWith(alt, globalAlts).toList
      pathSeg <- original._2
    } yield pathSeg
  }

  def edgesAndFlowsIn(allSegments: Seq[SORoutingPathSegment]): Map[SORoutingPathSegment.EdgeId, Int] = {
    allSegments
      .groupBy { _.edgeId }
      .mapValues { _.size }
  }

  def costOffsetFromEdgesAndFlows(edgesAndFlows: Map[SORoutingPathSegment.EdgeId, Int], graph: LocalGraph): BigDecimal = (
    for {
      tuple <- edgesAndFlows
      edge <- graph.edgeById(tuple._1)
      origCost <- edge.attribute.linkCostFlow
      thisCost <- edge.attribute.costFlow(tuple._2)
    } yield {
      BigDecimal.decimal(thisCost - origCost)
    }).sum

  def evaluateCostOffset(theseAlts: AlternatesSet, globalAlts: GlobalAlternates, graph: LocalGraph): BigDecimal =
    costOffsetFromEdgesAndFlows(edgesAndFlowsIn(allPathSegmentsIn(theseAlts, globalAlts)), graph)


  def produceEvaluationTuples(theseAlts: AlternatesSet, globalAlts: GlobalAlternates, graph: LocalGraph): Iterable[EvaluationTuple] = {

    val allSegments: Seq[SORoutingPathSegment] = allPathSegmentsIn(theseAlts, globalAlts)
    val allEdges: Map[SORoutingPathSegment.EdgeId, Seq[SORoutingPathSegment]] = allSegments.groupBy { _.edgeId }

    val allTuples = for {
          edgeTuple <- allEdges
          edge <- graph.edgeById(edgeTuple._1)
          origFlow <- edge.attribute.flow
          origCost <- edge.attribute.linkCostFlow
          thisCost <- edge.attribute.costFlow(edgeTuple._2.size)
        } yield {
          val result = EvaluationTuple(edgeTuple._1, origFlow, origFlow + edgeTuple._2.size, origCost, thisCost)
          result
        }

    allTuples
  }

  type EvaluationFunction = (Iterable[EvaluationTuple]) => Double
  def meanCostDiff(congestionThreshold: Double): EvaluationFunction =
    (tuples: Iterable[EvaluationTuple]) => {
      val growth = tuples.map {
        tuple =>
          if (tuple.origCost == 0D) 0D
          else tuple.thisCost / tuple.origCost
      }
      val averagePercentGrowth: Double = growth.sum / tuples.size
      if (averagePercentGrowth <= congestionThreshold) 1D else 0D
    }
}
