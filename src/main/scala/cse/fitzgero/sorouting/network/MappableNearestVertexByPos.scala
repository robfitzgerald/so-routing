package cse.fitzgero.sorouting.network

import cse.fitzgero.sorouting.network.vertex._

/**
  * has a method which can be used to map from points to vertex ids
  */
trait MappableNearestVertexByPos {
  /**
    * performs a search for the nearest vertex by coordinate position
    * @param point tuple (x,y) coordinate
    * @tparam V graph vertex id type
    * @return the vertex id of the closest vertex to this point
    */
  def findNearestVertex [V <: VertexProperty](point: (Double, Double)): V
}
