package cse.fitzgero.sorouting.algorithm.shortestpath.mssp.graphx

import cse.fitzgero.sorouting.algorithm.shortestpath._
import org.apache.spark.graphx.VertexId

abstract class GraphXODPair extends ODPair[VertexId] with Serializable {
  override def srcVertex: VertexId
  override def dstVertex: VertexId
}