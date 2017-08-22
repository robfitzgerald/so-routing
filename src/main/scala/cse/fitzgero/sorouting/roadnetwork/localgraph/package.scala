package cse.fitzgero.sorouting.roadnetwork

import cse.fitzgero.sorouting.roadnetwork.edge.MacroscopicEdgeProperty
import cse.fitzgero.sorouting.roadnetwork.vertex.CoordinateVertexProperty

package object localgraph {
  type VertexId = Long
  type EdgeId = String
  case class Triplet(o: VertexId, e: EdgeId, d: VertexId)
  type EdgeMATSim = MacroscopicEdgeProperty[EdgeId]
  type VertexMATSim = CoordinateVertexProperty
}
