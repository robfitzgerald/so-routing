package cse.fitzgero.sorouting.algorithm.mssp.graphx

import cse.fitzgero.sorouting.algorithm.mssp.ODPath
import cse.fitzgero.sorouting.roadnetwork.edge.EdgeIdType
import org.apache.spark.graphx.VertexId

abstract class GraphXODPath extends ODPath[VertexId, EdgeIdType] {
  override def srcVertex: VertexId
  override def dstVertex: VertexId
  override def path: List[EdgeIdType]
}
