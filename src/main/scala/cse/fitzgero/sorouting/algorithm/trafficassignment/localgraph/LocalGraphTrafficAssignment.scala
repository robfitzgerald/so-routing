package cse.fitzgero.sorouting.algorithm.trafficassignment.localgraph

import cse.fitzgero.sorouting.algorithm.pathsearch.od.localgraph.LocalGraphODPair
import cse.fitzgero.sorouting.roadnetwork.localgraph._
import cse.fitzgero.sorouting.roadnetwork.edge._
import cse.fitzgero.sorouting.roadnetwork.vertex._
import cse.fitzgero.sorouting.algorithm.pathsearch.sssp.localgraphsimplesssp._
import cse.fitzgero.sorouting.algorithm.trafficassignment._

abstract class LocalGraphTrafficAssignment extends TrafficAssignment[LocalGraph[VertexMATSim, EdgeMATSim], LocalGraphODPair]{
  def solve (graph: LocalGraph[VertexMATSim, EdgeMATSim], odPairs: Seq[LocalGraphODPair], terminationCriteria: TerminationCriteria): TrafficAssignmentResult
}