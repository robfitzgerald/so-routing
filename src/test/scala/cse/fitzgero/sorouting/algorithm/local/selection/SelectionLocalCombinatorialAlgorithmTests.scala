package cse.fitzgero.sorouting.algorithm.local.selection

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate

class SelectionLocalCombinatorialAlgorithmTests extends SORoutingUnitTestTemplate {
  "SelectionLocalCombinatorialAlgorithm" when {
    "called with a set of possible alternate paths" should {
      "find the optimal combination" in new CombinatorialTestAssets.CombinationSet {
        SelectionLocalCombinatorialAlgorithm.runAlgorithm(graph, kspResult) match {
          case Some(resultStuff) =>
            resultStuff.foreach(println)
            val joeResult = kspResult(joeRequest)
            val bobResult = kspResult(bobRequest)

            joeResult.head.map(_.edgeId) should equal (Seq("102","204","406","610"))
            bobResult.head.map(_.edgeId) should equal (Seq("204", "406", "610"))

          case None => fail("there should be a result")
        }
      }
    }
    // this can take up to an hour
    "given a result of a bigger mksp search" should {
      "find a minimal cost set" in new CombinatorialTestAssets.BiggerMap {
        SelectionLocalCombinatorialAlgorithm.runAlgorithm(bigGraph, kspResult) match {
          case None => fail("there should be a result")
          case Some(result) =>
            result.foreach(println)
            println(s"total cost (minimal): ${result.values.map(_.map(_.cost.get.sum).sum).sum}")

        }
      }
    }
  }
}