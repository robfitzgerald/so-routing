package cse.fitzgero.sorouting.algorithm.trafficassignment

import java.time._
import scala.math.abs

import cse.fitzgero.sorouting.algorithm.shortestpath.Path
import cse.fitzgero.sorouting.roadnetwork.graph.RoadNetwork
import cse.fitzgero.sorouting.algorithm.shortestpath._
import org.apache.spark.graphx.VertexId


import cse.fitzgero.sorouting.roadnetwork.edge.EdgeIdType

object FrankWolfe extends TrafficAssignment {

  case class RelativeGapSummation(currentFlowTimesCost: Double, aonFlowTimesCost: Double)

  class Phi(val value: Double) {
    require(value <= 1 && value >= 0, s"Phi is only defined for values in the range [0,1], but found $value")
    val inverse: Double = 1 - value
  }

  object Phi {
    def apply(value: Double): Phi = new Phi(value)
  }

  override def solve(initialGraph: RoadNetwork, odPairs: Seq[(VertexId, VertexId)], terminationCriteria: TerminationCriteria): Seq[Path] = {
    val startTime = Instant.now().toEpochMilli
    val AONGraph: RoadNetwork = Assignment(initialGraph, odPairs, AONFlow())
    val firstAssignment: RoadNetwork = Assignment(initialGraph, odPairs, CostFlow())

    def _solve(previousGraph: RoadNetwork, currentGraph: RoadNetwork, phi: Phi, iter: Int = 1): (RoadNetwork, Seq[Path]) = {

      // TODO: 2. Build the set of minimum cost trees with the current costs

      // TODO: 3. Load the whole of the matrix T of these trees all-or-nothing, obtaining a set of auxiliary flows F_a

      // TODO: 4. calculate current flows (inner join and apply frankWolfeFlowCalculation, choosing Phi such that the value of the objective function is minimized

      // TODO: 5. calculate a new set of current link costs based on the flows you just set;


      // check our termination criteria; if not met, recurse
      val stop: Boolean = terminationCriteria match {
        case RelativeGapTerminationCriteria(relGapThresh) => relativeGap(currentGraph, currentGraph) > relGapThresh
        case IterationTerminationCriteria(iterThresh) => iter >= iterThresh
        case RunningTimeTerminationCriteria(timeThresh) => Instant.now().toEpochMilli - startTime > timeThresh
      }

      if (stop)
        (currentGraph, Seq.empty[List[EdgeIdType]])
      else {
        val nextPhi: Phi = phi
        _solve(previousGraph, currentGraph, nextPhi, iter + 1)
      }
    }

    _solve(initialGraph, firstAssignment, Phi(1.0D))._2
  }


  def Assignment(graph: RoadNetwork, odPairs: Seq[(VertexId, VertexId)], assignmentType: CostMethod): RoadNetwork = {
    val pathsToFlows: Map[String, Int] = GraphXShortestPaths.shortestPaths(graph, odPairs, assignmentType).flatMap(_._3).groupBy(identity).mapValues(_.size).map(identity)
    graph.mapEdges(edge => if (pathsToFlows.isDefinedAt(edge.attr.id)) edge.attr.copy(flow = edge.attr.flow + pathsToFlows(edge.attr.id)) else edge.attr)
  }

  /**
    * used to find the flow values for the current phase of Frank-Wolfe
    *
    * @param phi      a phi value with it's inverse (1 - phi) pre-calculated, for performance
    * @param linkFlow this link's flow of the previous FW iteration
    * @param aonFlow  this link's flow in the All-Or-Nothing assignment
    * @return the flow value for the next step
    */
  def frankWolfeFlowCalculation(phi: Phi, linkFlow: Double, aonFlow: Double): Double = (phi.inverse * linkFlow) + (phi.value * aonFlow)

  /**
    * calculates the relative gap value based on the two available graphs
    * @param currentGraph expected to be the graph constructed from the current Frank-Wolfe iteration
    * @param aonGraph expected to be the same graph as "currentGraph' but with all-or-nothing flow assignments
    * @return relative gap value, taken from J. de D. Ortuzar and L. G. Willumsen, "Modeling Transport", 4th Ed, p397 11:11:2:3 Solution Methods (Fig. 11.11)
    */
  def relativeGap(currentGraph: RoadNetwork, aonGraph: RoadNetwork): Double = {
    val sum: RelativeGapSummation = currentGraph.edges.innerJoin(aonGraph.edges)((_, _, currentEdge, aonEdge) => {
      RelativeGapSummation(
        currentEdge.flow * currentEdge.linkCostFlow,
        aonEdge.flow * currentEdge.linkCostFlow
      )
    }).reduce((e1, e2) => e1.copy(attr = RelativeGapSummation(e1.attr.currentFlowTimesCost + e2.attr.currentFlowTimesCost, e1.attr.aonFlowTimesCost + e2.attr.aonFlowTimesCost))).attr
    abs((sum.currentFlowTimesCost - sum.aonFlowTimesCost) / sum.currentFlowTimesCost)
  }
}