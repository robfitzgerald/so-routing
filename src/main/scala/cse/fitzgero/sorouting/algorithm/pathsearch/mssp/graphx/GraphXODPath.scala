package cse.fitzgero.sorouting.algorithm.pathsearch.mssp.graphx

import cse.fitzgero.sorouting.algorithm.pathsearch.od.ODPath
import cse.fitzgero.sorouting.roadnetwork.localgraph.EdgeId
import org.apache.spark.graphx.VertexId

abstract class GraphXODPath extends ODPath[VertexId, EdgeId] with Serializable {
  override def srcVertex: VertexId
  override def dstVertex: VertexId
  override def path: List[EdgeId]
}
