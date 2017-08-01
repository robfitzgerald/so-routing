package cse.fitzgero.sorouting.roadnetwork

import cse.fitzgero.sorouting.roadnetwork.edge.MacroscopicEdgeProperty
import cse.fitzgero.sorouting.roadnetwork.vertex.CoordinateVertexProperty

package object localgraph {
  type VertexId = Long
  type EdgeId = Long
  type EdgeMATSim = MacroscopicEdgeProperty[EdgeId]
  type VertexMATSim = CoordinateVertexProperty
}
