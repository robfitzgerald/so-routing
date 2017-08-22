package cse.fitzgero.sorouting.algorithm.pathsearch.sssp.localgraphsimplesssp

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate
import cse.fitzgero.sorouting.algorithm.pathsearch.od.localgraph._
import cse.fitzgero.sorouting.roadnetwork.costfunction.{BPRCostFunction, TestCostFunction}
import cse.fitzgero.sorouting.roadnetwork.localgraph._

class LocalGraphMATSimSSSPTests extends SORoutingUnitTestTemplate {
  "LocalGraphMATSimSSSP" when {
    val networkFilePath: String =       "src/test/resources/LocalGraphMATSimFactoryTests/network.xml"
    val snapshotFilePath: String =      "src/test/resources/LocalGraphMATSimFactoryTests/snapshot.xml"
    val equilNetworkFilePath: String =  "src/test/resources/LocalGraphMATSimFactoryTests/network-matsim-example-equil.xml"
    val equilSnapshotFilePath: String = "src/test/resources/LocalGraphMATSimFactoryTests/snapshot-matsim-example-equil.xml"
    val kspNetworkFilePath: String =    "src/test/resources/SimpleSSSPTests/ksp-3-routes-network.xml"
    val kspSnapshotFilePath: String =    "src/test/resources/SimpleSSSPTests/ksp-3-routes-snapshot.xml"
    val missingFilePath: String =       "src/test/resources/LocalGraphMATSimFactoryTests/blah-invalid.xml"
    "shortestPath" when {
      "run on two vertices in a 3 vertex network with one solution" should {
        "find that solution" in {
          val graph: LocalGraphMATSim = LocalGraphMATSimFactory(TestCostFunction).fromFile(networkFilePath).get
          val sssp = LocalGraphMATSimSSSP()
          val result: LocalGraphODPath = sssp.shortestPath(graph, LocalGraphODPairByEdge("", "1", "3"))
          result.path should equal (List("1", "2", "3"))
        }
      }
      "asking for the shortest path where the start edge and end edge are the same" should {
        "produce an empty path result" in {
          val graph: LocalGraphMATSim = LocalGraphMATSimFactory(TestCostFunction).fromFile(networkFilePath).get
          val sssp = LocalGraphMATSimSSSP()
          val result: LocalGraphODPath = sssp.shortestPath(graph, LocalGraphODPairByEdge("", "1", "1"))
          result.path should equal (List())
          result.cost should equal (List())
        }
      }
      "asking for the shortest path where the shortest path has a length of two" should {
        "correctly return that path" in {
          // confirms the logic branch that solves this sub-problem without calling the vertex-oriented SSSP algorithm
          val graph: LocalGraphMATSim = LocalGraphMATSimFactory(TestCostFunction).fromFile(networkFilePath).get
          val sssp = LocalGraphMATSimSSSP()
          val result: LocalGraphODPath = sssp.shortestPath(graph, LocalGraphODPairByEdge("", "1", "2"))
          result.path should equal (List("1", "2"))
          result.cost.length should equal (2)
        }
      }
      "run on 11-vertex test network made for the ksp problem on k=3" should {
        "find the solution" in {
          val graph: LocalGraphMATSim = LocalGraphMATSimFactory(BPRCostFunction, AlgorithmFlowRate = 10).fromFileAndSnapshot(kspNetworkFilePath, kspSnapshotFilePath).get
          val sssp = LocalGraphMATSimSSSP()
          graph.edgeAttrs.foreach(println)
          val result: LocalGraphODPath = sssp.shortestPath(graph, LocalGraphODPairByEdge("", "1-4", "9-11"))
          result.path should equal (List("1-4", "4-6", "6-9", "9-11"))
        }
      }
      "request has invalid request" ignore {
        "produce an empty solution" in {
          val graph: LocalGraphMATSim = LocalGraphMATSimFactory(TestCostFunction).fromFile(networkFilePath).get
          val sssp = LocalGraphMATSimSSSP()
          val result: LocalGraphODPath = sssp.shortestPath(graph, LocalGraphODPairByEdge("", "4", "7"))
          result.path should equal (List())
          result.cost should equal (List())
        }
      }
    }
  }
}
