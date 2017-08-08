package cse.fitzgero.sorouting.algorithm.pathsearch.mssp.graphx

import cse.fitzgero.sorouting.algorithm.pathsearch._
import cse.fitzgero.sorouting.algorithm.pathsearch.od.ODPair
import org.apache.spark.graphx.VertexId

abstract class GraphXODPair extends ODPair[VertexId] with Serializable {
  override def srcVertex: VertexId
  override def dstVertex: VertexId
}