package cse.fitzgero.sorouting.roadnetwork.io.reader

import cse.fitzgero.sorouting.roadnetwork.edge.EdgeProperty
import cse.fitzgero.sorouting.roadnetwork.graph.RoadNetworkGraph
import cse.fitzgero.sorouting.roadnetwork.vertex.VertexProperty

/**
  * Created by robertfitzgerald on 6/1/17.
  */
trait canReadNetworkFiles {
  def fromFile [V <: VertexProperty, E <: EdgeProperty](fileName: String): RoadNetworkGraph[V, E]
}
