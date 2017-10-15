package cse.fitzgero.sorouting.algorithm.local.ksp

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate
import cse.fitzgero.sorouting.model.roadnetwork.local.LocalODPair

class KSPLocalDijkstrasAlgorithmTests extends SORoutingUnitTestTemplate {
  "KSPLocalDijkstrasAlgorithm" when {
    "called with a valid graph and od pair, requesting exactly how many possible alternates exist" should {
      "produce two alternative paths of increasing cost" in new TestAssets.GraphWithAlternates {
        KSPLocalDijkstrasAlgorithm.runAlgorithm(graph, LocalODPair("joe", "1", "10"), Some(KSPLocalDijkstrasConfig(2))) match {
          case Some(result) =>
            result.paths.size should equal (2)
            // the sum of costs for the first path should be less than the sum of costs for the second
            result.paths.head.map(_.cost.get.sum).sum should be < result.paths.tail.head.map(_.cost.get.sum).sum
          case None => fail()
        }
      }
    }
    "called with a valid graph and od pair, requesting more alternates than what exists" should {
      "produce four alternative paths of increasing cost" in new TestAssets.GraphWithAlternates {
        KSPLocalDijkstrasAlgorithm.runAlgorithm(graph, LocalODPair("fred", "1", "10"), Some(KSPLocalDijkstrasConfig(10))) match {
          case Some(result) =>
            result.paths.foreach(println)
            result.paths.size should equal (4)
            // the paths should be ordered by cost
            result.paths.map(_.map(_.cost.get.sum).sum).iterator.sliding(2).foreach(pair => {
              pair(0) should be <= (pair(1))
            })

          case None => fail()
        }
      }
    }
    "called with a valid graph and od pair where origin == destination" should {
      "return " in new TestAssets.GraphWithAlternates {
        KSPLocalDijkstrasAlgorithm.runAlgorithm(graph, LocalODPair("fred", "4", "4"), Some(KSPLocalDijkstrasConfig(10))) match {
          case Some(result) =>
            // a result with an empty path set
            result.paths.size should equal (1)
            result.paths.head.isEmpty should be (true)
          case None => fail()
        }
      }
    }
    "called with a 25x25 graph without a direct path between two od pairs" should {
      "produce a valid set of k shortest paths" in new TestAssets.BiggerGraph {
        KSPLocalDijkstrasAlgorithm.runAlgorithm(graph, LocalODPair("billy", "1", "25"), Some(KSPLocalDijkstrasConfig(20))) match {
          case Some(result) =>
            result.paths.size should equal (6)
            // the paths should be ordered by cost
            result.paths.map(_.map(_.cost.get.sum).sum).iterator.sliding(2).foreach(pair => {
              pair(0) should be <= (pair(1))
            })
          case None => fail()
        }
      }
    }
  }
}
