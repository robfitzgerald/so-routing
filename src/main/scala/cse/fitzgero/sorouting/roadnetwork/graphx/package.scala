package cse.fitzgero.sorouting.roadnetwork

import cse.fitzgero.sorouting.roadnetwork.edge.MacroscopicEdgeProperty
import cse.fitzgero.sorouting.roadnetwork.vertex.CoordinateVertexProperty
import org.apache.spark.graphx.Graph

package object graphx {
  type EdgeIdType = String
  type GraphXEdge = MacroscopicEdgeProperty[EdgeIdType]
  type GraphxRoadNetwork = Graph[CoordinateVertexProperty, GraphXEdge]
}
