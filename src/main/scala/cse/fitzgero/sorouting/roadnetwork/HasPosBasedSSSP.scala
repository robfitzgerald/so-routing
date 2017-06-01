package cse.fitzgero.sorouting.roadnetwork

import org.apache.spark.graphx.Graph

import cse.fitzgero.sorouting.roadnetwork.edge._
import cse.fitzgero.sorouting.roadnetwork.vertex._

/**
  * has a shortest path method which takes (x,y) coordinate tuples for
  * origin and destination and must find the nearest vertex to that position
  */
trait HasPosBasedSSSP {

  /**
    * finds and returns the path which is shortest for the given o/d pair
    * @param o an origin given by x and y coordinates
    * @param d a destination given by x and y coordinates
    * @tparam E graph edge id type
    * @return sequence of edges describing a path
    */
  def findShortestPath [V <: VertexProperty, E <: EdgeProperty](o: (Double, Double), d: (Double, Double)): Seq[E]

  /**
    * increments the flow at edges used by this o/d pair's shortest path
    * @param o an origin given by x and y coordinates
    * @param d a destination given by x and y coordinates
    * @tparam V graph vertex type
    * @tparam E graph edge type
    * @return the modified road network graph
    */
  def setShortestPath [V <: VertexProperty, E <: EdgeProperty](o: (Double, Double), d: (Double, Double)): Graph[E, V]
}