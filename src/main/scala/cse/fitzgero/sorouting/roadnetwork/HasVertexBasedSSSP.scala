package cse.fitzgero.sorouting.roadnetwork

import org.apache.spark.graphx.Graph

import cse.fitzgero.sorouting.roadnetwork.edge._
import cse.fitzgero.sorouting.roadnetwork.vertex._

/**
  * has a shortest path method which takes vertex objects for origin and desination
  */
trait HasVertexBasedSSSP {

  /**
    * finds and returns the path which is shortest for the given o/d pair
    * @param o an origin vertex in the graph
    * @param d a destination vertex in the graph
    * @tparam V graph vertex id type
    * @tparam E graph edge id type
    * @return sequence of edges describing a path
    */
  def findShortestPath [V <: VertexProperty[VertexPosition], E <: EdgeProperty](o: V, d: V): Seq[E]

  /**
    * increments the flow at edges used by this o/d pair's shortest path
    * @param o an origin given by a vertex in the graph
    * @param d a destination given by a vertex in the graph
    * @tparam V graph vertex type
    * @tparam E graph edge type
    * @return the modified road network graph
    */
  def setShortestPath [V <: VertexProperty[VertexPosition], E <: EdgeProperty](o: V, d: V): Graph[V, E]
}
