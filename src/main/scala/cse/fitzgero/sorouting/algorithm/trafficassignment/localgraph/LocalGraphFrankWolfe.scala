package cse.fitzgero.sorouting.algorithm.trafficassignment.localgraph

import java.time.Instant

import cse.fitzgero.sorouting.algorithm.trafficassignment._
import cse.fitzgero.sorouting.roadnetwork.localgraph._
import cse.fitzgero.sorouting.algorithm.shortestpath.sssp.localgraph.simplesssp._
import cse.fitzgero.sorouting.roadnetwork.localgraph.edge._
import cse.fitzgero.sorouting.roadnetwork.localgraph.vertex._

import scala.annotation.tailrec
import scala.math.abs

object LocalGraphFrankWolfe extends TrafficAssignment[LocalGraph[CoordinateVertexProperty,MacroscopicEdgeProperty], SimpleSSSP_ODPair] {

  override def solve(
    initialGraph: LocalGraph[CoordinateVertexProperty, MacroscopicEdgeProperty],
    odPairs: Seq[SimpleSSSP_ODPair],
    terminationCriteria: TerminationCriteria): TrafficAssignmentResult = {
    if (odPairs.isEmpty)
      NoTrafficAssignmentSolution()
    else {
      val startTime = Instant.now().toEpochMilli

      /**
        * recursive inner function describing the Frank-Wolfe algorithm
        *
        * @param previousGraph result of previous iteration
        * @param iter          current iteration
        * @return results of this traffic assignment
        */
      @tailrec
      def _solve(
        previousGraph: LocalGraph[CoordinateVertexProperty, MacroscopicEdgeProperty],
        iter: Int = 1): LocalGraphFWSolverResult = {

        // all-or-nothing assignment
        val oracleGraph = assignment(previousGraph, odPairs)

        // TODO: step size - should be based on our objective function
        val phi = Phi(2.0D / (iter + 2.0D))

        val currentGraph: LocalGraph[CoordinateVertexProperty, MacroscopicEdgeProperty] =
        previousGraph
          .edges
          .map((id: EdgeId) => (id, previousGraph.edgeAttrOf(id).get))
          .zip(oracleGraph.edgeAttrs)
          .foldLeft(previousGraph)(
            (newGraph: LocalGraph[CoordinateVertexProperty, MacroscopicEdgeProperty],
              tuple: ((EdgeId, MacroscopicEdgeProperty), MacroscopicEdgeProperty)) => {
              newGraph.updateEdge(tuple._1._1, tuple._1._2.copy(flow = tuple._1._2.flow + tuple._2.flow))
            })

        val stoppingConditionIsMet: Boolean =
          terminationCriteria
            .eval(TerminationData(
              startTime,
              iter,
              relativeGap(currentGraph, oracleGraph)
            ))

        if (stoppingConditionIsMet) {
          val totalTime = Instant.now().toEpochMilli - startTime
          LocalGraphFWSolverResult(currentGraph, iter, totalTime)
        }
        else {
          _solve(currentGraph, iter + 1)
        }
      }

      _solve(initialGraph)
    }
  }

  def assignment(
    graph: LocalGraph[CoordinateVertexProperty, MacroscopicEdgeProperty],
    odPairs: Seq[SimpleSSSP_ODPair]
  ): LocalGraph[CoordinateVertexProperty, MacroscopicEdgeProperty] = {
    val SSSP = SimpleSSSP[CoordinateVertexProperty, MacroscopicEdgeProperty]()
    val flowsToAdd: Map[EdgeId, Double] = odPairs
      .map(od => SSSP.shortestPath(graph, od))
      .foldLeft(Map.empty[EdgeId, Double])((flows: Map[EdgeId, Double], odPath) => {
        odPath.path.zip(odPath.cost).foldLeft(flows)((updatedFlows, pathCostTuple) => {
          val eId = pathCostTuple._1
          if (updatedFlows.isDefinedAt(eId))
            updatedFlows.updated(eId, updatedFlows(eId) + 1D)
          else updatedFlows.updated(eId, 1D)
        })
      })
    flowsToAdd.foldLeft(graph)((updatedGraph, flowDelta) => {
      graph.edgeAttrOf(flowDelta._1) match {
        case Some(edgeAttr: MacroscopicEdgeProperty) =>
          updatedGraph.updateEdge(flowDelta._1, edgeAttr.copy(flow = edgeAttr.flow + flowDelta._2))
        case None => updatedGraph
      }
    })
  }

  /**
    * calculates the relative gap from the current graph to the aon graph, which as it goes to zero, identifies a minima (horiz. tangent line)
    * @param currentGraph the most recent estimation of the flow
    * @param allOrNothingGraph the artificial step beyond the direction of the nearest minima
    * @return a value in the range [0.0, 1.0]
    */
  def relativeGap(
    currentGraph: LocalGraph[CoordinateVertexProperty, MacroscopicEdgeProperty],
    allOrNothingGraph: LocalGraph[CoordinateVertexProperty, MacroscopicEdgeProperty]
  ): Double = {
    val (currentFlowTimesCost: Double, aonFlowTimesCost: Double) =
      currentGraph
      .edgeAttrs
      .zip(allOrNothingGraph.edgeAttrs)
      .map(tuple => (tuple._1.allFlow * tuple._1.linkCostFlow, tuple._2.allFlow * tuple._2.linkCostFlow))
      .reduce((a,b) => (a._1 + b._1, a._2 + b._2))
    val result = abs((currentFlowTimesCost - aonFlowTimesCost) / currentFlowTimesCost)
    math.max(math.min(result, 1.0D), 0D)  // result has domain [0.0, 1.0]
  }
}
