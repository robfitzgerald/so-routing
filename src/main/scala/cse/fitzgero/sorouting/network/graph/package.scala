package cse.fitzgero.sorouting.network

import org.apache.spark.graphx.Graph
import cse.fitzgero.sorouting.network.edge._
import cse.fitzgero.sorouting.network.vertex._

package object graph {
  type Intersection = CoordinateVertexProperty
  type Link = FrankWolfeEdgeProperty
  type RoadNetwork = Graph[Intersection, Link]
}
