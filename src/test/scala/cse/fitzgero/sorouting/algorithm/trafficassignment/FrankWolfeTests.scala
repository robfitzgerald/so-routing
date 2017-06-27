package cse.fitzgero.sorouting.algorithm.trafficassignment

import cse.fitzgero.sorouting.SparkUnitTestTemplate
import cse.fitzgero.sorouting.algorithm.shortestpath._
import cse.fitzgero.sorouting.roadnetwork.costfunction._
import cse.fitzgero.sorouting.roadnetwork.graph._
import org.apache.spark.graphx.VertexId

class FrankWolfeTests extends SparkUnitTestTemplate("FrankWolfeTests") {
  "FrankWolfe object" when {
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
    "frankWolfeFlowCalculation" when {
      "called with equal flow values and mean (50%) phi value" should {
        "return the same flow number" in {
          FrankWolfe.frankWolfeFlowCalculation(FrankWolfe.Phi(0.5), 100D, 100D) should equal (100D)
        }
      }
      "called with Phi Values at the extremum" should {
        "return 100% of the corresponding flow value" in {
          FrankWolfe.frankWolfeFlowCalculation(FrankWolfe.Phi(1.0D), 67, 100D) should equal (100D)
          FrankWolfe.frankWolfeFlowCalculation(FrankWolfe.Phi(0.0D), 67, 100D) should equal (67D)
        }
      }
    }
    "Phi" when {
      "called with a valid Phi value" should {
        "produce a valid Phi object" in {
          val phi = new FrankWolfe.Phi(0.8D)
          phi.value should equal (0.8D)
          phi.inverse should be >= 0.19D
          phi.inverse should be <= 0.2D
        }
      }
      "called with an invalid Phi value" should {
        "throw an exception" in {
          val testValue: Double = 1.2D
          val thrown = the [IllegalArgumentException] thrownBy new FrankWolfe.Phi(testValue)
          thrown.getMessage should equal (s"requirement failed: Phi is only defined for values in the range [0,1], but found ${testValue.toString}")
        }
      }
    }
    "relativeGap" when {
      "called with two road networks" should {
        "calculate the relative gap of those two networks" in {
          val graph = GraphXMacroRoadNetwork(sc, BPRCostFunction).fromFileAndSnapshot(networkFilePath, snapshotFilePath).get
          val currentIteration = FrankWolfe.Assignment(graph, testODPairs, CostFlow())
          val aonAssignment = FrankWolfe.Assignment(graph, testODPairs, AONFlow())
          FrankWolfe.relativeGap(currentIteration, aonAssignment) should be < 1.0D
        }
      }
    }
    "objective" when {
      "called" should {
        "exist" in {
          fail()
        }
        "be modular" in {
          fail()
        }
        "guide our phi value" in {
          fail()
        }
      }
    }
  }
}
