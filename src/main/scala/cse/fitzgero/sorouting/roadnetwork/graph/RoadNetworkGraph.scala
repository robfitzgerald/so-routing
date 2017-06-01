package cse.fitzgero.sorouting.roadnetwork.graph

import cse.fitzgero.sorouting.roadnetwork.io.reader.canReadNetworkFiles
import cse.fitzgero.sorouting.roadnetwork.edge._
import cse.fitzgero.sorouting.roadnetwork.path.Path
import cse.fitzgero.sorouting.roadnetwork.vertex._


abstract class RoadNetworkGraph [V <: VertexProperty, E <: EdgeProperty] {
  def shortestPath (o: Seq[(V, V)]): Seq[Path[_]]
}

object RoadNetworkGraph extends canReadNetworkFiles {
  def fromFile [V <: VertexProperty, E <: EdgeProperty](fileName : String): RoadNetworkGraph[V,E] = ???
}