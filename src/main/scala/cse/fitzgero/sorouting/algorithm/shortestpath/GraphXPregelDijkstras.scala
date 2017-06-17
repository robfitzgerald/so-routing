package cse.fitzgero.sorouting.algorithm.shortestpath

import org.apache.spark.graphx.{EdgeTriplet, Graph, VertexId}
import cse.fitzgero.sorouting.roadnetwork.edge._
import cse.fitzgero.sorouting.roadnetwork.graph._
import cse.fitzgero.sorouting.roadnetwork.vertex._

/**
  * some links on shortest path Pregel problems and GraphX
  * https://databricks-prod-cloudfront.cloud.databricks.com/public/4027ec902e239c93eaaa8714f173bcfc/3741049972324885/1494815129692969/4413065072037724/latest.html
  * https://blog.insightdatascience.com/computing-shortest-distances-incrementally-with-spark-1a280064a0b9
  * https://spark.apache.org/docs/2.1.0/graphx-programming-guide.html#pregel-api
  */

object GraphXPregelDijkstras {
  val DefaultValue: Double = Double.PositiveInfinity

  /**
    * creates the initial vertex data for the shortest path operation: a map of source Ids to distance values
    * @param odPairs origin/destination tuples for this shortest path search
    * @return
    */
  private def initializeShortestPathMap(odPairs: Seq[(VertexId, VertexId)]): Map[VertexId, Double] = {
    odPairs.foldLeft(Map.empty[VertexId, Double])((acc, odPair) => acc.updated(odPair._2, DefaultValue))
  }

  /**
    * updates the graph with initial data, correctly setting distance values of zero for source Ids of our search
    * @param graph road network graph
    * @param odPairs origin/destination tuples for this shortest path search
    * @return
    */
  private def initializeGraphVertexData (graph: Graph[CoordinateVertexProperty, MacroscopicEdgeProperty], odPairs: Seq[(VertexId, VertexId)]): Graph[Map[VertexId, Double], MacroscopicEdgeProperty] = {
    val startVals: Map[VertexId, Double] = initializeShortestPathMap(odPairs)
    graph.mapVertices((id, _) =>
      if (startVals isDefinedAt id) startVals.updated(id, 0) else startVals)
  }


  private def vertexProgram (vertexId: VertexId, localInfo: Map[VertexId, Double], newInfo: Map[VertexId, Double]) = ???

  private def sendMessage (edge: EdgeTriplet[CoordinateVertexProperty, MacroscopicEdgeProperty]): Iterator[(VertexId, Map[VertexId, Double])] = ???

  private def mergeMessage (a: Map[VertexId, Double], b: Map[VertexId, Double]): Map[VertexId, Double] = ???
}






// older version


//object GraphXPregelDijkstras extends HasRoadNetworkOps {
//
//
//  type RoadNetwork = Graph[CoordinateVertexProperty,MacroscopicEdgeProperty]
//  type Path = List[String]
//  def Path(): Path = List.empty[String]
//  type SPMap = Map[VertexId, Path]
//  case class Message(w: Double, p: Path) extends Serializable
//
//  val Infinity: Double = Double.PositiveInfinity
//  /**
//    * based on sssp from Spark GraphX Guide: https://spark.apache.org/docs/2.1.0/graphx-programming-guide.html#pregel-api
//    * @param graph the road network we will traverse
//    * @param odPairs id of the starting and terminal vertices
//    * @return
//    */
//  def shortestPath
//  [RoadNetwork, VertexId, Path]
//  (
//    graph: Graph[CoordinateVertexProperty,MacroscopicEdgeProperty],
//    odPairs: Seq[(VertexId, VertexId)]
//  ):
//  (Graph[SPMap, Double], Seq[(VertexId, VertexId, Path)]) = {
//
//
//
//    // Initialize the graph such that all vertices except the root have distance infinity.
//    val initialGraph = graph.mapVertices((id, _) =>
//      if (id == sourceId) 0.0 else Message(Infinity, List[String]()))
//
//    val sssp = initialGraph.pregel(Message(Infinity., Path()))(
//
//      // Vertex Program
//      (id: VertexId, myMsg: Message, receivedMsg: Message) =>
//        if (myMsg.w < receivedMsg.w) myMsg else receivedMsg
//      ,
//
//      // Send Message
//      triplet => {
//        if (triplet.srcAttr + triplet.attr < triplet.dstAttr) {
//          Iterator((triplet.dstId, triplet.srcAttr + triplet.attr))
//        } else {
//          Iterator.empty
//        }
//      },
//
//      // Merge Message
//      (a, b) => math.min(a, b)
//    )
//
//  }
//}
