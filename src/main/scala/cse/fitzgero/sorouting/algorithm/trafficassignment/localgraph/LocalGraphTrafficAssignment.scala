package cse.fitzgero.sorouting.algorithm.trafficassignment.localgraph

import cse.fitzgero.sorouting.roadnetwork.localgraph._
import cse.fitzgero.sorouting.roadnetwork.edge._
import cse.fitzgero.sorouting.roadnetwork.vertex._
import cse.fitzgero.sorouting.algorithm.shortestpath.sssp.localgraph.simplesssp._
import cse.fitzgero.sorouting.algorithm.trafficassignment._

abstract class LocalGraphTrafficAssignment extends TrafficAssignment[LocalGraph[VertexMATSim, EdgeMATSim], SimpleSSSP_ODPair]{
  def solve (graph: LocalGraph[VertexMATSim, EdgeMATSim], odPairs: Seq[SimpleSSSP_ODPair], terminationCriteria: TerminationCriteria): TrafficAssignmentResult
}