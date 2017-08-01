package cse.fitzgero.sorouting.algorithm.trafficassignment.localgraph

import cse.fitzgero.sorouting.algorithm.shortestpath.sssp.localgraph.simplesssp._
import cse.fitzgero.sorouting.algorithm.trafficassignment._
import cse.fitzgero.sorouting.roadnetwork.localgraph._

import scala.collection.GenSeq

/**
  * invariant: all Edge.flow values should be set to zero
  */
object LocalGraphFrankWolfe2
  extends TrafficAssignment[LocalGraphMATSim, SimpleSSSP_ODPair] {
  override def solve (
    graph: LocalGraphMATSim,
    odPairs: Seq[SimpleSSSP_ODPair],
    terminationCriteria: TerminationCriteria): TrafficAssignmentResult = ???

  /**
    * wipes any flow data from the graph, except any set from the current snapshot data
    * @param g the road network
    * @return the road network where network flows from traffic assignment equals zero
    */
  def initializeFlows(g: LocalGraphMATSim): LocalGraphMATSim = {
    val newEdges: GenSeq[EdgeMATSim] =
      g
      .edgeAttrs
      .map(_.copy(flow = 0D))
      .toSeq
    g.replaceEdgeList(newEdges)
  }

  /**
    * for each od pair, find it's shortest path, then update the graph flows with the number of vehicles assigned to each road segment
    * @param g the road network we will load flows onto
    * @param odPairs the set of origin/destination pairs to find paths between
    * @return the road network updated with the flows associated with this set of shortest paths
    */
  def loadAllOrNothing(g: LocalGraphMATSim, odPairs: GenSeq[SimpleSSSP_ODPair]): LocalGraphMATSim = {
    val SSSP = SimpleSSSP[LocalGraphMATSim, VertexMATSim, EdgeMATSim]()
    val edgesToUpdate: GenSeq[EdgeMATSim] =
      odPairs
      .flatMap(od => SSSP.shortestPath(g, od).path)
      .groupBy(identity)
      .map(edgeIdGrouped => {
        g.edgeAttrOf(edgeIdGrouped._1).get.copy(flow = edgeIdGrouped._2.size)
      })
      .toSeq
    g.integrateEdgeList(edgesToUpdate)
  }


}
