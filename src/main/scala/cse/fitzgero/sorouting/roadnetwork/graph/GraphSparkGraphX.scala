package cse.fitzgero.sorouting.roadnetwork.graph

import org.apache.spark.graphx.{EdgeRDD, Graph, VertexRDD}
import cse.fitzgero.sorouting.roadnetwork.edge._
import cse.fitzgero.sorouting.roadnetwork.io.reader._
import cse.fitzgero.sorouting.roadnetwork.path.Path
import cse.fitzgero.sorouting.roadnetwork.vertex._

class GraphSparkGraphX [V <: VertexProperty, E <: EdgeProperty](g: Graph[V, E]) extends RoadNetworkGraph[V, E] {
  type IdType = String
//  val vertices: VertexRDD[V]  = g.vertices
//  val edges: EdgeRDD[E] = g.edges
  def shortestPath (OD: Seq[(V, V)]): Seq[Path[IdType]] = ???
}

object GraphSparkGraphX extends canReadNetworkFiles {
  def fromFile [V <: VertexProperty, E <: EdgeProperty](fileName : String): GraphSparkGraphX[V, E] = ???
}