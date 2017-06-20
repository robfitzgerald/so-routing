package cse.fitzgero.sorouting.algorithm.trafficassignment

import cse.fitzgero.sorouting.roadnetwork.edge.{EdgeIdType, MacroscopicEdgeProperty}
import cse.fitzgero.sorouting.roadnetwork.vertex.CoordinateVertexProperty
import org.apache.spark.graphx._

/**
  * Gradient-based traffic assignment solver
  */
object TrafficAssignment {
  def solve(graph: Graph[CoordinateVertexProperty, MacroscopicEdgeProperty], odPairs: Seq[(VertexId, VertexId)]): Graph[CoordinateVertexProperty, MacroscopicEdgeProperty] = ???
  private def updateEdges(graph: Graph[CoordinateVertexProperty, MacroscopicEdgeProperty], paths: Seq[(VertexId, VertexId, List[EdgeIdType])]): Graph[CoordinateVertexProperty, MacroscopicEdgeProperty] = ???
}