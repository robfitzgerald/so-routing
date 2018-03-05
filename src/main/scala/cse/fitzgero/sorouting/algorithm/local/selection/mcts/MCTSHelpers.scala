package cse.fitzgero.sorouting.algorithm.local.selection.mcts

import scala.collection.{GenIterable, GenMap}

import cse.fitzgero.sorouting.algorithm.local.selection.mcts.Tag._
import cse.fitzgero.sorouting.model.path.SORoutingPathSegment
import cse.fitzgero.sorouting.model.roadnetwork.local.{LocalEdge, LocalGraph, LocalGraphOps, LocalODPair}

object MCTSHelpers {

  case class EvaluationTuple(id: SORoutingPathSegment.EdgeId, origFlow: Double, thisFlow: Double, origCost: Double, thisCost: Double)

  case class GraphMinMaxCongestion(min: BigDecimal, max: BigDecimal)

  def multiplicativeIdentity(n: BigDecimal): BigDecimal = if (n <= BigDecimal.decimal(1)) BigDecimal.decimal(1) else n

  def graphMinMaxCongestion(graph: LocalGraph): GraphMinMaxCongestion =
    (for {
      edge <- graph.edges.values
      freeFlowCost <- edge.attribute.freeFlowCostFlow
      capacityCost <- edge.attribute.capacityCostFlow
    } yield {
      GraphMinMaxCongestion(BigDecimal.decimal(freeFlowCost),BigDecimal.decimal(capacityCost))
    }).reduce {
      (a, b) => GraphMinMaxCongestion(multiplicativeIdentity(a.min) * multiplicativeIdentity(b.min), multiplicativeIdentity(a.max) * multiplicativeIdentity(b.max))
    }

  /**
    * evaluates the current congestion as a reward function from [0,1]
    * @param edge
    * @return
    */
  def rewardOf(edge: LocalEdge): Option[Double] = {
    for {
      cost <- edge.attribute.linkCostFlow
      min <- edge.attribute.freeFlowCostFlow
      max <- edge.attribute.capacityCostFlow
      if min != max
    } yield {
//      if (cost<min) 1.0D - shouldn't be possible yo. but, cost can exceed max, so < 0.0D is still possible (though not desired)
      math.max(0.0D, 1D-(cost-min)/(max-min))
    }
  }

  /**
    * 20180305 - attempting based on new capacityCostFlow evaluation, and bounds [0,1] from rewardOf(LocalEdge)
    * @param theseAlts the tags of the selected alternate paths to evaluate a reward for
    * @param globalAlts the collection of tags mapped to edge sets
    * @param graph the current road network state
    * @return a reward, in the range [0,1]
    */
  def evaluateRewardSumForAll(theseAlts: AlternatesSet, globalAlts: GlobalAlternates, graph: LocalGraph): Option[Double] = {
    val edgesAndFlows = edgesAndFlowsIn(allPathSegmentsIn(theseAlts, globalAlts))
    val updatedGraph = LocalGraphOps.updateGraph(edgesAndFlows, graph)
    evaluateRewardSum(updatedGraph.edges)
  }

  def evaluateRewardSumForChanged(theseAlts: AlternatesSet, globalAlts: GlobalAlternates, graph: LocalGraph): Option[Double] = {
    val edgesAndFlows = edgesAndFlowsIn(allPathSegmentsIn(theseAlts, globalAlts))
    val updatedGraph = LocalGraphOps.updateGraph(edgesAndFlows, graph)
    val lookup = edgesAndFlows.keySet
    val edgesToEvaluate = updatedGraph.edges.filter{ e => lookup(e._1) }
    evaluateRewardSum(edgesToEvaluate)
  }

  def evaluateRewardSum(edges: GenMap[String, LocalEdge]): Option[Double] =
    (for {
      e <- edges
      reward <- rewardOf(e._2)
    } yield {
      (reward, 1)
    }).toList match {
      case Nil => None
      case xs: List[(Double, Int)] =>
        val result = xs.reduce {
          (a,b) =>
            (a._1 + b._1, a._2 + b._2)
        }
        Some(result._1 / result._2)
    }

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

  def costDifferencePerLink(edgesAndFlows: Map[SORoutingPathSegment.EdgeId, Int], graph: LocalGraph, op: (BigDecimal, BigDecimal) => BigDecimal): Iterable[BigDecimal] =
    for {
      tuple <- edgesAndFlows
      edge <- graph.edgeById(tuple._1)
      origCost <- edge.attribute.linkCostFlow
      thisCost <- edge.attribute.costFlow(tuple._2)
    } yield {
      op(thisCost, origCost)
    }

  def costPerLink(edgesAndFlows: Map[SORoutingPathSegment.EdgeId, Int], graph: LocalGraph): GenIterable[BigDecimal] = {
    for {
      edge <- graph.edges.values
    } yield {
      if (edgesAndFlows.isDefinedAt(edge.id)) {
        edge.attribute.costFlow(edgesAndFlows(edge.id)) map { n => multiplicativeIdentity(BigDecimal.decimal(n)) }
      } else {
        edge.attribute.linkCostFlow map { n => multiplicativeIdentity(BigDecimal.decimal(n)) }
      }
    }
  }.flatten

  def evaluateCostOfCurrentState(theseAlts: AlternatesSet, globalAlts: GlobalAlternates, graph: LocalGraph): BigDecimal = {
    costPerLink(edgesAndFlowsIn(allPathSegmentsIn(theseAlts, globalAlts)), graph).product
  }

  def evaluateCostOffsetByProduct(theseAlts: AlternatesSet, globalAlts: GlobalAlternates, graph: LocalGraph): BigDecimal =
  costDifferencePerLink(edgesAndFlowsIn(allPathSegmentsIn(theseAlts, globalAlts)), graph, (a,b) => if (b == BigDecimal.decimal(0) || a <= b) BigDecimal.decimal(1) else a / b).product

  def evaluateCostOffsetBySum(theseAlts: AlternatesSet, globalAlts: GlobalAlternates, graph: LocalGraph): BigDecimal =
    costDifferencePerLink(edgesAndFlowsIn(allPathSegmentsIn(theseAlts, globalAlts)), graph, (a,b) => a-b).sum

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
