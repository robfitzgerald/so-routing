package cse.fitzgero.sorouting.algorithm.local.selection.mcts

import cse.fitzgero.sorouting.algorithm.local.selection.mcts.Tag._
import cse.fitzgero.sorouting.model.path.SORoutingPathSegment
import cse.fitzgero.sorouting.model.roadnetwork.local.{LocalGraph, LocalODPair}

object AltPathSelection {

  case class EvaluationTuple(id: SORoutingPathSegment.EdgeId, origFlow: Double, thisFlow: Double, origCost: Double, thisCost: Double)

  def produceEvaluationTuples(theseAlts: AlternatesSet, globalAlts: GlobalAlternates, graph: LocalGraph): Iterable[EvaluationTuple] = {

    val allSegments: Seq[SORoutingPathSegment] = for {
      alt <- theseAlts
      original <- Tag.grabOriginalsAssociatedWith(alt, globalAlts).toList
      pathSeg <- original._2
    } yield pathSeg

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
