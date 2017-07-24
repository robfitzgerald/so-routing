package cse.fitzgero.sorouting.algorithm.shortestpath.mssp.graphx

import cse.fitzgero.sorouting.algorithm.shortestpath._
import cse.fitzgero.sorouting.roadnetwork.graphx.edge.EdgeIdType
import org.apache.spark.graphx.VertexId

abstract class GraphXODPath extends ODPath[VertexId, EdgeIdType] {
  override def srcVertex: VertexId
  override def dstVertex: VertexId
  override def path: List[EdgeIdType]
}
