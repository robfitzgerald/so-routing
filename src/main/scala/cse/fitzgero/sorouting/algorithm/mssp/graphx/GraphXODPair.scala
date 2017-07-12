package cse.fitzgero.sorouting.algorithm.mssp.graphx

import cse.fitzgero.sorouting.algorithm.mssp.ODPair
import org.apache.spark.graphx.VertexId

abstract class GraphXODPair extends ODPair[VertexId] {
  override def srcVertex: VertexId
  override def dstVertex: VertexId
}