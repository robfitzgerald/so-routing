package cse.fitzgero.sorouting.algorithm.pathsearch.ksp.localgraph.simpleksp

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate
import cse.fitzgero.sorouting.algorithm.pathsearch.ksp.PathsFoundBounds
import cse.fitzgero.sorouting.algorithm.pathsearch.ksp.localgraphsimpleksp.SimpleKSP
import cse.fitzgero.sorouting.algorithm.pathsearch.od.localgraph._
import cse.fitzgero.sorouting.roadnetwork.costfunction.{BPRCostFunction, TestCostFunction}
import cse.fitzgero.sorouting.roadnetwork.localgraph.{EdgeMATSim, LocalGraphMATSim, LocalGraphMATSimFactory, VertexMATSim}

import scala.collection.GenSeq

class SimpleKSPTests extends SORoutingUnitTestTemplate {
  "SimpleKSP" when {
    val networkFilePath: String =       "src/test/resources/SimpleKSPTests/ksp-simple-alternate-routes-network.xml"
    val snapshotFilePath: String =      "src/test/resources/SimpleKSPTests/ksp-simple-alternate-routes-snapshot.xml"
    val ryeNetworkFilePath: String =    "src/main/resources/matsimNetworks/RyeNetwork.xml"
    "kShortestPaths" when {
      "called with a graph and an od pair and k = 4" should {
        "find four alternative paths and return them ordered by total cost" in {
          val graph: LocalGraphMATSim = LocalGraphMATSimFactory(BPRCostFunction, 3600D, 10D).fromFileAndSnapshot(networkFilePath, snapshotFilePath).get
          val ksp = SimpleKSP[LocalGraphMATSim, VertexMATSim, EdgeMATSim]()
          val result: GenSeq[LocalGraphODPath] = ksp.kShortestPaths(graph, LocalGraphODPair(1L, 11L), 3)
          result.head.path should equal (List("1-3", "3-5", "5-9", "9-8", "8-11"))
          result.tail.head.path should equal (List("1-3", "3-5", "5-9", "9-10", "10-11"))
          result.tail.tail.head.path should equal (List("1-3", "3-5", "5-9", "9-8", "8-10", "10-11"))
        }
      }
      "called with a graph, an od pair, and k is much greater than the length (size of edge set) of the shortest path" should {
        "find as many alternative paths as the length of the shortest path plus one" in {
          val graph: LocalGraphMATSim = LocalGraphMATSimFactory(BPRCostFunction, 3600D, 10D).fromFileAndSnapshot(networkFilePath, snapshotFilePath).get
          val ksp = SimpleKSP[LocalGraphMATSim, VertexMATSim, EdgeMATSim]()
          val result: GenSeq[LocalGraphODPath] = ksp.kShortestPaths(graph, LocalGraphODPair(1L, 11L), 100)
          // should result in shortestPath.length + 1 distinct paths
          result.distinct.size should equal (result.head.path.size + 1)
        }
      }
      "called with a large road network and k is very small compared to the possible number of alternative paths" should {
        "find ten paths" in {
          val graph: LocalGraphMATSim = LocalGraphMATSimFactory(BPRCostFunction, 3600D, 10D).fromFile(ryeNetworkFilePath).get.par
          val ksp = SimpleKSP[LocalGraphMATSim, VertexMATSim, EdgeMATSim]()
          val result: GenSeq[LocalGraphODPath] = ksp.kShortestPaths(graph, LocalGraphODPair(2292029039L, 254874068L), 10)
          // should result in 10 distinct paths
          result.distinct.size should equal (10)
          result.foreach(solution => {
            solution.path.head should equal ()
          })
          result.foreach(odPath => println(s"${odPath.cost.sum} ${odPath.path}"))
        }
      }
      "called with a large road network, setting a PathFoundBounds to 20" should {
        "find ten paths" in {
          val graph: LocalGraphMATSim = LocalGraphMATSimFactory(BPRCostFunction, 3600D, 10D).fromFile(ryeNetworkFilePath).get.par
          val ksp = SimpleKSP[LocalGraphMATSim, VertexMATSim, EdgeMATSim]()
          val result: GenSeq[LocalGraphODPath] = ksp.kShortestPaths(graph, LocalGraphODPair(2292029039L, 254874068L), 10, PathsFoundBounds(20))
          // should result in 10 distinct paths
          result.distinct.size should equal (10)
          result.foreach(odPath => println(s"${odPath.cost.sum} ${odPath.path}"))
        }
      }
    }
  }
}
