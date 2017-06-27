package cse.fitzgero.sorouting.roadnetwork.graph

import java.io.IOException

import org.apache.spark.graphx.{Edge, EdgeRDD, Graph, VertexId}
import org.apache.spark.rdd.RDD
import cse.fitzgero.sorouting.roadnetwork.edge._
import cse.fitzgero.sorouting.roadnetwork.vertex._
import cse.fitzgero.sorouting.SparkUnitTestTemplate
import cse.fitzgero.sorouting.roadnetwork.costfunction._

import scala.xml.XML

class GraphXMacroRoadNetworkTests extends SparkUnitTestTemplate("GraphXMacroRoadNetworkTests") {
  "GraphXMacroRoadNetwork Class" when {
    val networkFilePath: String =       "src/test/resources/GraphXMacroRoadNetworkTests/network.xml"
    val snapshotFilePath: String =      "src/test/resources/GraphXMacroRoadNetworkTests/snapshot.xml"
    val equilNetworkFilePath: String =  "src/test/resources/GraphXMacroRoadNetworkTests/network-matsim-example-equil.xml"
    val equilSnapshotFilePath: String = "src/test/resources/GraphXMacroRoadNetworkTests/snapshot-matsim-example-equil.xml"
    val missingFilePath: String =       "src/test/resources/GraphXMacroRoadNetworkTests/blah-invalid.xml"
    val testXML: xml.Elem =
      <network>
        <links>
          <link id="1" from="1" to="2" freespeed="27.78" capacity="1000.0"/>
          <link id="2" from="2" to="3" freespeed="27.78" capacity="2000.0"/>
          <link id="3" from="3" to="1" freespeed="27.78" capacity="4000.0"/>
        </links>
        <nodes>
          <node id="1" x="-10" y="-10"/>
          <node id="2" x="-10" y="10"/>
          <node id="3" x="10" y="5"/>
        </nodes>
      </network>
    val testFlows: xml.Elem =
      <network name="test flows">
        <links>
          <link id="1" flow="10"></link>
          <link id="2" flow="20"></link>
          <link id="3" flow="30"></link>
        </links>
      </network>
    val badFlows: xml.Elem =
      <network name="bad flows">
        <links>
          <link id="1"></link>
          <link id="a" flow="abcd"></link>
          <link id="true" flows="nose"></link>
        </links>
      </network>
    val testFlowsMap: Map[String, Int] = Map(
      ("1", 10),
      ("2", 20),
      ("3", 30)
    )
    "grabEdges" when {
      "passed a valid xml.Elem object" should {
        "produce a correct EdgeRDD" in {
          val grabEdges = PrivateMethod[EdgeRDD[MacroscopicEdgeProperty]]('grabEdges)
          val result: RDD[Edge[MacroscopicEdgeProperty]] = GraphXMacroRoadNetwork(sc, TestCostFunction) invokePrivate grabEdges(testXML, testFlows)
          // confirm that the flow values in our graph are equivalent to those stored in the test object
          result.map(edge => edge.attr.flow == testFlowsMap(edge.attr.id)).reduce(_&&_) should equal (true)
          // confirm the TestCostFunction, which should be (x) => 1
          result.map(edge => edge.attr.linkCostFlow).sum should equal (3)
        }
      }
      "passed an xml.Elem object which has a network with links, but links are malformed (i.e. bad 'id' or 'flow' attributes)" should {
        "throw an IOException" in {
          val grabEdges = PrivateMethod[EdgeRDD[MacroscopicEdgeProperty]]('grabEdges)
          val thrown = the [java.io.IOException] thrownBy {GraphXMacroRoadNetwork(sc, TestCostFunction) invokePrivate grabEdges(testXML, badFlows)}
          thrown getMessage() should startWith ("snapshot flow data was malformed")
        }
      }
      "passed a real CostFunctionFactory, we can call those from spark" in {
        val grabEdges = PrivateMethod[EdgeRDD[MacroscopicEdgeProperty]]('grabEdges)
        val result: RDD[Edge[MacroscopicEdgeProperty]] = GraphXMacroRoadNetwork(sc, BPRCostFunction) invokePrivate grabEdges(testXML, testFlows)
        // confirm that the costFlow function result is never the same as the flow amount
        // (a simple check that applying the function yielded a different number)
        result.map(edge => edge.attr.linkCostFlow != edge.attr.flow).reduce(_&&_) should equal (true)
      }
    }
    "grabVertices" when {
      "passed a valid xml.Elem object" should {
        "produce a correct VertexRDD" in {
          val grabVertices = PrivateMethod[RDD[(Long, CoordinateVertexProperty)]]('grabVertices)
          val result: RDD[(Long, CoordinateVertexProperty)] = GraphXMacroRoadNetwork(sc, TestCostFunction) invokePrivate grabVertices(testXML)
          val makeLocal: Map[String, CoordinateVertexProperty] = result.map(vertex => (vertex._1.toString, vertex._2)).toLocalIterator.toMap
          makeLocal("1").position should equal (Euclidian(-10, -10))
          makeLocal("2").position should equal (Euclidian(-10, 10))
          makeLocal("3").position should equal (Euclidian(10, 5))
        }
      }
    }
    "fromFile" when {
      "passed a file path for a valid MATSim network_v2.xml file" should {
        "produce a correct GraphXMacroRoadNetwork, with all flows equal to 0" in {
          val result: Graph[CoordinateVertexProperty, MacroscopicEdgeProperty] = GraphXMacroRoadNetwork(sc, TestCostFunction).fromFile(networkFilePath).get
          for (vertexId <- result.vertices.map(_._1).toLocalIterator) {
            Seq(1, 2, 3) should contain (vertexId)
          }
          for (linkId <- result.edges.map(_.attr.id).toLocalIterator) {
            List("1", "2", "3") should contain (linkId)
          }
          // there are no flows assigned
          result.edges.map(_.attr.flow).sum should equal (0)
          // (the TestCostFunction returns 1, so this should be equal to |E|)
          result.edges.map(_.attr.linkCostFlow).sum should equal (3)
        }
      }
      "passed an invalid network file link" should {
        "throw an IOException" in {
          val thrown = the [java.io.IOException] thrownBy GraphXMacroRoadNetwork(sc, TestCostFunction).fromFile(missingFilePath)
          thrown getMessage() should startWith (s"$missingFilePath is not a valid network filename.")
        }
      }
    }
    "fromFileAndSnapshot" when {
      "G[3,3]: passed a file path for a valid MATSim network_v2.xml file and a snapshot_v1.xml file" should {
        "produce a correct GraphXMacroRoadNetwork, with loaded snapshot flows" in {
          val result: Graph[CoordinateVertexProperty, MacroscopicEdgeProperty] = GraphXMacroRoadNetwork(sc, TestCostFunction).fromFileAndSnapshot(networkFilePath, snapshotFilePath).get
          for (vertexId <- result.vertices.map(_._1).toLocalIterator) {
            Seq(1, 2, 3) should contain (vertexId)
          }
          for (linkId <- result.edges.map(_.attr.id).toLocalIterator) {
            List("1", "2", "3") should contain (linkId)
          }
          for (flow <- result.edges.map(_.attr.flow).toLocalIterator) {
            List(123, 456, 789) should contain (flow)
          }
        }
      }
      "G[15,23] test with sample network from the MATSim examples directory with a snapshop" should {
        "produce a correct GraphXMacroRoadNetwork, with loaded snapshot flows" in {
          val result: Graph[CoordinateVertexProperty, MacroscopicEdgeProperty] = GraphXMacroRoadNetwork(sc, TestCostFunction).fromFileAndSnapshot(equilNetworkFilePath, equilSnapshotFilePath).get
          val networkLinks: Map[String, (String, String)] =
            (XML.loadFile(equilNetworkFilePath) \ "links" \ "link").map(l => {
              ((l \ "@id").toString,
                ((l \ "@from").toString, (l \ "@to").toString))
            }).toMap
          val networkFlows: Map[String, String] =
            (XML.loadFile(equilSnapshotFilePath) \ "links" \ "link").map(l => {
              ((l \ "@id").toString, (l \ "@flow").toString)
            }).toMap

          val edgeSrcMap: Map[String, String] = result.edges.toLocalIterator.foldLeft(Map.empty[String, String])((map, e) => map ++ List((e.attr.id, e.srcId.toString)))
          val edgeDstMap: Map[String, String] = result.edges.toLocalIterator.foldLeft(Map.empty[String, String])((map, e) => map ++ List((e.attr.id, e.dstId.toString)))
          val edgeFlowMap: Map[String, String] = result.edges.toLocalIterator.foldLeft(Map.empty[String, String])((map, e) => map ++ List((e.attr.id, e.attr.flow.toInt.toString)))

          // just testing our mock data here - the cardinality and id-uniqueness of the edge set is confirmed
          networkFlows.keys.foreach(networkLinks.keys.toSeq should contain (_))
          // for each id, the source id should be consistent (link.value.first)
          networkLinks.foreach(l => l._2._1 should equal (edgeSrcMap(l._1)))
          // for each id, the destination id should be consistent (link.value.second)
          networkLinks.foreach(l => l._2._2 should equal (edgeDstMap(l._1)))
          // for each id, the flow should be consistent
          // (casted back to Int from Double above, as the file has it written without trailing decimal)
          networkFlows.foreach(l => l._2 should equal (edgeFlowMap(l._1)))
        }
      }
      "passed an invalid network file link" should {
        "throw an IOException" in {
          val thrown = the [java.io.IOException] thrownBy GraphXMacroRoadNetwork(sc, TestCostFunction).fromFileAndSnapshot(missingFilePath, snapshotFilePath)
          thrown getMessage() should startWith (s"$missingFilePath is not a valid network filename.")
        }
      }
      "passed a valid network file but invalid snapshot file" should {
        "throw an IOException" in {
          val thrown = the [java.io.IOException] thrownBy GraphXMacroRoadNetwork(sc, TestCostFunction).fromFileAndSnapshot(networkFilePath, missingFilePath)
          thrown getMessage() should startWith (s"$missingFilePath is not a valid snapshot filename.")
        }
      }
    }
  }
  "GraphXMacroRoadNetwork Object" when {
    "updateEdges" when {
      "called with a road network and a collection of paths" should {
        "increment the flows on the corresponding edges" in {
          val graph: RoadNetwork = Graph[CoordinateVertexProperty, MacroscopicEdgeProperty](
            sc.parallelize(List(
              (1, CoordinateVertexProperty(Euclidian(0,0))),
              (2, CoordinateVertexProperty(Euclidian(0,0))),
              (3, CoordinateVertexProperty(Euclidian(0,0))))),
            sc.parallelize(List(
              Edge(1,2,MacroscopicEdgeProperty("1", 5, BPRCostFunction(Map("capacity" -> "10", "freespeed" -> "25")))),
              Edge(2,3,MacroscopicEdgeProperty("2", 10, BPRCostFunction(Map("capacity" -> "10", "freespeed" -> "25")))))))
          val paths: Seq[(VertexId, VertexId, List[EdgeIdType])] = Seq((1,3,List("1", "2")))

          val result: RoadNetwork = GraphXMacroRoadNetwork.updateEdges(graph, paths)

          result.edges.toLocalIterator.foreach(edge => {
            if (edge.attr.id == "1") edge.attr.flow should equal (6)
            if (edge.attr.id == "2") edge.attr.flow should equal (11)
          })
        }
      }
    }
  }
}
