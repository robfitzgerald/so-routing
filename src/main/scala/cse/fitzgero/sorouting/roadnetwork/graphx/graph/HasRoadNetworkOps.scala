package cse.fitzgero.sorouting.roadnetwork.graphx.graph

/**
  * a singleton extends this trait if it can compute road network operations on a Graph G
  */
trait HasRoadNetworkOps {

  /**
    * randomly samples the road network graph edge list and returns the flows of that sampling
    * @param graph the road network graph
    * @param samplePercentage a percentage in the range [0, 1]
    * @tparam G the type of the road network graph
    * @return
    */
  def getCostFlowValues [G] (graph: G, samplePercentage: Double): Traversable[Double]

  /**
    * finds the shortest paths of odPairs, and returns a graph with modified flow data as well as the list of odPairs with their shortest path lists
    * @param graph the road network graph
    * @param odPairs tuples of origin and destination values
    * @tparam IG the type of the road network graph
    * @tparam OG the type of the output graph
    * @tparam ID the type of the vertex id
    * @tparam P the collection type for storing edge path data (i.e. List[String] for a list of String Edge Ids)
    * @return
    */
  def shortestPath [IG,OG,ID,P] (graph: IG, odPairs: Traversable[(ID, ID)]): (OG, Traversable[(ID, ID, Traversable[P])])
}