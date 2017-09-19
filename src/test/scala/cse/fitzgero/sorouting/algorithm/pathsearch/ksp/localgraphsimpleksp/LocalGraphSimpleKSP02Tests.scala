package cse.fitzgero.sorouting.algorithm.pathsearch.ksp.localgraphsimpleksp

import scala.collection.GenSeq
import cse.fitzgero.sorouting.SORoutingUnitTestTemplate
import cse.fitzgero.sorouting.algorithm.pathsearch.ksp.{PathsFoundBounds, TimeBounds}
import cse.fitzgero.sorouting.algorithm.pathsearch.od.localgraph._
import cse.fitzgero.sorouting.roadnetwork.costfunction.BPRCostFunction
import cse.fitzgero.sorouting.roadnetwork.localgraph.{EdgeMATSim, LocalGraphMATSim, LocalGraphMATSimFactory, VertexMATSim}
import cse.fitzgero.sorouting.util.implicits._


import scala.concurrent.Future
import scala.util.{Failure, Success}

class LocalGraphSimpleKSP02Tests extends SORoutingUnitTestTemplate {
  "SimpleKSP" when {
    val networkFilePath: String =       "src/test/resources/SimpleKSPTests/ksp-simple-alternate-routes-network.xml"
    val snapshotFilePath: String =      "src/test/resources/SimpleKSPTests/ksp-simple-alternate-routes-snapshot.xml"
    val ryeNetworkFilePath: String =    "src/main/resources/matsimNetworks/RyeNetwork.xml"
    "kShortestPaths" when {
      "called with a graph and an od pair and k = 4" should {
        "find four alternative paths and return them ordered by total cost" in {
          val graph: LocalGraphMATSim = LocalGraphMATSimFactory(BPRCostFunction, AlgorithmFlowRate = 10).fromFileAndSnapshot(networkFilePath, snapshotFilePath).get
          val ksp = LocalGraphSimpleKSP02[LocalGraphMATSim, VertexMATSim, EdgeMATSim]()
          val result: GenSeq[LocalGraphODPath] = ksp.kShortestPaths(graph, LocalGraphODPairByVertex("",1L, 11L), 3).paths
          result.head.path should equal (List("1-3", "3-5", "5-9", "9-8", "8-11"))
          result.tail.head.path should equal (List("1-3", "3-5", "5-9", "9-10", "10-11"))
          result.tail.tail.head.path should equal (List("1-3", "3-5", "5-9", "9-8", "8-10", "10-11"))
        }
      }
      "called with a graph, an od pair, and k is much greater than the length (size of edge set) of the shortest path" should {
        "find as many alternative paths as the length of the shortest path plus one" in {
          val graph: LocalGraphMATSim = LocalGraphMATSimFactory(BPRCostFunction, AlgorithmFlowRate = 10).fromFileAndSnapshot(networkFilePath, snapshotFilePath).get
          val ksp = LocalGraphSimpleKSP02[LocalGraphMATSim, VertexMATSim, EdgeMATSim]()
          val result: GenSeq[LocalGraphODPath] = ksp.kShortestPaths(graph, LocalGraphODPairByVertex("",1L, 11L), 100).paths
          // should result in shortestPath.length + 1 distinct paths
          result.distinct.size should equal (result.head.path.size + 1)
        }
      }
      "called with a large road network and k is very small compared to the possible number of alternative paths" should {
        "find ten paths in descending cost order which are more diverse than of KSP algorithm 01" in {
          val graph: LocalGraphMATSim = LocalGraphMATSimFactory(BPRCostFunction, AlgorithmFlowRate = 10).fromFile(ryeNetworkFilePath).get.par
          val ksp01 = LocalGraphSimpleKSP01[LocalGraphMATSim, VertexMATSim, EdgeMATSim]()
          val ksp02 = LocalGraphSimpleKSP02[LocalGraphMATSim, VertexMATSim, EdgeMATSim]()
          val result01: GenSeq[LocalGraphODPath] = ksp01.kShortestPaths(graph, LocalGraphODPairByVertex("", 2292029039L, 254224738L), 10, TimeBounds(60 seconds)).paths
          val result02: GenSeq[LocalGraphODPath] = ksp02.kShortestPaths(graph, LocalGraphODPairByVertex("", 2292029039L, 254224738L), 10, TimeBounds(60 seconds)).paths

//          result01.distinct.size should equal (10)
//          result01.iterator.sliding(2).foreach(pair => pair(0).cost.sum should be <= pair(1).cost.sum)

          result01.foreach(result => println(f"${result.cost.sum}%03f ${result.path}"))
          println
          result02.foreach(result => println(f"${result.cost.sum}%03f ${result.path}"))

          val res2DistinctEdges =
            result02
              .flatMap(_.path)
              .distinct
              .size
          val res1DistinctEdges =
            result01
              .take(result02.size) // compare the same number of paths
              .flatMap(_.path)
              .distinct
              .size
          res1DistinctEdges should be < res2DistinctEdges
          println(s"ksp01 produced $res1DistinctEdges distinct edges; ksp02 produced $res2DistinctEdges distinct edges")
          println("ksp02 should have a larger set of distinct edges by the minimum overlap requirement")
        }
      }
//      "called with a large road network, setting a PathFoundBounds to 20" should {
//        "find ten paths" in {
//          val graph: LocalGraphMATSim = LocalGraphMATSimFactory(BPRCostFunction, AlgorithmFlowRate = 10).fromFile(ryeNetworkFilePath).get.par
//          val ksp = LocalGraphSimpleKSP02[LocalGraphMATSim, VertexMATSim, EdgeMATSim]()
//          val result: GenSeq[LocalGraphODPath] = ksp.kShortestPaths(graph, LocalGraphODPairByVertex("", 2292029039L, 254224738L), 10, PathsFoundBounds(20)).paths
//
//          result.distinct.size should equal (10)
//          result.iterator.sliding(2).foreach(pair => pair(0).cost.sum should be <= pair(1).cost.sum)
//        }
//      }
    }
  }
}
