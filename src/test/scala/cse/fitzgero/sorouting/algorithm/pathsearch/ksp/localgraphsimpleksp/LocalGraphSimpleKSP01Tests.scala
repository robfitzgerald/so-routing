package cse.fitzgero.sorouting.algorithm.pathsearch.ksp.localgraphsimpleksp

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate
import cse.fitzgero.sorouting.algorithm.pathsearch.ksp.PathsFoundBounds
import cse.fitzgero.sorouting.algorithm.pathsearch.od.localgraph._
import cse.fitzgero.sorouting.roadnetwork.costfunction.{BPRCostFunction, TestCostFunction}
import cse.fitzgero.sorouting.roadnetwork.localgraph.{EdgeMATSim, LocalGraphMATSim, LocalGraphMATSimFactory, VertexMATSim}

import scala.collection.GenSeq

class LocalGraphSimpleKSP01Tests extends SORoutingUnitTestTemplate {
  "SimpleKSP" when {
    val networkFilePath: String =       "src/test/resources/SimpleKSPTests/ksp-simple-alternate-routes-network.xml"
    val snapshotFilePath: String =      "src/test/resources/SimpleKSPTests/ksp-simple-alternate-routes-snapshot.xml"
    val ryeNetworkFilePath: String =    "src/main/resources/matsimNetworks/RyeNetwork.xml"
    "kShortestPaths" when {
      "called with a graph and an od pair and k = 4" should {
        "find four alternative paths and return them ordered by total cost" in {
          val graph: LocalGraphMATSim = LocalGraphMATSimFactory(BPRCostFunction, AlgorithmFlowRate = 10).fromFileAndSnapshot(networkFilePath, snapshotFilePath).get
          val ksp = LocalGraphSimpleKSP01[LocalGraphMATSim, VertexMATSim, EdgeMATSim]()
          val result: GenSeq[LocalGraphODPath] = ksp.kShortestPaths(graph, LocalGraphODPairByVertex("",1L, 11L), 3).paths
          result.head.path should equal (List("1-3", "3-5", "5-9", "9-8", "8-11"))
          result.tail.head.path should equal (List("1-3", "3-5", "5-9", "9-10", "10-11"))
          result.tail.tail.head.path should equal (List("1-3", "3-5", "5-9", "9-8", "8-10", "10-11"))
        }
      }
      "called with a graph, an od pair, and k is much greater than the length (size of edge set) of the shortest path" should {
        "find as many alternative paths as the length of the shortest path plus one" in {
          val graph: LocalGraphMATSim = LocalGraphMATSimFactory(BPRCostFunction, AlgorithmFlowRate = 10).fromFileAndSnapshot(networkFilePath, snapshotFilePath).get
          val ksp = LocalGraphSimpleKSP01[LocalGraphMATSim, VertexMATSim, EdgeMATSim]()
          val result: GenSeq[LocalGraphODPath] = ksp.kShortestPaths(graph, LocalGraphODPairByVertex("",1L, 11L), 100).paths
          // should result in shortestPath.length + 1 distinct paths
          result.distinct.size should equal (result.head.path.size + 1)
        }
      }
      "called with a large road network and k is very small compared to the possible number of alternative paths" should {
        "find ten paths in descending cost order" in {
          val graph: LocalGraphMATSim = LocalGraphMATSimFactory(BPRCostFunction, AlgorithmFlowRate = 10).fromFile(ryeNetworkFilePath).get.par
          val ksp = LocalGraphSimpleKSP01[LocalGraphMATSim, VertexMATSim, EdgeMATSim]()
          val result: GenSeq[LocalGraphODPath] = ksp.kShortestPaths(graph, LocalGraphODPairByVertex("", 2292029039L, 254224738L), 10).paths

          result.distinct.size should equal (10)
          result.iterator.sliding(2).foreach(pair => pair(0).cost.sum should be <= pair(1).cost.sum)
        }
      }
      "called with a large road network, setting a PathFoundBounds to 20" should {
        "find ten paths" in {
          val graph: LocalGraphMATSim = LocalGraphMATSimFactory(BPRCostFunction, AlgorithmFlowRate = 10).fromFile(ryeNetworkFilePath).get.par
          val ksp = LocalGraphSimpleKSP01[LocalGraphMATSim, VertexMATSim, EdgeMATSim]()
          val result: GenSeq[LocalGraphODPath] = ksp.kShortestPaths(graph, LocalGraphODPairByVertex("", 2292029039L, 254224738L), 10, PathsFoundBounds(20)).paths

          result.distinct.size should equal (10)
          result.iterator.sliding(2).foreach(pair => pair(0).cost.sum should be <= pair(1).cost.sum)
        }
      }
    }
  }
}
