package cse.fitzgero.sorouting.roadnetwork

import cse.fitzgero.sorouting.roadnetwork.edge.MacroscopicEdgeProperty
import cse.fitzgero.sorouting.roadnetwork.vertex.CoordinateVertexProperty
import org.apache.spark.graphx.Graph

package object graph {
  type RoadNetwork = Graph[CoordinateVertexProperty, MacroscopicEdgeProperty]
}
