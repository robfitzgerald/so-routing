package cse.fitzgero.sorouting.algorithm.flowestimation.localgraph

import cse.fitzgero.sorouting.algorithm.pathsearch.od.localgraph.LocalGraphODPairByVertex
import cse.fitzgero.sorouting.roadnetwork.localgraph._
import cse.fitzgero.sorouting.roadnetwork.edge._
import cse.fitzgero.sorouting.roadnetwork.vertex._
import cse.fitzgero.sorouting.algorithm.pathsearch.sssp.localgraphsimplesssp._
import cse.fitzgero.sorouting.algorithm.flowestimation._

abstract class LocalGraphTrafficAssignment extends TrafficAssignment[LocalGraph[VertexMATSim, EdgeMATSim], LocalGraphODPairByVertex]{
  def solve (graph: LocalGraph[VertexMATSim, EdgeMATSim], odPairs: Seq[LocalGraphODPairByVertex], terminationCriteria: FWBounds): TrafficAssignmentResult
}