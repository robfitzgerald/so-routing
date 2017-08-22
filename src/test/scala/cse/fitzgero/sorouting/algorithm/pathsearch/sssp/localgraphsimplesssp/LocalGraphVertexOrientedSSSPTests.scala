package cse.fitzgero.sorouting.algorithm.pathsearch.sssp.localgraphsimplesssp

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate
import cse.fitzgero.sorouting.algorithm.pathsearch.od.localgraph._
import cse.fitzgero.sorouting.roadnetwork.costfunction.{BPRCostFunction, TestCostFunction}
import cse.fitzgero.sorouting.roadnetwork.localgraph._

class LocalGraphVertexOrientedSSSPTests extends SORoutingUnitTestTemplate {
  "LocalGraphVertexOrientedSSSP" when {
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
          val sssp = LocalGraphVertexOrientedSSSP[LocalGraphMATSim, VertexMATSim, EdgeMATSim]()
          val result: LocalGraphODPath = sssp.shortestPath(graph, LocalGraphODPairByVertex("", 1L, 3L))
          result.path should equal (List("1", "2"))
//          println(result.toString)
        }
      }
      "run on 11-vertex test network made for the ksp problem on k=3" should {
        "find the solution" in {
          val graph: LocalGraphMATSim = LocalGraphMATSimFactory(BPRCostFunction, AlgorithmFlowRate = 10).fromFileAndSnapshot(kspNetworkFilePath, kspSnapshotFilePath).get
          val sssp = LocalGraphVertexOrientedSSSP[LocalGraphMATSim, VertexMATSim, EdgeMATSim]()
          graph.edgeAttrs.foreach(println)
          val result: LocalGraphODPath = sssp.shortestPath(graph, LocalGraphODPairByVertex("", 1L, 11L))
          result.path should equal (List("1-4", "4-6", "6-9", "9-11"))
          //          println(result.toString)
        }
      }
    }
    "_backPropagate" when {
      "called with a set of nodes with calculated distance values" should {
        "find the path through the edges associated with those nodes" in {
          val sssp = new LocalGraphVertexOrientedSSSP[LocalGraphMATSim, VertexMATSim, EdgeMATSim]
          val aSolution = Map(
            3L -> SimpleSSSP_SearchNode(π("102", 2L, 5D), 5D),
            2L -> SimpleSSSP_SearchNode(π("101", 1L, 5D), 10D),
            1L -> SimpleSSSP_SearchNode(Origin, 0D)
          )
          val goalVertex: VertexId = 3L
          val result: List[(EdgeId, Double)] = sssp._backPropagate(aSolution)(goalVertex)
          val path = result.map(_._1)
          val totalCost = result.map(_._2).sum
          path should equal (List("101", "102"))
          totalCost should equal (10D)
        }
      }
    }
    "djikstrasAlgorithm" when {
      "called with an itty bitty triangle graph and valid od pair" should {
        "produce the shortest path" in {
          val graph: LocalGraphMATSim = LocalGraphMATSimFactory(TestCostFunction).fromFile(networkFilePath).get
          val odPair = LocalGraphODPairByVertex("", 1L, 3L)
          val sssp = new LocalGraphVertexOrientedSSSP[LocalGraphMATSim, VertexMATSim, EdgeMATSim]
          val result = sssp.djikstrasAlgorithm(graph, odPair)
          result.cost.sum should equal (2D)
          result.path should equal (List("1", "2"))
        }
      }
      "called with an itty bitty triangle graph, snapshot, and valid od pair" should {
        "produce the shortest path" in {
          val graph: LocalGraphMATSim = LocalGraphMATSimFactory(BPRCostFunction, AlgorithmFlowRate = 10).fromFileAndSnapshot(networkFilePath, snapshotFilePath).get
          val odPair = LocalGraphODPairByVertex("", 1L, 3L)
          val sssp = new LocalGraphVertexOrientedSSSP[LocalGraphMATSim, VertexMATSim, EdgeMATSim]
          val result = sssp.djikstrasAlgorithm(graph, odPair)
          result.cost.sum should be > 55D
          result.path should equal (List("1", "2"))
        }
      }
      "called with the MATSim Equil network and valid od pair" should {
        "produce the shortest path" in {
          val graph: LocalGraphMATSim = LocalGraphMATSimFactory(TestCostFunction).fromFileAndSnapshot(equilNetworkFilePath, equilSnapshotFilePath).get
          val odPair = LocalGraphODPairByVertex("", 1L, 15L)
          val sssp = new LocalGraphVertexOrientedSSSP[LocalGraphMATSim, VertexMATSim, EdgeMATSim]
          val result = sssp.djikstrasAlgorithm(graph, odPair)

          // there is more than one correct solution. if this fails, consider coming up with a test that reflects this.
          result.path should equal (List("1", "8", "17", "20", "21", "22"))
        }
      }
    }
  }
}
