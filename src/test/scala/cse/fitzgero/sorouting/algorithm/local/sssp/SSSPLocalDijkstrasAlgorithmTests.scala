package cse.fitzgero.sorouting.algorithm.local.sssp

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate
import cse.fitzgero.sorouting.model.roadnetwork.local.LocalODPair

class SSSPLocalDijkstrasAlgorithmTests extends SORoutingUnitTestTemplate {
  "runAlgorithm" when {
    "called with a graph and an od pair" should {
      "result in an algorithm result with a correct shortest path" in new TestAssets.BackPropData with TestAssets.TwoPathsGraph {
        SSSPLocalDijkstrasAlgorithm.runAlgorithm(graph, LocalODPair("bobby", "1", "10")) match {
          case None => fail()
          case Some(result) =>
            val one :: two :: three :: four :: Nil = result.path
            one.edgeId should equal ("102")
            two.edgeId should equal ("204")
            three.edgeId should equal ("406")
            four.edgeId should equal ("610")
            result.path.map(_.cost.get.sum).sum should equal (4D)
        }
      }
    }
  }
  "minSpanningDijkstras" when {
    "called with a small road network and an origin" should {
      "find the set of edges that are members of the minimum spanning tree" in new TestAssets.TriangleWorld {
        SSSPLocalDijkstrasAlgorithm.minSpanningDijkstras(graph, "1") match {
          case None => fail()
          case Some(result) =>
            result.size should equal (3)
            result.foreach(_._2.d should be <= 2D)
        }
      }
    }
    "called with a k5 graph and an origin" should {
      "find the set of 5 edges that comprises its spanning tree" in new TestAssets.K5Graph {
        SSSPLocalDijkstrasAlgorithm.minSpanningDijkstras(graph, "1") match {
          case None => fail()
          case Some(result) =>
            result.size should be (5)
            result("1").d should equal (0D)
            result("1").π should equal (None)
            result("2").d should equal (1D)
            result("2").π should equal (Some("102"))
            result("3").d should equal (1D)
            result("3").π should equal (Some("103"))
            result("4").d should equal (1D)
            result("4").π should equal (Some("104"))
            result("5").d should equal (1D)
            result("5").π should equal (Some("105"))
        }
      }
    }
    "called with a graph that has a dominant path and a request which includes a destination" should {
      "find the minimum spanning tree" in new TestAssets.TwoPathsGraph {
        SSSPLocalDijkstrasAlgorithm.minSpanningDijkstras(graph, "1", Some("10")) match {
          case None => fail()
          case Some(result) =>
            // the longest path should be a distance of 4
            result.foreach(_._2.d should be <= 4D)
            // the path to vertex 8 is the same distance as the path to the destination, so
            // it is possible that we might find 8 or 9 back propagation tuples. result is a map, its keys are distinct.
            result.size should (equal (8) or equal (9))
        }
      }
      "have a different result with and without a destination" in new TestAssets.TwoPathsGraph {
        SSSPLocalDijkstrasAlgorithm.minSpanningDijkstras(graph, "1") match {
          case None => fail()
          case Some(spanningAll) =>
            SSSPLocalDijkstrasAlgorithm.minSpanningDijkstras(graph, "1", Some("6")) match {
              case None => fail()
              case Some(spanningBounded) =>
                // for the complete spanning tree
                // all vertices are visited
                spanningAll.size should be (10)
                // the furthest distance should be 4
                spanningAll.foreach(_._2.d should be <= 4D)
                // for the bounded spanning tree
                // some vertices are visited
                spanningBounded.size should (equal (6) or equal (7))
                // 8 and 10 should never be visited
                spanningBounded.isDefinedAt("8") should be (false)
                spanningBounded.isDefinedAt("10") should be (false)
                // the furthest distance should be 3
                spanningBounded.foreach(_._2.d should be <= 3D)
            }
        }
      }
    }
  }
  "backPropagate" when {
    "called with a minimum spanning tree of edges" should {
      "find the path back from a destination to the origin" in new TestAssets.BackPropData with TestAssets.TwoPathsGraph {
        SSSPLocalDijkstrasAlgorithm.backPropagate(graph, spanning, "10") match {
          case None => fail()
          case Some(result) =>
            val one :: two :: three :: four :: Nil = result
            one.edgeId should equal ("102")
            two.edgeId should equal ("204")
            three.edgeId should equal ("406")
            four.edgeId should equal ("610")
            result.map(_.cost.get.sum).sum should equal (4D)
        }
      }
    }
  }
}
