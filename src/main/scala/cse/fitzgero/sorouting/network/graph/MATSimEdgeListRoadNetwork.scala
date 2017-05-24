package cse.fitzgero.sorouting.network.graph

import org.apache.spark.graphx.Graph

import cse.fitzgero.sorouting.network.edge._
import cse.fitzgero.sorouting.network.vertex._
import cse.fitzgero.sorouting.network.{CalculatesGradientObjective, HasVertexBasedSSSP}
import cse.fitzgero.sorouting.algorithm.shortestpath.PregelDijkstras

class MATSimEdgeListRoadNetwork (val g: Graph[CoordinateVertexProperty, FrankWolfeEdgeProperty])
  extends GenericRoadNetwork
  with CalculatesGradientObjective
  with HasVertexBasedSSSP {

  /**
    * calculates the objective Σ VaCa(V), flow * cost(flow) ∀ e ∈ G
    * @return a value to be minimized
    */
  def calculateObjective(): Double = {
    g.edges.map(row=>row.attr.flow * row.attr.cost()).reduce(_+_)
  }

  def findShortestPath[CoordinateVertexProperty, FrankWolfeEdgeProperty](o: CoordinateVertexProperty, d: CoordinateVertexProperty): Seq[FrankWolfeEdgeProperty] = ???
  def setShortestPath[CoordinateVertexProperty, FrankWolfeEdgeProperty](o: CoordinateVertexProperty, d: CoordinateVertexProperty): Graph[CoordinateVertexProperty, FrankWolfeEdgeProperty] = ???
}

object MATSimEdgeListRoadNetwork {
//  def fromMATSimEdgeFile (fileName: String, sc: SparkContext): MATSimEdgeListRoadNetwork = {
//
//  }
}