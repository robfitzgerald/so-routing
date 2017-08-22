package cse.fitzgero.sorouting.algorithm.pathsearch.ksp.localgraph.simpleksp

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate
import cse.fitzgero.sorouting.algorithm.pathsearch.ksp.PathsFoundBounds
import cse.fitzgero.sorouting.algorithm.pathsearch.ksp.localgraphsimpleksp.{LocalGraphMATSimKSP, LocalGraphSimpleKSP}
import cse.fitzgero.sorouting.algorithm.pathsearch.od.localgraph._
import cse.fitzgero.sorouting.roadnetwork.costfunction.BPRCostFunction
import cse.fitzgero.sorouting.roadnetwork.localgraph.{EdgeMATSim, LocalGraphMATSim, LocalGraphMATSimFactory, VertexMATSim}

import scala.collection.GenSeq

class LocalGraphMATSimKSPTests extends SORoutingUnitTestTemplate {
  "LocalGraphMATSimKSPTests" when {
    val networkFilePath: String =       "src/test/resources/SimpleKSPTests/ksp-simple-alternate-routes-network.xml"
    val snapshotFilePath: String =      "src/test/resources/SimpleKSPTests/ksp-simple-alternate-routes-snapshot.xml"
    val limitedAltsPath: String =       "src/test/resources/SimpleKSPTests/ksp-simple-only-2-possible.xml"
    val ryeNetworkFilePath: String =    "src/main/resources/matsimNetworks/RyeNetwork.xml"
    "kShortestPaths" when {
      "called with a graph and an od pair and k = 4" should {
        "find four alternative paths and return them ordered by total cost" in {
          val graph: LocalGraphMATSim = LocalGraphMATSimFactory(BPRCostFunction, AlgorithmFlowRate = 10).fromFileAndSnapshot(networkFilePath, snapshotFilePath).get
          val ksp = LocalGraphMATSimKSP()
          val result: GenSeq[LocalGraphODPath] = ksp.kShortestPaths(graph, LocalGraphODPairByEdge("","0-1", "11-100"), 4)
          result.foreach(println)
          result.head.path should equal (List("0-1", "1-3", "3-5", "5-9", "9-8", "8-11", "11-100"))
          result.tail.head.path should equal (List("0-1", "1-3", "3-5", "5-9", "9-10", "10-11", "11-100"))
          result.tail.tail.head.path should equal (List("0-1", "1-3", "3-5", "5-9", "9-8", "8-10", "10-11", "11-100"))
          result.tail.tail.tail.head.path should equal (List("0-1", "1-3", "3-6", "6-10", "10-11", "11-100"))
        }
      }
      "called with a graph, an od pair, and k is much greater than the length (size of edge set) of the shortest path" should {
        "find as many alternative paths as the length of the shortest path plus one" in {
          val graph: LocalGraphMATSim = LocalGraphMATSimFactory(BPRCostFunction, AlgorithmFlowRate = 10).fromFileAndSnapshot(networkFilePath, snapshotFilePath).get
          val ksp = LocalGraphMATSimKSP()
          val result: GenSeq[LocalGraphODPath] = ksp.kShortestPaths(graph, LocalGraphODPairByEdge("","0-1", "11-100"), 100)
          // the head and tail of the shortest path are non-negotiable in the edge-oriented path search;
          result.foreach(println)
          result.distinct.size should equal (result.head.path.size + 1)
        }
      }
      "called with a network where the path length and k are both greater than the number of possible alternate paths (by this ksp method)" should {
        "limit its selection to two" in {
          val graph: LocalGraphMATSim = LocalGraphMATSimFactory(BPRCostFunction, AlgorithmFlowRate = 10).fromFile(limitedAltsPath).get
          val ksp = LocalGraphMATSimKSP()
          val result: GenSeq[LocalGraphODPath] = ksp.kShortestPaths(graph, LocalGraphODPairByEdge("","0-1", "11-100"), 100)
          // the head and tail of the shortest path are non-negotiable in the edge-oriented path search;
//          result.size should be (2)
          result.foreach(_.cost.sum should be < Double.PositiveInfinity)
          result.foreach(println)
        }

      }
      "called with a large road network and k is very small compared to the possible number of alternative paths" should {
        "find ten paths" in {
          val graph: LocalGraphMATSim = LocalGraphMATSimFactory(BPRCostFunction, AlgorithmFlowRate = 10).fromFile(ryeNetworkFilePath).get.par
          val ksp = LocalGraphMATSimKSP()
          val result: GenSeq[LocalGraphODPath] = ksp.kShortestPaths(graph, LocalGraphODPairByEdge("", "295023436_0", "23537360_0_r"), 10)
          // should result in 10 distinct paths
          result.distinct.size should equal (10)
          // can we test the values of the path somehow?
//          result.foreach(solution => {
//            solution.path.head should equal ()
//          })
          result.foreach(odPath => println(s"${odPath.cost.sum} ${odPath.path}"))
        }
      }
      "called with a large road network, setting a PathFoundBounds to 20" should {
        "find ten paths" in {
          val graph: LocalGraphMATSim = LocalGraphMATSimFactory(BPRCostFunction, AlgorithmFlowRate = 10).fromFile(ryeNetworkFilePath).get.par
          val ksp = LocalGraphMATSimKSP()
          val result: GenSeq[LocalGraphODPath] = ksp.kShortestPaths(graph, LocalGraphODPairByEdge("", "295023436_0", "23537360_0_r"), 10, PathsFoundBounds(20))
          // should result in 10 distinct paths
          result.distinct.size should equal (10)
          result.foreach(odPath => println(s"${odPath.cost.sum} ${odPath.path}"))
        }
      }
    }
  }
}
