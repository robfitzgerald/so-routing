package cse.fitzgero.sorouting.algorithm.pathsearch.mssp.graphx

import cse.fitzgero.sorouting.algorithm.pathsearch._
import cse.fitzgero.sorouting.algorithm.pathsearch.od.ODPath
import cse.fitzgero.sorouting.roadnetwork.graphx.edge.EdgeIdType
import org.apache.spark.graphx.VertexId

abstract class GraphXODPath extends ODPath[VertexId, EdgeIdType] with Serializable {
  override def srcVertex: VertexId
  override def dstVertex: VertexId
  override def path: List[EdgeIdType]
}
