package cse.fitzgero.sorouting.network

import org.apache.spark.SparkContext
import org.apache.spark._
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD

import cse.fitzgero.sorouting.network.edge._
import cse.fitzgero.sorouting.network.vertex._

class MATSimEdgeListRoadNetwork (val g: Graph[CoordinateVertexProperty, FrankWolfeEdgeProperty])
  extends CalculatesGradientObjective
  with HasPosBasedSSSP {

  /**
    * calculates the objective Σ VaCa(V), flow * cost(flow) ∀ e ∈ G
    * @return a value to be minimized
    */
  def calculateObjective(): Double = {
    g.edges.map(row=>row.attr.flow * row.attr.cost()).reduce(_+_)
  }

  def findShortestPath[E](o: (Double, Double),d: (Double, Double)): Seq[E] = ???
  def setShortestPath[G](o: (Double, Double),d: (Double, Double)): G = ???
}

object MATSimEdgeListRoadNetwork {
//  def fromMATSimEdgeFile (fileName: String, sc: SparkContext): MATSimEdgeListRoadNetwork = {
//
//  }
}