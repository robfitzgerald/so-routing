package cse.fitzgero.sorouting.algorithm.trafficassignment.localgraph

import cse.fitzgero.sorouting.roadnetwork.localgraph._
import cse.fitzgero.sorouting.roadnetwork.localgraph.edge._
import cse.fitzgero.sorouting.roadnetwork.localgraph.vertex._
import cse.fitzgero.sorouting.algorithm.shortestpath.sssp.localgraph.simplesssp._
import cse.fitzgero.sorouting.algorithm.trafficassignment._

abstract class LocalGraphTrafficAssignment extends TrafficAssignment[LocalGraph[CoordinateVertexProperty, MacroscopicEdgeProperty], SimpleSSSP_ODPair]{
  def solve (graph: LocalGraph[CoordinateVertexProperty, MacroscopicEdgeProperty], odPairs: Seq[SimpleSSSP_ODPair], terminationCriteria: TerminationCriteria): TrafficAssignmentResult
}