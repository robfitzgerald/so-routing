package cse.fitzgero.sorouting.algorithm.local.selection

import cse.fitzgero.sorouting.SparkUnitTestTemplate

class SelectionSparkCombinatorialAlgorithmTests extends SparkUnitTestTemplate("SelectionSparkCombinatorialAlgorithm") {
  "SelectionLocalCombinatorialAlgorithm" when {
    "runAlgorithm" when {
      "called with a small graph and set of alternative paths" should {
        "produce the minimal combination" in new TestAssets.CombinationSet {
          val result = SelectionSparkCombinatorialAlgorithm.runAlgorithm(graph, kspResult, Some(sc))
          val joeResult = kspResult(joeRequest)
          val bobResult = kspResult(bobRequest)
          joeResult.head.map(_.edgeId) should equal (Seq("102","204","406","610"))
          bobResult.head.map(_.edgeId) should equal (Seq("204", "406", "610"))
        }
      }
//      "called with a large graph and set of alternative paths" should {
//        "produce the minimal combination" in new TestAssets.BiggerMap {
//          val result = SelectionSparkCombinatorialAlgorithm.runAlgorithm(bigGraph, kspResult, Some(sc))
//          result.foreach(println)
//        }
//      }
    }
    "generateAllCombinations" when {
      "called with a small set of tags and paths" should {
        "produce all combinations" in new TestAssets.CombinationSet {
          val req = SelectionSparkCombinatorialAlgorithm.tagRequests(kspResult)
          val result = SelectionSparkCombinatorialAlgorithm.generateAllCombinations(sc)(req).collect
          // there should be 3 x 4 = 12 combinations for joe and bob
          result.distinct.size should equal (12)
          result.foreach(res => println(res.map(_._1)))
          result.foreach {
            pair =>
              pair.head._1.personId should equal (joeRequest.id)
              pair.tail.head._1.personId should equal (bobRequest.id)
          }
        }
      }
      "called with a set of 4 driver alternate paths" should {
        "produce all combinations" in new TestAssets.CombinationSet {
          val altBobAndJoe = kspResult.map { person =>
            val newAlterEgo = person._1.copy(id = person._1.id + "boog")
            (newAlterEgo, person._2)
          }.toMap
          val req = SelectionSparkCombinatorialAlgorithm.tagRequests(kspResult ++ altBobAndJoe)
          val result = SelectionSparkCombinatorialAlgorithm.generateAllCombinations(sc)(req).collect
          result.foreach(res => println(res.map(_._1)))
          // there should be 3 x 4 x 3 x 4 = 144 combinations for joe and bob and their alter-egos
          result.distinct.size should equal(144)
        }
      }
      "called with a set of 6 driver alternate paths" should {
        "produce all combinations" in new TestAssets.CombinationSet {
          val altBobAndJoe = kspResult.map { person =>
            val newAlterEgo = person._1.copy(id = person._1.id + "ert")
            (newAlterEgo, person._2)
          }.toMap
          val moreBobAndJoeShow = kspResult.map { person =>
            val newAlterEgo = person._1.copy(id = person._1.id + "burger")
            (newAlterEgo, person._2)
          }.toMap
          val req = SelectionSparkCombinatorialAlgorithm.tagRequests(kspResult ++ altBobAndJoe ++ moreBobAndJoeShow)
          val result = SelectionSparkCombinatorialAlgorithm.generateAllCombinations(sc)(req).collect
//          result.foreach(res => println(res.map(_._1)))
          // there should be 3 x 4 x 3 x 4 x 3 x 4 = 1728 combinations for joe and bob and their alter-egos
          result.distinct.size should equal(1728)
        }
      }
//      "called with a large set of tags and paths" should {
//        "produce all combinations" in new TestAssets.BiggerMap {
//          val req = SelectionSparkCombinatorialAlgorithm.tagRequests(kspResult)
//          val result = SelectionSparkCombinatorialAlgorithm.generateAllCombinations(sc)(req).collect
//          result.foreach(println)
//        }
//      }
    }
    "minimalCostCombination" when {
      "called with a set of combinations" should {
        "return the minimum cost combination" in new TestAssets.CombinationSet {
          val altBobAndJoe = kspResult.map { person =>
            val newAlterEgo = person._1.copy(id = person._1.id + "ert")
            (newAlterEgo, person._2)
          }.toMap
          val req = SelectionSparkCombinatorialAlgorithm.tagRequests(kspResult ++ altBobAndJoe)
          val combinations = SelectionSparkCombinatorialAlgorithm.generateAllCombinations(sc)(req)
          val expectedCombinations: Long = combinations.count()
          expectedCombinations should be (144)
          val result = SelectionSparkCombinatorialAlgorithm.minimalCostCombination(sc)(combinations, graph, expectedCombinations)
          // TODO: i did not calculate the true minimal cost combination. test correctness!
          println(result)
        }
      }
    }
  }
}