package cse.fitzgero.sorouting.roadnetwork.graphx

import cse.fitzgero.sorouting.roadnetwork.graphx.edge.MacroscopicEdgeProperty
import cse.fitzgero.sorouting.roadnetwork.graphx.vertex.CoordinateVertexProperty
import org.apache.spark.graphx.Graph

package object graph {
  type GraphxRoadNetwork = Graph[CoordinateVertexProperty, MacroscopicEdgeProperty]
}
