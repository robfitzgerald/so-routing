package cse.fitzgero.sorouting.roadnetwork.localgraph

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate
import cse.fitzgero.sorouting.roadnetwork.costfunction.TestCostFunction
import cse.fitzgero.sorouting.roadnetwork.scalagraph.edge.MacroscopicEdgeProperty
import cse.fitzgero.sorouting.roadnetwork.scalagraph.vertex.CoordinateVertexProperty

class LocalGraphMATSimFactoryTests extends SORoutingUnitTestTemplate {
  "LocalGraphMATSimFactory" when {
    val networkFilePath: String =       "src/test/resources/LocalGraphMATSimFactoryTests/network.xml"
    val snapshotFilePath: String =      "src/test/resources/LocalGraphMATSimFactoryTests/snapshot.xml"
    val equilNetworkFilePath: String =  "src/test/resources/LocalGraphMATSimFactoryTests/network-matsim-example-equil.xml"
    val equilSnapshotFilePath: String = "src/test/resources/LocalGraphMATSimFactoryTests/snapshot-matsim-example-equil.xml"
    val missingFilePath: String =       "src/test/resources/LocalGraphMATSimFactoryTests/blah-invalid.xml"
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
    "fromFile" when {
      "called with a valid network file url" should {
        "build a graph from that file" in {
          val result = LocalGraphMATSimFactory(TestCostFunction).fromFile(networkFilePath).get
          result.vertices.toList.sorted should equal (List(1L, 2L, 3L))
          result.edges.toList.sorted should equal (List("1", "2", "3"))
          result.adjacencyList(1L) should equal (Map[EdgeId, VertexId]("1" -> 2L))
          result.adjacencyList(2L) should equal (Map[EdgeId, VertexId]("2" -> 3L))
          result.adjacencyList(3L) should equal (Map[EdgeId, VertexId]("3" -> 1L))
        }
      }
    }
    "fromFileAndSnapshot" when {
      "called with valid network file and snapshot file urls" should {
        "build a graph with edge flows" in {
          val result = LocalGraphMATSimFactory(TestCostFunction).fromFileAndSnapshot(networkFilePath, snapshotFilePath).get
          result.edgeAttrOf("1").get.cost.snapshotFlow should equal (123.0D)
          result.edgeAttrOf("2").get.cost.snapshotFlow should equal (456.0D)
          result.edgeAttrOf("3").get.cost.snapshotFlow should equal (789.0D)
        }
      }
    }
    "grabVertices" when {
      "passed a valid network file" should {
        "create a LocalGraph instance which has all vertices described in the network file" in {
          val grabVertices = PrivateMethod[LocalGraphMATSim]('grabVertices)
          val emptyGraph = LocalGraph[CoordinateVertexProperty, MacroscopicEdgeProperty]()
          val result: LocalGraphMATSim = LocalGraphMATSimFactory(TestCostFunction) invokePrivate grabVertices(emptyGraph, testXML)
          result.vertices.toList should equal (List(1L, 2L, 3L))
        }
      }
    }
  }
}
