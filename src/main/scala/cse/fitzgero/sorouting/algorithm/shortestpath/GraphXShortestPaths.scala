package cse.fitzgero.sorouting.algorithm.shortestpath

import org.apache.spark.graphx.{EdgeTriplet, VertexId}
import cse.fitzgero.sorouting.roadnetwork.edge._
import cse.fitzgero.sorouting.roadnetwork.graph._


/**
  * some links on shortest path Pregel problems and GraphX
  * https://databricks-prod-cloudfront.cloud.databricks.com/public/4027ec902e239c93eaaa8714f173bcfc/3741049972324885/1494815129692969/4413065072037724/latest.html
  * https://blog.insightdatascience.com/computing-shortest-distances-incrementally-with-spark-1a280064a0b9
  * https://spark.apache.org/docs/2.1.0/graphx-programming-guide.html#pregel-api
  */

object GraphXShortestPaths {
  private val Infinity: Double = Double.PositiveInfinity
  private val Zero: Double = 0.0D

  /**
    * For each OD pair, finds its shortest path and total path cost within the graph
    * @param graph a road network
    * @param odPairs tuples of (Origin Vertex Id, Destination Vertex Id)
    * @return a collection of tuples (Origin Vertex Id, Destination Vertex Id, Shortest Path as Edge Ids)
    */
  def shortestPaths (graph: RoadNetwork, odPairs: Seq[(VertexId, VertexId)], costMethod: CostMethod = CostFlow()): Seq[(VertexId, VertexId, Path)] = {
    val destinations: Seq[VertexId] = odPairs.map(_._2)
    val shortestPathsGraph: ShortestPathsGraph =
      initializeShortestPathsGraph(graph, odPairs)
        .pregel(initialShorestPathsMessage(odPairs))(
          shortestPathVertexProgram,
          shortestPathSendMessageWrapper(costMethod),
          shortestPathMergeMessage
        )
    val relevantSubset: Map[VertexId, SPGraphData] = shortestPathsGraph.vertices.filter(destinations contains _._1).collect().toMap
    odPairs.map(tuple => {
      (tuple._1, tuple._2, relevantSubset(tuple._2)(tuple._1).path)
    })
  }


  /**
    * Runs the Pregel operation for the provided graph and collection of origin/destination pairs, left here for testing only
    * @param graph a road network
    * @param odPairs tuples of OD pairs
    * @return A Shortest Paths graph where each vertex contains shortest path information from each listed origin vertex
    */
  private def runPregelShortestPaths (graph: RoadNetwork, odPairs: Seq[(VertexId, VertexId)]): ShortestPathsGraph =
    initializeShortestPathsGraph(graph, odPairs)
      .pregel(initialShorestPathsMessage(odPairs))(
        shortestPathVertexProgram,
        shortestPathSendMessageWrapper(CostFlow()),
        shortestPathMergeMessage
      )


  /**
    * creates the initial vertex data for the shortest path operation: a map of source Ids to distance values
    * @param odPairs origin/destination tuples for this shortest path search
    * @return
    */
  private def initialShorestPathsMessage(odPairs: Seq[(VertexId, VertexId)]): SPGraphData =
    odPairs.foldLeft(Map.empty[VertexId, WeightAndPath])((map, tuple) => {
      map + (tuple._1 -> WeightAndPath())
    }).withDefaultValue(WeightAndPath())


  /**
    * updates the graph with initial data, correctly setting distance values of zero for source Ids of our search
    * @param graph road network graph
    * @param odPairs origin/destination tuples for this shortest path search
    * @return
    */
  private def initializeShortestPathsGraph (graph: RoadNetwork, odPairs: Seq[(VertexId, VertexId)]): ShortestPathsGraph = {
    val startVals: Map[VertexId, WeightAndPath] = initialShorestPathsMessage(odPairs)  // was the shortestPathMap function
    graph.mapVertices((id, _) =>
      if (startVals isDefinedAt id) startVals.updated(id, WeightAndPath(Zero)) else startVals)
  }


  /**
    * Pregel vertex update function
    * @param vertexId the current vertex
    * @param localInfo the data at the vertex
    * @param newInfo the data arriving by the most recent sendMessage step
    * @return the value used to update this vertex
    */
  private def shortestPathVertexProgram (vertexId: VertexId, localInfo: SPGraphData, newInfo: SPGraphData): SPGraphData = {
    newInfo.foldLeft(localInfo)((pathDistances, tuple) => {
      if (pathDistances(tuple._1).weight > tuple._2.weight) pathDistances + tuple else pathDistances
    }).withDefaultValue(WeightAndPath())
  }


  /**
    * Closure to allow passing costMethod into scope of shorestPathSendMessage function
    * @param costMethod a case class used to determine which method of cost function we want to use
    * @return shortestPathSendMessage() ready for Pregel
    */
  private def shortestPathSendMessageWrapper(costMethod: CostMethod) = {
    /**
      * Pregel send message function
      * @param edge the current edge triplet: src-[edge]->dst
      * @return a message to forward to the destination vertex, or no message at all
      */
    def shortestPathSendMessage()(edge: EdgeTriplet[SPGraphData, MacroscopicEdgeProperty]): Iterator[(VertexId, SPGraphData)] = {
      val edgeWeight: Double = costMethod match {
        case CostFlow() => edge.attr.linkCostFlow
        case AONFlow() => edge.attr.cost.freeFlowCost
      }
      if (edge.srcAttr.forall(src => {
        (src._2.weight + edgeWeight) >= edge.dstAttr.getOrElse(src._1, WeightAndPath()).weight
      })) Iterator.empty
      else {
        // identity called on srcWithEdgeWeight to avoid mapValues returning a non-serializable object
        // https://stackoverflow.com/questions/17709995/notserializableexception-for-mapstring-string-alias
        val srcWithEdgeWeight = edge.srcAttr.mapValues(data => {
          data.copy(
            weight = data.weight + edgeWeight,
            path = data.path :+ edge.attr.id
          )
        }).map(identity)

        val newVals =
          edge.dstAttr.foldLeft(srcWithEdgeWeight.withDefaultValue(WeightAndPath()))((srcDistancesPlusEdge, destCostTuple) => {
            val vertex: VertexId = destCostTuple._1
            val destCost: WeightAndPath = destCostTuple._2
            if (!srcDistancesPlusEdge.isDefinedAt(vertex) ||
              srcDistancesPlusEdge(vertex).weight > destCost.weight) srcDistancesPlusEdge + destCostTuple
            else srcDistancesPlusEdge
          })
        Iterator((edge.dstId, newVals))
      }
    }
    shortestPathSendMessage()_
  }


  /**
    * Pregel merge function
    * @param a left operand of a merge between two messages
    * @param b right operand of a merge between two messages
    * @return a single message to be received by the vertex program
    */
  private def shortestPathMergeMessage (a: SPGraphData, b: SPGraphData): SPGraphData = {
    a.foldLeft(b.withDefaultValue(WeightAndPath()))((pathDistances, tuple) => {
      if (pathDistances(tuple._1).weight > tuple._2.weight) pathDistances + tuple else pathDistances
    }).withDefaultValue(WeightAndPath())
  }
}
