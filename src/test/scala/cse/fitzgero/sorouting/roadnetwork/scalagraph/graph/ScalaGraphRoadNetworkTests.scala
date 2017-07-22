//package cse.fitzgero.sorouting.roadnetwork.scalagraph.graph
//
//import cse.fitzgero.sorouting.SORoutingUnitTestTemplate
//import cse.fitzgero.sorouting.roadnetwork.scalagraph.edge._
//import cse.fitzgero.sorouting.roadnetwork.scalagraph.vertex._
//import cse.fitzgero.sorouting.roadnetwork.costfunction._
//import cse.fitzgero.sorouting.roadnetwork.scalagraph.graph.ScalaGraphRoadNetwork
//
//import scalax.collection.Graph
//
//class ScalaGraphRoadNetworkTests extends SORoutingUnitTestTemplate {
//  "ScalaGraphRoadNetwork" when {
//    val networkFilePath: String =       "src/test/resources/ScalaGraphRoadNetworkTests/network.xml"
//    val snapshotFilePath: String =      "src/test/resources/ScalaGraphRoadNetworkTests/snapshot.xml"
//    val equilNetworkFilePath: String =  "src/test/resources/ScalaGraphRoadNetworkTests/network-matsim-example-equil.xml"
//    val equilSnapshotFilePath: String = "src/test/resources/ScalaGraphRoadNetworkTests/snapshot-matsim-example-equil.xml"
//    val missingFilePath: String =       "src/test/resources/ScalaGraphRoadNetworkTests/blah-invalid.xml"
//    val testXML: xml.Elem =
//      <network>
//        <links>
//          <link id="1" from="1" to="2" freespeed="27.78" capacity="1000.0"/>
//          <link id="2" from="2" to="3" freespeed="27.78" capacity="2000.0"/>
//          <link id="3" from="3" to="1" freespeed="27.78" capacity="4000.0"/>
//        </links>
//        <nodes>
//          <node id="1" x="-10" y="-10"/>
//          <node id="2" x="-10" y="10"/>
//          <node id="3" x="10" y="5"/>
//        </nodes>
//      </network>
//    val testFlows: xml.Elem =
//      <network name="test flows">
//        <links>
//          <link id="1" flow="10"></link>
//          <link id="2" flow="20"></link>
//          <link id="3" flow="30"></link>
//        </links>
//      </network>
//    val badFlows: xml.Elem =
//      <network name="bad flows">
//        <links>
//          <link id="1"></link>
//          <link id="a" flow="abcd"></link>
//          <link id="true" flows="nose"></link>
//        </links>
//      </network>
//    val testFlowsMap: Map[EdgeIdType, Int] = Map(
//      ("1", 10),
//      ("2", 20),
//      ("3", 30)
//    )
//    "grabEdges" when {
//      "passed a valid xml.Elem object" should {
//        "produce a correct EdgeRDD" in {
//          val grabEdges = PrivateMethod[Seq[Edge[VertexId]]]('grabEdges)
//          val result: Seq[Edge[VertexId]] = ScalaGraphRoadNetwork(TestCostFunction) invokePrivate grabEdges(testXML, testFlows)
//          // confirm that the flow values in our graph are equivalent to those stored in the test object
//          result.foreach(edge => edge.attr.cost.zeroValue should equal (testFlowsMap(edge.attr.id)))
//          // confirm the TestCostFunction, which should be (x) => 1
//          result.map(edge => edge.attr.linkCostFlow).sum should equal (3)
//        }
//      }
//      "passed an xml.Elem object which has a network with links, but links are malformed (i.e. bad 'id' or 'flow' attributes)" should {
//        "throw an IOException" in {
//          val grabEdges = PrivateMethod[Seq[Edge[VertexId]]]('grabEdges)
//          val thrown = the [java.io.IOException] thrownBy {ScalaGraphRoadNetwork(TestCostFunction) invokePrivate grabEdges(testXML, badFlows)}
//          thrown getMessage() should startWith ("snapshot flow data was malformed")
//        }
//      }
//      "passed a real CostFunctionFactory, should calculate cost values which are different than the flow counts" in {
//        val grabEdges = PrivateMethod[Seq[Edge[VertexId]]]('grabEdges)
//        val result: Seq[Edge[VertexId]] = ScalaGraphRoadNetwork(BPRCostFunction) invokePrivate grabEdges(testXML, testFlows)
//        // confirm that the costFlow function result is never the same as the flow amount
//        // (a simple check that applying the function yielded a different number)
//        result.map(edge => edge.attr.linkCostFlow != edge.attr.flow).reduce(_&&_) should equal (true)
//      }
//    }
//    "grabVertices" when {
//      "passed a valid xml.Elem object" should {
//        "produce a correct VertexRDD" in {
//          val grabVertices = PrivateMethod[Seq[(VertexId, CoordinateVertexProperty)]]('grabVertices)
//          val result: Seq[(VertexId, CoordinateVertexProperty)] = ScalaGraphRoadNetwork(TestCostFunction) invokePrivate grabVertices(testXML)
//          val makeLocal: Map[String, CoordinateVertexProperty] = result.map(vertex => (vertex._1.toString, vertex._2)).toMap
//          makeLocal("1").position should equal (Euclidian(-10, -10))
//          makeLocal("2").position should equal (Euclidian(-10, 10))
//          makeLocal("3").position should equal (Euclidian(10, 5))
//        }
//      }
//    }
//    "fromFile" when {
//      "passed a file path for a valid MATSim network_v2.xml file" should {
//        "produce a correct GraphXMacroRoadNetwork, with all flows equal to 0" in {
//          val result: Graph[VertexId, Edge] = ScalaGraphRoadNetwork(TestCostFunction).fromFile(networkFilePath).get
//          for (vertexId <- result. .vertices.map(_._1).toLocalIterator) {
//            Seq(1, 2, 3) should contain (vertexId)
//          }
//          for (linkId <- result.edges.map(_.attr.id).toLocalIterator) {
//            List("1", "2", "3") should contain (linkId)
//          }
//          // there are no flows assigned
//          result.edges.map(_.attr.flow).sum should equal (0)
//          // (the TestCostFunction returns 1, so this should be equal to |E|)
//          result.edges.map(_.attr.linkCostFlow).sum should equal (3)
//        }
//      }
//      "passed an invalid network file link" should {
//        "throw an IOException" in {
//          val thrown = the [java.io.IOException] thrownBy GraphXMacroRoadNetwork(sc, TestCostFunction).fromFile(missingFilePath)
//          thrown getMessage() should startWith (s"$missingFilePath is not a valid network filename.")
//        }
//      }
//    }
//  }
//}
