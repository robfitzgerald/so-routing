package cse.fitzgero.sorouting.algorithm.trafficassignment

import java.time._

import scala.math.abs
import cse.fitzgero.sorouting.roadnetwork.graph.RoadNetwork
import cse.fitzgero.sorouting.algorithm.shortestpath._
import org.apache.spark.graphx.{EdgeRDD, Graph, VertexId}
import cse.fitzgero.sorouting.roadnetwork.edge.{EdgeIdType, MacroscopicEdgeProperty}

import scala.annotation.tailrec

object FrankWolfe extends TrafficAssignment {

  case class RelativeGapSummation(currentFlowTimesCost: Double, aonFlowTimesCost: Double)

  case class Phi (value: Double) {
    require(value <= 1 && value >= 0, s"Phi is only defined for values in the range [0,1], but found $value")
    val inverse: Double = 1 - value
  }

  abstract class FrankWolfeObjective extends Serializable {
    case class MapReduceAccumulator(value: Double)
  }

  object SystemOptimalObjective extends FrankWolfeObjective {
    def apply(g: RoadNetwork): Double = g.mapEdges(e => e.attr.flow * e.attr.linkCostFlow).edges.reduce((a, b) => a.copy(attr = a.attr + b.attr)).attr
  }

  /**
    * Solve an equilibrium traffic assignment, based on J. de D. Ortuzar and L. G. Willumsen, "Modeling Transport", 4th Ed, p398 11:2:3:1 The Frank-Wolfe Algorithm
    * @param initialGraph road network graph with no flows assigned though there may be flows stored as "Zero-values" to be treated as a lower-bounds
    * @param odPairs a collection of origin/destination tuples denoting vertices in the road network
    * @param terminationCriteria a rule for algorithm termination
    * @return results of this traffic assignment
    */
  override def solve(initialGraph: RoadNetwork, odPairs: ODPairs, terminationCriteria: TerminationCriteria): FWSolverResult = {
    val startTime = Instant.now().toEpochMilli

    /**
      * recursive inner function describing the Frank-Wolfe algorithm
      * @param previousGraph result of previous iteration
      * @param iter current iteration
      * @return results of this traffic assignment
      */
    @tailrec
    def _solve(previousGraph: RoadNetwork, iter: Int = 1): FWSolverResult = {

//      println(s"~~ _solve at iteration $iter - previousGraph")
//      previousGraph.edges.toLocalIterator.foreach(edge => println(s"${edge.attr.id} ${edge.attr.flow} ${edge.attr.cost.zeroValue} ${edge.attr.linkCostFlow}"))

      // all-or-nothing assignment
      val (oracleGraph, paths) = Assignment(previousGraph, odPairs, CostFlow())

//      println(s"~~ _solve at iteration $iter - oracleGraph")
//      oracleGraph.edges.toLocalIterator.foreach(edge => println(s"${edge.attr.id} ${edge.attr.flow} ${edge.attr.cost.zeroValue} ${edge.attr.linkCostFlow}"))

      // step size - should be based on our objective function
      val phi = Phi(2.0D/(iter + 2.0D))

      val updatedEdges: EdgeRDD[MacroscopicEdgeProperty] = previousGraph.edges.innerJoin(oracleGraph.edges)((_, _, currentEdge, oracleEdge) => {
        currentEdge.copy(
          flow = frankWolfeFlowCalculation(phi, currentEdge.flow, oracleEdge.flow)
        )
      })
      val currentGraph: RoadNetwork = Graph(previousGraph.vertices, updatedEdges)

//      println(s"~~ _solve at iteration $iter - currentGraph")
//      currentGraph.edges.toLocalIterator.foreach(edge => println(s"${edge.attr.id} ${edge.attr.flow} ${edge.attr.cost.zeroValue} ${edge.attr.linkCostFlow}"))

      val stoppingConditionIsMet: Boolean =
        evaluateStoppingCriteria(
          terminationCriteria,
          relativeGap(currentGraph, oracleGraph),  // call-by-name argument
          startTime,
          iter)

      if (stoppingConditionIsMet) {
        val totalTime = startTime - Instant.now().toEpochMilli
        FWSolverResult(paths, currentGraph, iter, totalTime)
      }
      else {
        _solve(currentGraph, iter + 1)
      }
    }
    _solve(initialGraph)
  }

  def evaluateStoppingCriteria(terminationCriteria: TerminationCriteria, relGap: => Double, startTime: Long, iter: Int): Boolean = terminationCriteria match {
    case RelativeGapTerminationCriteria(relGapThresh) => relGap > relGapThresh
    case IterationTerminationCriteria(iterThresh) => iter >= iterThresh
    case RunningTimeTerminationCriteria(timeThresh) => Instant.now().toEpochMilli - startTime > timeThresh
    case AllTerminationCriteria(relGapThresh, iterThresh, timeThresh) =>
      relGap > relGapThresh &&
      iter >= iterThresh &&
      Instant.now().toEpochMilli - startTime > timeThresh
  }

  def Assignment(graph: RoadNetwork, odPairs: ODPairs, assignmentType: CostMethod): (RoadNetwork, ODPaths) = {
    val pathsResult: ODPaths = GraphXShortestPaths.shortestPaths(graph, odPairs, assignmentType)
    val pathsToFlows: Map[String, Int] = pathsResult.flatMap(_.path).groupBy(identity).mapValues(_.size).map(identity)
    val newAssignment: RoadNetwork =
      graph
        .mapEdges(edge =>
          if (pathsToFlows.isDefinedAt(edge.attr.id)) edge.attr.copy(flow = pathsToFlows(edge.attr.id))
          else edge.attr.copy(flow = 0D)
        )
    (newAssignment, pathsResult)
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
    * @return relative gap value, based on J. de D. Ortuzar and L. G. Willumsen, "Modeling Transport", 4th Ed, p397 11:11:2:3 Solution Methods (Fig. 11.11)
    */
  def relativeGap(currentGraph: RoadNetwork, aonGraph: RoadNetwork): Double = {
    val sum: RelativeGapSummation = currentGraph.edges.innerJoin(aonGraph.edges)((_, _, currentEdge, aonEdge) => {
      RelativeGapSummation(
        currentEdge.flow * currentEdge.linkCostFlow,
        aonEdge.flow * aonEdge.linkCostFlow
      )
    }).reduce((e1, e2) => e1.copy(attr = RelativeGapSummation(e1.attr.currentFlowTimesCost + e2.attr.currentFlowTimesCost, e1.attr.aonFlowTimesCost + e2.attr.aonFlowTimesCost))).attr
    abs((sum.currentFlowTimesCost - sum.aonFlowTimesCost) / sum.currentFlowTimesCost)
  }
}