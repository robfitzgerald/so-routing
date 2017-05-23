package cse.fitzgero.sorouting.network

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
  def findShortestPath [V, E](o: V, d: V): Seq[E]

  /**
    * increments the flow at edges used by this o/d pair's shortest path
    * @param o an origin given by a vertex in the graph
    * @param d a destination given by a vertex in the graph
    * @tparam G a road network graph
    * @tparam V graph vertex id type
    * @return the modified road network graph
    */
  def setShortestPath [G, V](o: V, d: V): G
}
