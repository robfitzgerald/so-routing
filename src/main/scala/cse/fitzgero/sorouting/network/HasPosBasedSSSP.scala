package cse.fitzgero.sorouting.network

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
  def findShortestPath [E](o: (Double, Double), d: (Double, Double)): Seq[E]

  /**
    * increments the flow at edges used by this o/d pair's shortest path
    * @param o an origin given by x and y coordinates
    * @param d a destination given by x and y coordinates
    * @tparam G a road network graph
    * @return the modified road network graph
    */
  def setShortestPath [G](o: (Double, Double), d: (Double, Double)): G
}