package cse.fitzgero.sorouting.algorithm.pathsearch.od.graphx

import cse.fitzgero.sorouting.algorithm.pathsearch.od.ODPair
import org.apache.spark.graphx.VertexId

abstract class GraphXODPair extends ODPair[VertexId] with Serializable {
  override def src: VertexId
  override def dst: VertexId
}