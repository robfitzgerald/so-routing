package cse.fitzgero.sorouting.algorithm.trafficassignment

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate
import cse.fitzgero.sorouting.algorithm.shortestpath.sssp.localgraph.simplesssp._
import cse.fitzgero.sorouting.algorithm.trafficassignment._
import cse.fitzgero.sorouting.algorithm.trafficassignment.localgraph._
import cse.fitzgero.sorouting.roadnetwork.localgraph.LocalGraphMATSimFactory

class LocalGraphFrankWolfeTests extends SORoutingUnitTestTemplate {
  "LocalGraphFrankWolfe" when {
    val networkFilePath: String =   "src/test/resources/LocalGraphFrankWolfeTests/network.xml"
    val snapshotFilePath: String =   "src/test/resources/LocalGraphFrankWolfeTests/snapshot.xml"
    val networkNoSolutionFilePath: String = "src/test/resources/LocalGraphFrankWolfeTests/networkNoSolution.xml"
    "assignment" when {
      "called with a small valid graph with no flows and valid set of od pairs" should {
        "assign flows to the obvious edges along the most direct paths" in {
          val graph = LocalGraphMATSimFactory.fromFile(networkFilePath).get
          val odPairs: Seq[SimpleSSSP_ODPair] = Seq(
            SimpleSSSP_ODPair(1L, 3L),
            SimpleSSSP_ODPair(1L, 3L),
            SimpleSSSP_ODPair(2L, 1L)
          )
          val result = LocalGraphFrankWolfe.assignment(graph, odPairs)

          // should result with 2 on edge 1, 3 on edge 2, and 1 on edge 3
          result.edgeAttrOf(1L).get.flow should equal (2)
          result.edgeAttrOf(2L).get.flow should equal (3)
          result.edgeAttrOf(3L).get.flow should equal (1)
        }
      }
      "called with a small graph where no solution can be found" should {
        "make no change" in {
          val graph = LocalGraphMATSimFactory.fromFile(networkNoSolutionFilePath).get
          val odPairs: Seq[SimpleSSSP_ODPair] = Seq(
            SimpleSSSP_ODPair(1L, 3L),
            SimpleSSSP_ODPair(1L, 3L),
            SimpleSSSP_ODPair(2L, 1L)
          )
          val result = LocalGraphFrankWolfe.assignment(graph, odPairs)

          // resulting graph should be unchanged
          result should equal(graph)
          result.edgeAttrOf(1L).get.flow should equal (0)
          result.edgeAttrOf(2L).get.flow should equal (0)
        }
      }
    }
    "relativeGap" when {
      "called with two graphs whos flows are not too different" should {
        "produce a smaller relative gap" in {
          val graph = LocalGraphMATSimFactory.fromFile(networkFilePath).get
          val thisGraph =
            graph
              .edges
              .map(id => (id, graph.edgeAttrOf(id).get))
              .foldLeft(graph)((newGraph, edgeData) => {
                newGraph.updateEdge(edgeData._1, edgeData._2.copy(flow = edgeData._2.flow + 10))
              })
          val thatGraph =
            graph
              .edges
              .map(id => (id, graph.edgeAttrOf(id).get))
              .foldLeft(graph)((newGraph, edgeData) => {
                newGraph.updateEdge(edgeData._1, edgeData._2.copy(flow = edgeData._2.flow + 11))
              })
          val result = LocalGraphFrankWolfe.relativeGap(thisGraph, thatGraph)
          result should be < 0.5
        }
      }
      "called with two graphs whos flows that differ in size by nearly 100%" should {
        "produce a larger relative gap" in {
          val graph = LocalGraphMATSimFactory.fromFile(networkFilePath).get
          val thisGraph =
            graph
              .edges
              .map(id => (id, graph.edgeAttrOf(id).get))
              .foldLeft(graph)((newGraph, edgeData) => {
                newGraph.updateEdge(edgeData._1, edgeData._2.copy(flow = edgeData._2.flow + 10))
              })
          val thatGraph =
            graph
              .edges
              .map(id => (id, graph.edgeAttrOf(id).get))
              .foldLeft(graph)((newGraph, edgeData) => {
                newGraph.updateEdge(edgeData._1, edgeData._2.copy(flow = edgeData._2.flow + 18))
              })
          val result = LocalGraphFrankWolfe.relativeGap(thisGraph, thatGraph)
          result should be > 0.5
        }
      }
      "called with two graphs whos flows that differ greater than 100% (upper bounds test)" should {
        "be no greater than 100%" in {
          val graph = LocalGraphMATSimFactory.fromFile(networkFilePath).get
          val thisGraph =
            graph
              .edges
              .map(id => (id, graph.edgeAttrOf(id).get))
              .foldLeft(graph)((newGraph, edgeData) => {
                newGraph.updateEdge(edgeData._1, edgeData._2.copy(flow = edgeData._2.flow + 10))
              })
          val thatGraph =
            graph
              .edges
              .map(id => (id, graph.edgeAttrOf(id).get))
              .foldLeft(graph)((newGraph, edgeData) => {
                newGraph.updateEdge(edgeData._1, edgeData._2.copy(flow = edgeData._2.flow + 5000))
              })
          val result = LocalGraphFrankWolfe.relativeGap(thisGraph, thatGraph)
          result should equal (1.0D)
        }
      }
    }
  }
}
