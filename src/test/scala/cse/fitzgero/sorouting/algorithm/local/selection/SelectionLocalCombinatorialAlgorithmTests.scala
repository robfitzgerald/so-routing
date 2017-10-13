package cse.fitzgero.sorouting.algorithm.local.selection

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate

class SelectionLocalCombinatorialAlgorithmTests extends SORoutingUnitTestTemplate {
  "SelectionLocalCombinatorialAlgorithm" when {
    "called with a set of possible alternate paths" should {
      "find the optimal combination" in new TestAssets.CombinationSet {
        val result = SelectionLocalCombinatorialAlgorithm.runAlgorithm(graph, requests)
      }
    }
  }
}
