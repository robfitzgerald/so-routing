//package cse.fitzgero.sorouting.roadnetwork
//
//import cse.fitzgero.sorouting.roadnetwork.edge._
//import cse.fitzgero.sorouting.roadnetwork.vertex._
//import org.apache.spark.graphx.Graph
//
//object MatSimEdgeListRoadNetwork {
////  type Intersection = CoordinateVertexProperty
////  type Link = FrankWolfeEdgeProperty
////  type RoadNetwork = Graph[Intersection, Link]
//  //  def fromMATSimEdgeFile (fileName: String, sc: SparkContext): MATSimEdgeListRoadNetwork = ???
//}
//
////import MatSimEdgeListRoadNetwork.{Intersection, Link, RoadNetwork}
//
//class MATSimEdgeListRoadNetwork [V <: VertexProperty, E <: EdgeProperty](val g: Graph[V, E])
//
//  extends Graph
//  with CalculatesGradientObjective
//  with HasVertexBasedSSSP {
//
//  /**
//    * calculates the objective Σ VaCa(V), flow * cost(flow) ∀ e ∈ G
//    * @return a value to be minimized
//    */
//  def calculateObjective(): Double = {
//    g.edges.map(row=>row.attr.flow * row.attr.cost()).reduce(_+_)
//  }
//
//  def findShortestPath(o: V, d: V): Seq[E] = ???
//  def setShortestPath(o: V, d: V): Graph = ???
//}
//
