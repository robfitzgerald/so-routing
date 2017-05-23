package cse.fitzgero.sorouting.network

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
  def findNearestVertex [V](point: (Double, Double)): V
}
