package cse.fitzgero.sorouting.algorithm.shortestpath

import org.apache.spark.graphx.{Edge, Graph, VertexId}
import org.apache.spark.graphx.lib.ShortestPaths
import org.apache.spark.graphx.lib.ShortestPaths.SPMap

import cse.fitzgero.sorouting.SparkUnitTestTemplate
import cse.fitzgero.sorouting.roadnetwork.costfunction._
import cse.fitzgero.sorouting.roadnetwork.edge.MacroscopicEdgeProperty
import cse.fitzgero.sorouting.roadnetwork.vertex.{CoordinateVertexProperty, Euclidian}
import cse.fitzgero.sorouting.roadnetwork.graph._

class GraphXPregelDijkstrasTests extends SparkUnitTestTemplate("GraphXPregelDijkstrasTests") {
  val equilNetworkFilePath: String =  "src/test/resources/GraphXPregelDijkstrasTests/network-matsim-example-equil.xml"
  val equilSnapshotFilePath: String = "src/test/resources/GraphXPregelDijkstrasTests/snapshot-matsim-example-equil.xml"
  "GraphXPregelDijkstras" when {
    "initializeShortestPathMap" when {
      "passed a set of origin-destination pairs" should {
        "return a map of those destinations to default weights (infinity)" in {
          val odPairs: Seq[(VertexId, VertexId)] = Seq((1,10), (2,20), (3,30))
          val initializeShortestPathMap = PrivateMethod[Map[VertexId, Double]]('initializeShortestPathMap)
          val result: Map[VertexId, Double] = GraphXPregelDijkstras invokePrivate initializeShortestPathMap(odPairs)

          Seq(10, 20, 30).foreach(v => {
            result(v) should equal (Double.PositiveInfinity)
          })
        }
      }
    }

    "initializeGraphVertexData" when {
      "passed a set of od pairs and a graph" should {
        "replace the vertices with the Id->Weight map which should be defined with Infinity values unless the Id corresponds to a source of an od pair" in {
          val graph: Graph[CoordinateVertexProperty, MacroscopicEdgeProperty] = Graph[CoordinateVertexProperty, MacroscopicEdgeProperty](
            sc.parallelize(List(
              (1, CoordinateVertexProperty(Euclidian(0,0))),
              (2, CoordinateVertexProperty(Euclidian(0,0))),
              (3, CoordinateVertexProperty(Euclidian(0,0))))),
            sc.parallelize(List(
              Edge(1,2,MacroscopicEdgeProperty("1",0,(x)=>1)),
              Edge(2,3,MacroscopicEdgeProperty("2",0,(x)=>1)),
              Edge(3,1,MacroscopicEdgeProperty("3",0,(x)=>1)))))
          val odPairs: Seq[(VertexId, VertexId)] = Seq((1,3), (2,1), (3,2))
          val initializeGraphVertexData = PrivateMethod[Graph[Map[VertexId, Double], MacroscopicEdgeProperty]]('initializeGraphVertexData)

          val result = GraphXPregelDijkstras invokePrivate initializeGraphVertexData(graph, odPairs)

          //shortest path searches from origins of our odPairs should have initial distance values of zero, otherwise infinity
          result.vertices.toLocalIterator.foreach(v => {
            (1 to 3).foreach({
              case x if x == v._1 => v._2(x) should equal (0)
              case y => v._2(y) should equal (Double.PositiveInfinity)
            })

          })
        }
      }
    }

    "vertexProgram" when {
      "called with the vertexId, local information, and summed neighbor messages" should {
        "choose which values are better for all shortest paths being calculated" in {

        }
      }
    }

    "sendMessage" when {
      "called with a vertex->edge->vertex triplet in the send message phase" should {
        "decide if it has a shorter path to its destination, then send that updated data to its destination vertex, else return an empty output" in {

        }
      }
    }

    "mergeMessage" when {
      "called with two messages headed to the same vertex" should {
        "take the smaller values from each message" in {

        }
      }
    }


    "shortestPath" ignore {
      "given a pair of vertex ids and a graph with no congestion and a constant-valued cost function" should {
        "return the correct shortest path, which should be shortest by number of links" in {
          val roadNetwork: Graph[CoordinateVertexProperty, MacroscopicEdgeProperty] = GraphXMacroRoadNetwork(sc, TestCostFunction).fromFile(equilNetworkFilePath).get
          println("here")
          val result: Graph[SPMap, MacroscopicEdgeProperty] = ShortestPaths.run[CoordinateVertexProperty, MacroscopicEdgeProperty](roadNetwork, Seq[VertexId](12))
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
