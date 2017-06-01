package cse.fitzgero.sorouting.roadnetwork.graph

import org.apache.spark.graphx._
import cse.fitzgero.sorouting.roadnetwork.edge._
import cse.fitzgero.sorouting.roadnetwork.io.reader._
import cse.fitzgero.sorouting.roadnetwork.path.Path
import cse.fitzgero.sorouting.roadnetwork.vertex._

class GraphSparkGraphX [V <: VertexProperty, E <: EdgeProperty](g: Graph[V, E]) extends RoadNetworkGraph[V, E] {
  type IdType = Long
  def getCostFlowValues (linkIds: Seq[IdType]): Seq[Double] = ???
  def shortestPath (OD: Seq[(V, V)]): Seq[Path[IdType]] = ???
}

object GraphSparkGraphX extends canReadNetworkFiles {
  def fromFile [V <: VertexProperty, E <: EdgeProperty](fileName : String): GraphSparkGraphX[V, E] = ???
}