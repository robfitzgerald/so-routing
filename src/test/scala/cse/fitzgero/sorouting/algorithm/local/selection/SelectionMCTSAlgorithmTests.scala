package cse.fitzgero.sorouting.algorithm.local.selection

import scala.collection.GenMap

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate
import cse.fitzgero.sorouting.algorithm.local.selection.SelectionMCTSAlgorithm.{MCTSAltPath, MCTSTreeNode, Tag}

class SelectionMCTSAlgorithmTests extends SORoutingUnitTestTemplate {
  "SelectionMCTSAlgorithm" when {
    "runAlgorithm" when {
      "called with a small graph and two small sets of alternate paths" should {
        "find the optimal combination" in new CombinatorialTestAssets.CombinationSet {
          case class Config(coefficientCp: Double, congestionRatioThreshold: Double, computationalLimit: Long)
          val config = Config(0.7071D, 3D, 2000)
          SelectionMCTSAlgorithm.runAlgorithm(graph, kspResult, Some(config)) match {
            case None => fail()
            case Some(result) =>
              println(result)
          }
        }
      }
      "called with a bigger graph and set of alternate paths" should {
        "find the optimal combination" in new CombinatorialTestAssets.BiggerMap {
          case class Config(coefficientCp: Double, congestionRatioThreshold: Double, computationalLimit: Long)
          val config = Config(0, 3.14D, 2000)
          SelectionMCTSAlgorithm.runAlgorithm(bigGraph, kspResult, Some(config)) match {
            case None => fail()
            case Some(result) =>
              println(result)
              println(s"request size ${kspResult.size} result size ${result.size}")
          }
        }
      }
    }
  }
}
