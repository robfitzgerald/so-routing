package cse.fitzgero.sorouting.algorithm.shortestpath

import scala.math
import org.apache.spark.graphx.{Graph, VertexId}
import cse.fitzgero.sorouting.roadnetwork.edge.EdgeProperty
import cse.fitzgero.sorouting.roadnetwork.graph._
import cse.fitzgero.sorouting.roadnetwork.vertex.VertexProperty


object PregelDijkstras {

  type SPMap = Map[VertexId, Double]

  /**
    * based on sssp from Spark GraphX Guide: https://spark.apache.org/docs/2.1.0/graphx-programming-guide.html#pregel-api
    * @param graph the road network we will traverse
    * @param sourceId id of the starting vertex
    * @param destId id of the terminal vertex
    * @return
    */
  def shortestPath [E<: EdgeProperty, V <: VertexProperty[_], G <: RoadNetworkWrapper[E, V]](graph: G, sourceId: VertexId, destId: VertexId): (Path, Graph[SPMap, Double]) = {

    case class Message(w: Double, p: Path) extends Serializable

    // Initialize the graph such that all vertices except the root have distance infinity.
    val initialGraph = graph.g.mapVertices((id, _) =>
      if (id == sourceId) 0.0 else Message(Double.PositiveInfinity, Path()))

    val sssp = initialGraph.pregel(Message(Double.PositiveInfinity, Path()))(

      // Vertex Program
      (id: VertexId, myMsg: Message, receivedMsg: Message) =>
        if (myMsg.w < receivedMsg.w) myMsg else receivedMsg
      ,

      // Send Message
      triplet => {
        if (triplet.srcAttr + triplet.attr < triplet.dstAttr) {
          Iterator((triplet.dstId, triplet.srcAttr + triplet.attr))
        } else {
          Iterator.empty
        }
      },

      // Merge Message
      (a, b) => math.min(a, b)
    )

  }
}
