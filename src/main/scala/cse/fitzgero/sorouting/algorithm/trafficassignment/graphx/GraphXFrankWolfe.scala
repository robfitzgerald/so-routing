package cse.fitzgero.sorouting.algorithm.trafficassignment.graphx

import java.time._

import cse.fitzgero.sorouting.algorithm.pathsearch.mssp.graphx.simplemssp._
import cse.fitzgero.sorouting.algorithm.trafficassignment._
import cse.fitzgero.sorouting.roadnetwork.edge._
import cse.fitzgero.sorouting.roadnetwork.graphx.{GraphXEdge, GraphxRoadNetwork}
import cse.fitzgero.sorouting.roadnetwork.localgraph.EdgeId
import org.apache.spark.graphx.{EdgeRDD, Graph}

import scala.annotation.tailrec
import scala.collection.GenSeq
import scala.math.abs

object GraphXFrankWolfe extends GraphXTrafficAssignment {

  // TODO: switch to DataFrames or GenSeqs for odPairs/odPaths
  override def solve(graph: GraphxRoadNetwork, odPairs: GenSeq[SimpleMSSP_ODPair], terminationCriteria: TerminationCriteria): TrafficAssignmentResult = ???

  case class RelativeGapSummation(currentFlowTimesCost: Double, aonFlowTimesCost: Double)

  abstract class FrankWolfeObjective extends Serializable {
    case class MapReduceAccumulator(value: Double)
  }

  object SystemOptimalObjective extends FrankWolfeObjective {
    def apply(g: GraphxRoadNetwork): Double = g.mapEdges(e => e.attr.flow * e.attr.linkCostFlow).edges.reduce((a, b) => a.copy(attr = a.attr + b.attr)).attr
  }

  /**
    * Solve an equilibrium traffic assignment, based on J. de D. Ortuzar and L. G. Willumsen, "Modeling Transport", 4th Ed, p398 11:2:3:1 The Frank-Wolfe Algorithm
    * @param initialGraph road network graph with no flows assigned though there may be flows stored as "Zero-values" to be treated as a lower-bounds
    * @param odPairs a collection of origin/destination tuples denoting vertices in the road network
    * @param terminationCriteria a rule for algorithm termination
    * @return results of this traffic assignment
    */
  override def solve(initialGraph: GraphxRoadNetwork, odPairs: ODPairs, terminationCriteria: TerminationCriteria): GraphXFWSolverResult = {
    if (odPairs.isEmpty)
      GraphXFWSolverResult(Nil,  initialGraph, 0, 0)
    else {
      val startTime = Instant.now().toEpochMilli

      /**
        * recursive inner function describing the Frank-Wolfe algorithm
        * @param previousGraph result of previous iteration
        * @param iter current iteration
        * @return results of this traffic assignment
        */
      @tailrec
      def _solve(previousGraph: GraphxRoadNetwork, iter: Int = 1): GraphXFWSolverResult = {

        // all-or-nothing assignment
        val (oracleGraph, paths) = Assignment(previousGraph, odPairs, CostFlow())

        // TODO: step size - should be based on our objective function
        val phi = Phi(2.0D/(iter + 2.0D))

        val updatedEdges: EdgeRDD[GraphXEdge] = previousGraph.edges.innerJoin(oracleGraph.edges)((_, _, currentEdge, oracleEdge) => {
          currentEdge.copy(
            flowUpdate = frankWolfeFlowCalculation(phi, currentEdge.flow, oracleEdge.flow)
          )
        })
        val currentGraph: GraphxRoadNetwork = Graph(previousGraph.vertices, updatedEdges)

        val stoppingConditionIsMet: Boolean =
          terminationCriteria
            .eval(TerminationData(
              startTime,
              iter,
              relativeGap(currentGraph, oracleGraph)
            ))

        if (stoppingConditionIsMet) {
          val totalTime = Instant.now().toEpochMilli - startTime
          GraphXFWSolverResult(paths, currentGraph, iter, totalTime)
        }
        else {
          _solve(currentGraph, iter + 1)
        }
      }
      _solve(initialGraph)
    }
  }

  def Assignment(graph: GraphxRoadNetwork, odPairs: ODPairs, assignmentType: CostMethod): (GraphxRoadNetwork, ODPaths) = {
    SimpleMSSP.setCostMethod(assignmentType)
    val pathsResult: ODPaths = SimpleMSSP.shortestPaths(graph, odPairs)
    val pathsToFlows: Map[EdgeId, Int] = pathsResult.flatMap(_.path).groupBy(identity).mapValues(_.size).map(identity)
    val newAssignment: GraphxRoadNetwork =
      graph
        .mapEdges(edge =>
          if (pathsToFlows.isDefinedAt(edge.attr.id)) edge.attr.copy(flowUpdate = pathsToFlows(edge.attr.id))
          else edge.attr.copy(flowUpdate = 0D)
        )
    (newAssignment, pathsResult)
  }

  /**
    * calculates the relative gap value based on the two available graphs
    * @param currentGraph expected to be the graph constructed from the current Frank-Wolfe iteration
    * @param aonGraph expected to be the same graph as "currentGraph' but with all-or-nothing flow assignments
    * @return relative gap value, based on J. de D. Ortuzar and L. G. Willumsen, "Modeling Transport", 4th Ed, p397 11:11:2:3 Solution Methods (Fig. 11.11)
    */
  def relativeGap(currentGraph: GraphxRoadNetwork, aonGraph: GraphxRoadNetwork): Double = {
    val sum: RelativeGapSummation = currentGraph.edges.innerJoin(aonGraph.edges)((_, _, currentEdge, aonEdge) => {
      RelativeGapSummation(
        currentEdge.allFlow * currentEdge.linkCostFlow,
        aonEdge.allFlow * aonEdge.linkCostFlow
      )
    }).reduce((e1, e2) => e1.copy(attr = RelativeGapSummation(e1.attr.currentFlowTimesCost + e2.attr.currentFlowTimesCost, e1.attr.aonFlowTimesCost + e2.attr.aonFlowTimesCost))).attr
    val result = abs((sum.currentFlowTimesCost - sum.aonFlowTimesCost) / sum.currentFlowTimesCost)
    math.max(math.min(result, 1.0D), 0.0D) // result has domain [0.0, 1.0]
  }
}