package cse.fitzgero.sorouting.network.graph

import org.apache.spark.graphx.Graph

import cse.fitzgero.sorouting.network.edge._
import cse.fitzgero.sorouting.network.vertex._
import cse.fitzgero.sorouting.network.{CalculatesGradientObjective, HasVertexBasedSSSP}
import cse.fitzgero.sorouting.algorithm.shortestpath.PregelDijkstras

object MatSimEdgeListRoadNetwork {
  type Intersection = CoordinateVertexProperty
  type Link = FrankWolfeEdgeProperty
  type RoadNetwork = Graph[Intersection, Link]
  //  def fromMATSimEdgeFile (fileName: String, sc: SparkContext): MATSimEdgeListRoadNetwork = ???
}

import MatSimEdgeListRoadNetwork.{Intersection, Link, RoadNetwork}

class MATSimEdgeListRoadNetwork (val g: Graph[Intersection, Link])

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

  def findShortestPath[Intersection, Link](o: Intersection, d: Intersection): Seq[Link] = ???
  def setShortestPath[Intersection, Link](o: Intersection, d: Intersection): RoadNetwork = ???
}

