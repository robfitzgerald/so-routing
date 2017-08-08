package cse.fitzgero.sorouting.algorithm.trafficassignment.localgraph

import java.time.Instant
import scala.collection.GenSeq

import cse.fitzgero.sorouting.algorithm.pathsearch.od.localgraph._
import cse.fitzgero.sorouting.algorithm.pathsearch.sssp.localgraphsimplesssp._
import cse.fitzgero.sorouting.algorithm.trafficassignment._
import cse.fitzgero.sorouting.roadnetwork.localgraph._

object LocalGraphFrankWolfe
  extends TrafficAssignment[LocalGraphMATSim, LocalGraphODPair] {

  val SSSP: LocalGraphSimpleSSSP[LocalGraphMATSim, VertexMATSim, EdgeMATSim] =
    LocalGraphSimpleSSSP[LocalGraphMATSim, VertexMATSim, EdgeMATSim]()

  /**
    * solve a traffic assignment for the given network state and set of origin/destination pairs
    * @param graph the network to solve on
    * @param odPairs the set of origin/destination pairs
    * @param terminationCriteria the way to determine convergence
    * @return a solution which contains the final graph estimation, or no solution
    */
  override def solve (
    graph: LocalGraphMATSim,
    odPairs: GenSeq[LocalGraphODPair],
    terminationCriteria: TerminationCriteria): TrafficAssignmentResult = {

    val startTime = Instant.now().toEpochMilli

    def _solve(previousGraph: LocalGraphMATSim, iteration: Int = 1): LocalGraphFWSolverResult = {
      val oracleGraph = generateOracleGraph(previousGraph, odPairs)
      val phi = Phi.linearFromIteration(iteration)
//      println("previousGraph")
//      println(s"${previousGraph.edgeAttrs.map(_.flow).mkString(" ")}")
//      println(s"${previousGraph.edgeAttrs.map(e => f"${e.linkCostFlow}%1.2f").mkString(" ")}")
//      println("oracleGraph")
//      println(s"${oracleGraph.edgeAttrs.map(_.flow).mkString(" ")}")
//      println(s"${oracleGraph.edgeAttrs.map(e => f"${e.linkCostFlow}%1.2f").mkString(" ")}")
      val currentGraph = calculateCurrentFlows(previousGraph, oracleGraph, phi)

      val stoppingConditionIsMet: Boolean =
      terminationCriteria
        .eval(TerminationData(startTime, iteration, relativeGap(currentGraph, oracleGraph)))

//      println(s"_solve at iteration $iteration with phi ${phi.value}")
      if (stoppingConditionIsMet) {
        val totalTime = Instant.now().toEpochMilli - startTime
        LocalGraphFWSolverResult(currentGraph, iteration, totalTime)
      }
      else {
        _solve(currentGraph, iteration + 1)
      }
    }

//    println(s"solve at iteration 0")
    _solve(graph)

  }

  /**
    * wipes any flow data from the graph, except any set from the current snapshot data
    * @param g the road network
    * @return the road network where network flows from traffic assignment equals zero
    */
  def initializeFlows(g: LocalGraphMATSim): LocalGraphMATSim = {
    val newEdges: GenSeq[EdgeMATSim] =
      g
      .edgeAttrs
      .map(_.copy(flowUpdate = 0D))
      .toSeq
    g.replaceEdgeList(newEdges)
  }

  /**
    * for each od pair, find it's shortest path, then update the graph flows with the number of vehicles assigned to each road segment
    * @param g the road network we will load flows onto
    * @param odPairs the set of origin/destination pairs to find paths between
    * @return the road network updated with the flows associated with this set of shortest paths
    */
  def generateOracleGraph(g: LocalGraphMATSim, odPairs: GenSeq[LocalGraphODPair]): LocalGraphMATSim = {
    val edgesToUpdate: GenSeq[EdgeMATSim] =
      odPairs
      .flatMap(od => {
        val path = SSSP.shortestPath(g, od)
//        println(path)
        path.path
      })
      .groupBy(identity)
      .map(edgeIdGrouped => {
        g.edgeAttrOf(edgeIdGrouped._1).get.copy(flowUpdate = edgeIdGrouped._2.size)
      })
      .toSeq
    initializeFlows(g).integrateEdgeList(edgesToUpdate)
  }

  /**
    * given the previous graph and the most recent all-or-nothing assignment, calculate a new flow value for each link based on a proportion
    * @param previousGraph either the initial graph or the graph at the ith - 1 iteration of the traffic assignment algorithm
    * @param oracle a graph with flows updated based on assigning all of the origin/destination pairs provided by the user
    * @param phi proportional argument used to determine how much of each source value to use
    * @return
    */
  def calculateCurrentFlows(previousGraph: LocalGraphMATSim, oracle: LocalGraphMATSim, phi: Phi): LocalGraphMATSim = {
    val edgesWithUpdatedFlows: GenSeq[EdgeMATSim] = previousGraph.edges.map(edgeId => {
      val thisAttr = previousGraph.edgeAttrOf(edgeId).get
      val flowPrevious = thisAttr.flow
      val flowOracle = oracle.edgeAttrOf(edgeId).get.flow
      thisAttr.copy(flowUpdate = calculateThisFlow(flowPrevious, flowOracle, phi))
    }).toSeq
    previousGraph.replaceEdgeList(edgesWithUpdatedFlows)
  }

  /**
    * calculate the next flows as a step toward the local minimum by a proportion of a real solution and an oracle solution which is outside of the solution space. taken from Modeling Transport 4th Ed., pg 398 in figure 11.2.3.1 step 4
    * @param flowPrevious a real (suboptimal) solution - the previous (ith - 1) algorithm step
    * @param flowOracle an oracle solution which we will use to construct a linear gradient
    * @param phi the descent step proportion (how far to proceed down the tangent between flowPrevious + flowAON)
    * @return
    */
  def calculateThisFlow(flowPrevious: Double, flowOracle: Double, phi: Phi): Double =
    (phi.inverse * flowPrevious) + (phi.value * flowOracle)

    /**
      * calculates the relative gap from the current graph to the aon graph, which as it goes to zero, identifies a minima (horiz. tangent line)
      * @param currentGraph the most recent estimation of the flow
      * @param allOrNothingGraph the artificial step beyond the direction of the nearest minima
      * @return a value in the range [0.0, 1.0]
      */
    def relativeGap(
      currentGraph: LocalGraphMATSim,
      allOrNothingGraph: LocalGraphMATSim
    ): Double = {
      case class RelGapData(currentCoef: Double, aonCoef: Double)
      val relGapData: RelGapData =
        allOrNothingGraph
        .edgeAttrs
        .map(aonEdge => {
          val currentEdge = currentGraph.edgeAttrOf(aonEdge.id).get
          RelGapData(
            currentEdge.linkCostFlow * currentEdge.allFlow,
            currentEdge.linkCostFlow * aonEdge.allFlow
          )
        }).reduce((a,b) => {
          RelGapData(a.currentCoef + b.currentCoef, a.aonCoef + b.aonCoef)
        })
      val result = math.abs((relGapData.currentCoef - relGapData.aonCoef) / relGapData.currentCoef)
      math.max(math.min(result, 1.0D), 0D)  // result has domain [0.0, 1.0]
    }
}