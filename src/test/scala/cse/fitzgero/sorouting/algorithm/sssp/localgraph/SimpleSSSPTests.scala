package cse.fitzgero.sorouting.algorithm.sssp.localgraph

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate
import cse.fitzgero.sorouting.algorithm.sssp.SSSP
import cse.fitzgero.sorouting.roadnetwork.costfunction.{BPRCostFunction, TestCostFunction}
import cse.fitzgero.sorouting.roadnetwork.localgraph._
import cse.fitzgero.sorouting.roadnetwork.localgraph.edge._
import cse.fitzgero.sorouting.roadnetwork.localgraph.vertex._

class SimpleSSSPTests extends SORoutingUnitTestTemplate {
  "SimpleSSSP" when {
    val networkFilePath: String =       "src/test/resources/LocalGraphMATSimFactoryTests/network.xml"
    val snapshotFilePath: String =      "src/test/resources/LocalGraphMATSimFactoryTests/snapshot.xml"
    val equilNetworkFilePath: String =  "src/test/resources/LocalGraphMATSimFactoryTests/network-matsim-example-equil.xml"
    val equilSnapshotFilePath: String = "src/test/resources/LocalGraphMATSimFactoryTests/snapshot-matsim-example-equil.xml"
    val missingFilePath: String =       "src/test/resources/LocalGraphMATSimFactoryTests/blah-invalid.xml"
    "shortestPath" when {
      "run on two vertices in a 3 vertex network with one solution" should {
        "find that solution" in {
          val graph: LocalGraph[CoordinateVertexProperty,MacroscopicEdgeProperty] = LocalGraphMATSimFactory.fromFile(networkFilePath).get
          val sssp: SSSP[LocalGraph[CoordinateVertexProperty,MacroscopicEdgeProperty], SimpleSSSP_ODPair, SimpleSSSP_ODPath] =
            SimpleSSSP[CoordinateVertexProperty, MacroscopicEdgeProperty]()
          val result: SimpleSSSP_ODPath = sssp.shortestPath(graph, SimpleSSSP_ODPair(1L, 3L))
          result.path should equal (List(1L, 2L))
          println(result.toString)
        }
      }
    }
    "_backPropagate" when {
      "called with a set of nodes with calculated distance values" should {
        "find the path through the edges associated with those nodes" in {
          val sssp = new SimpleSSSP[CoordinateVertexProperty, MacroscopicEdgeProperty]
          val aSolution = Map(
            3L -> SimpleSSSP_SearchNode(π(102L, 2L), 5D),
            2L -> SimpleSSSP_SearchNode(π(101L, 1L), 5D),
            1L -> SimpleSSSP_SearchNode(Origin, 0D)
          )
          val goalVertex: VertexId = 3L
          val result: List[(EdgeId, Double)] = sssp._backPropagate(aSolution)(goalVertex)
          val path = result.map(_._1)
          val totalCost = result.map(_._2).sum
          path should equal (List(101, 102))
          totalCost should equal (10D)
        }
      }
    }
    "djikstrasAlgorithm" when {
      "called with an itty bitty triangle graph and valid od pair" should {
        "produce the shortest path" in {
          val factory = LocalGraphMATSimFactory
          factory.setCostFunctionFactory(TestCostFunction)
          val graph: LocalGraph[CoordinateVertexProperty,MacroscopicEdgeProperty] = factory.fromFile(networkFilePath).get
          val odPair = SimpleSSSP_ODPair(1L, 3L)
          val sssp = new SimpleSSSP[CoordinateVertexProperty, MacroscopicEdgeProperty]
          val result = sssp.djikstrasAlgorithm(graph, odPair)
          result.cost.sum should equal (2D)
          result.path should equal (List(1L, 2L))
        }
      }
      "called with an itty bitty triangle graph, snapshot, and valid od pair" should {
        "produce the shortest path" in {
          val factory = LocalGraphMATSimFactory
          factory.setCostFunctionFactory(BPRCostFunction)
          val graph: LocalGraph[CoordinateVertexProperty,MacroscopicEdgeProperty] = factory.fromFileAndSnapshot(networkFilePath, snapshotFilePath).get
          val odPair = SimpleSSSP_ODPair(1L, 3L)
          val sssp = new SimpleSSSP[CoordinateVertexProperty, MacroscopicEdgeProperty]
          val result = sssp.djikstrasAlgorithm(graph, odPair)
          result.cost.sum should be > 55D
          result.path should equal (List(1L, 2L))
        }
      }
      "called with the MATSim Equil network and valid od pair" should {
        "produce the shortest path" in {
          val factory = LocalGraphMATSimFactory
          factory.setCostFunctionFactory(BPRCostFunction)
          val graph: LocalGraph[CoordinateVertexProperty,MacroscopicEdgeProperty] = factory.fromFileAndSnapshot(equilNetworkFilePath, equilSnapshotFilePath).get
          val odPair = SimpleSSSP_ODPair(1L, 15L)
          val sssp = new SimpleSSSP[CoordinateVertexProperty, MacroscopicEdgeProperty]
          val result = sssp.djikstrasAlgorithm(graph, odPair)
          result.path should equal (List(1L, 5L, 14L, 20L, 21L, 22L))
          // @TODO: confirm this is running correctly. cost function evaluation?
        }
      }
    }
  }
}
