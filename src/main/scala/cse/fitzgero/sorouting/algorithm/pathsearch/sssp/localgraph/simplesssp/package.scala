package cse.fitzgero.sorouting.algorithm.pathsearch.sssp.localgraph

import cse.fitzgero.sorouting.algorithm.pathsearch.{ODPair, ODPath}
import cse.fitzgero.sorouting.roadnetwork.localgraph.{EdgeId, VertexId}

package object simplesssp {

  case class SimpleSSSP_ODPair(srcVertex: VertexId, dstVertex: VertexId) extends ODPair[VertexId]

  case class SimpleSSSP_ODPath(srcVertex: VertexId, dstVertex: VertexId, path: List[EdgeId], cost: List[Double]) extends ODPath[VertexId, EdgeId]

  sealed trait BackPropagationData
  case class π(edge: EdgeId, srcVertex: VertexId, thisEdgeCost: Double) extends BackPropagationData
  case object Origin extends BackPropagationData
  case class SimpleSSSP_SearchNode(π: BackPropagationData, d: Double)

}
