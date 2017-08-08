package cse.fitzgero.sorouting.algorithm.pathsearch.sssp

import cse.fitzgero.sorouting.roadnetwork.localgraph.{EdgeId, VertexId}

package object localgraphsimplesssp {
  sealed trait BackPropagationData
  case class π(edge: EdgeId, srcVertex: VertexId, thisEdgeCost: Double) extends BackPropagationData
  case object Origin extends BackPropagationData
  case class SimpleSSSP_SearchNode(π: BackPropagationData, d: Double)

}
