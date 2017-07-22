package cse.fitzgero.sorouting.algorithm.sssp.localgraph

import cse.fitzgero.sorouting.algorithm.sssp._
import cse.fitzgero.sorouting.roadnetwork.localgraph._

case class SimpleSSSP_ODPair(srcVertex: VertexId, dstVertex: VertexId) extends ODPair[VertexId]
case class SimpleSSSP_ODPath(srcVertex: VertexId, dstVertex: VertexId, path: List[EdgeId]) extends ODPath[VertexId, EdgeId]

class SimpleSSSP [V, E] extends SSSP[LocalGraph[V, E],SimpleSSSP_ODPair,SimpleSSSP_ODPath] {
  override def shortestPath(graph: LocalGraph[V, E], od: SimpleSSSP_ODPair): SimpleSSSP_ODPath = ???
}
