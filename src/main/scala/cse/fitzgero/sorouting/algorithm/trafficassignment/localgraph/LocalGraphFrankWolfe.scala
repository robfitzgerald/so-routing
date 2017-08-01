package cse.fitzgero.sorouting.algorithm.trafficassignment.localgraph

import java.time.Instant

import cse.fitzgero.sorouting.algorithm.trafficassignment._
import cse.fitzgero.sorouting.roadnetwork.localgraph._
import cse.fitzgero.sorouting.algorithm.shortestpath.sssp.localgraph.simplesssp._
import cse.fitzgero.sorouting.roadnetwork.localgraph.edge._
import cse.fitzgero.sorouting.roadnetwork.localgraph.vertex._

import scala.annotation.tailrec
import scala.collection.parallel.ParSeq
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

//        println(s"_solve iteration $iter (total: ${previousGraph.edgeAttrs.map(_.allFlow).sum})")
//        println(s"${previousGraph.edgeAttrs.map(_.allFlow).mkString(" ")}")

        // all-or-nothing assignment
        // TODO: this should output a new set of flows, not add to the flows of previousGraph
        val oracleGraph = assignment(previousGraph, odPairs)

        val phi = Phi.linearFromIteration(iter)


        val currentGraph: LocalGraph[CoordinateVertexProperty, MacroscopicEdgeProperty] =
          previousGraph
          .edges
            // TODO: better UNION here please. don't like the assumption that id/edge ordering is consistent
          .map((id: EdgeId) => (id, previousGraph.edgeAttrOf(id).get)).zip(oracleGraph.edgeAttrs)
          .foldLeft(initialGraph)(
            (newGraph: LocalGraph[CoordinateVertexProperty, MacroscopicEdgeProperty],
                tuple: ((EdgeId, MacroscopicEdgeProperty), MacroscopicEdgeProperty)) => {
              val edgeIdToModify = tuple._1._1
              val edgePreviousAttr = tuple._1._2
              val edgePreviousFlow = edgePreviousAttr.flow
              val edgeCurrentFlow = tuple._2.flow
              newGraph
                .updateEdge(
                    edgeIdToModify,
                    edgePreviousAttr
                      .copy(flow = frankWolfeFlowCalculation(phi, edgePreviousFlow, edgeCurrentFlow)))
            })

        println(s"current graph at iteration $iter ")
        println(s"phi: ${phi.value} phiInverse: ${phi.inverse}")
        println(s"links used: ${currentGraph.edgeAttrs.map(_.allFlow).sum}")
        println(s"network cost: ${currentGraph.edgeAttrs.map(_.linkCostFlow).sum}")
        println(s"${currentGraph.edgeAttrs.map(_.allFlow).mkString(" ")}")

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

  /**
    * given a graph and a set of od pairs, we perform an "all-or-nothing" assignment based on the link costs of srcGraph
    * @param srcGraph graph to grab flow costs from, to copy, reset flow values, and add the path flows to
    * @param odPairs set of origin destination pairs
    * @return
    */
  def assignment(
    srcGraph: LocalGraph[CoordinateVertexProperty, MacroscopicEdgeProperty],
    odPairs: Seq[SimpleSSSP_ODPair]
  ): LocalGraph[CoordinateVertexProperty, MacroscopicEdgeProperty] = {
    val SSSP = SimpleSSSP[CoordinateVertexProperty, MacroscopicEdgeProperty]()

    val flowsToAdd: Map[EdgeId, Double] =
      odPairs
      .map(od => SSSP.shortestPath(srcGraph, od))
      .foldLeft(Map.empty[EdgeId, Double])((flows: Map[EdgeId, Double], odPath) => {
        odPath.path.zip(odPath.cost).foldLeft(flows)((updatedFlows, pathCostTuple) => {
          val eId = pathCostTuple._1
          if (updatedFlows.isDefinedAt(eId))
            updatedFlows.updated(eId, updatedFlows(eId) + 1D)
          else updatedFlows.updated(eId, 1D)
        })
      })

    val dstGraph =
      srcGraph
        .edgeKVPairs
        .foldLeft(srcGraph: LocalGraph[CoordinateVertexProperty, MacroscopicEdgeProperty])((g, eTup) => {
          g.updateEdge(eTup._1, eTup._2.copy(flow = 0D))
        })

    flowsToAdd.foldLeft(dstGraph)((updatedGraph, flowDelta) => {
      srcGraph.edgeAttrOf(flowDelta._1) match {
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
