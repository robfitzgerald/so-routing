package cse.fitzgero.sorouting.algorithm.trafficassignment

import cse.fitzgero.sorouting.SparkUnitTestTemplate
import cse.fitzgero.sorouting.algorithm.shortestpath._
import cse.fitzgero.sorouting.roadnetwork.costfunction._
import cse.fitzgero.sorouting.roadnetwork.graph._
import org.apache.spark.graphx.VertexId

class FrankWolfeTests extends SparkUnitTestTemplate("FrankWolfeTests") {
  "FrankWolfe" when {
    val networkFilePath: String =       "src/test/resources/FrankWolfeTests/network.xml"
    val snapshotFilePath: String =      "src/test/resources/FrankWolfeTests/snapshot.xml"
    //  1
    //
    //      2
    //
    //  4       6
    //
    //      3
    //
    //  5
    val testODPairs: Seq[(VertexId, VertexId)] = List(
      (1L, 6L),
      (1L, 6L),
      (4L, 6L),
      (4L, 6L),
      (5L, 6L),
      (5L, 6L)
    )
    "AONAssignment" when {
      "called with a graph with an shortest but obviously congested path" should {
        "route everything through that link anyway" in {
          val graph = GraphXMacroRoadNetwork(sc, BPRCostFunction).fromFileAndSnapshot(networkFilePath, snapshotFilePath).get
          val result = FrankWolfe.Assignment(graph, testODPairs, AONFlow())
          // all vehicles should have been routed on link "100", even though it was gnarly congested
          result.edges.toLocalIterator.filter(_.attr.id == "100").next.attr.flow should equal (2506.0D)
        }
      }
    }
  }
}
