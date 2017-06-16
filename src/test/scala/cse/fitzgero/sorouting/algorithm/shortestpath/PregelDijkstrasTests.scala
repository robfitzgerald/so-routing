package cse.fitzgero.sorouting.algorithm.shortestpath

import cse.fitzgero.sorouting.SparkUnitTestTemplate
import cse.fitzgero.sorouting.roadnetwork.costfunction._
import cse.fitzgero.sorouting.roadnetwork.edge.MacroscopicEdgeProperty
import cse.fitzgero.sorouting.roadnetwork.vertex.CoordinateVertexProperty
import cse.fitzgero.sorouting.roadnetwork.graph._
import org.apache.spark.graphx.Graph
import org.apache.spark.graphx.VertexId
import org.apache.spark.graphx.lib.ShortestPaths
import org.apache.spark.graphx.lib.ShortestPaths.SPMap

class PregelDijkstrasTests extends SparkUnitTestTemplate("PregelDijkstrasTests") {
  val equilNetworkFilePath: String =  "src/test/resources/PregelDijkstrasTests/network-matsim-example-equil.xml"
  val equilSnapshotFilePath: String = "src/test/resources/PregelDijkstrasTests/snapshot-matsim-example-equil.xml"
  "PregelDijkstras" when {
    "shortestPath" when {
      "given a pair of vertex ids and a graph with no congestion and a constant-valued cost function" should {
        "return the correct shortest path, which should be shortest by number of links" in {
          val roadNetwork: GraphXMacroRoadNetwork = GraphXMacroFactory(sc, TestCostFunction).fromFile(equilNetworkFilePath).get
          println("here")
          val result: Graph[SPMap, MacroscopicEdgeProperty] = ShortestPaths.run[CoordinateVertexProperty, MacroscopicEdgeProperty](roadNetwork.g, Seq[VertexId](12))
          println("here")
          result.triplets.toLocalIterator.foreach(t => {
            println(s"edge ${t.srcId} ${t.attr.id} ${t.dstId}")
            println(s"source distances ${t.srcAttr.mkString("->")}")
            println(s"destination distances ${t.dstAttr.mkString("->")}")
          })
        }
      }
    }
  }
}
