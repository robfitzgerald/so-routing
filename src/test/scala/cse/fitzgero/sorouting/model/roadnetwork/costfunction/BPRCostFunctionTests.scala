package cse.fitzgero.sorouting.model.roadnetwork.costfunction

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate

class BPRCostFunctionTests extends SORoutingUnitTestTemplate {
  "BPRCostFunction" when {
    case class Link(flow: Option[Double], capacity: Option[Double], freeFlowSpeed: Option[Double], distance: Option[Double])
    "linkCostFlow" when {
      "called on an empty link" should {
        "give the cost of that empty link" in {
          val test = new Link(None, Some(1000), Some(100), Some(100)) with BPRCostFunction
          for {
            testCost <- test.linkCostFlow
          } {
            testCost should equal(1.0D)
          }
        }
      }
    }
    "costFlow" when {
      "called with -1 on a link with one flow" should {
        "give the same cost as linkCostFlow for a link with no flow" in {
          val test = new Link(Some(1), Some(1000), Some(25), Some(100)) with BPRCostFunction
          val shouldEqual = new Link(None, Some(1000), Some(25), Some(100)) with BPRCostFunction
          val shouldAlsoEqual = new Link(Some(0), Some(1000), Some(25), Some(100)) with BPRCostFunction
          for {
            testCost <- test.costFlow(-1)
            testLinkCostFlow <- test.linkCostFlow
            shouldEqualCost <- shouldEqual.linkCostFlow
            shouldAlsoEqualCost <- shouldAlsoEqual.linkCostFlow
          } {
            testCost should not equal testLinkCostFlow
            testCost should equal (shouldEqualCost)
            testCost should equal (shouldAlsoEqualCost)
          }
        }
      }
    }
    "capacityCostFlow" when {
      "demonstration" should {
        "show values we expect" in {
          (1 to 10).foreach {
            n =>
              val test = new Link(Some(n), Some(8.333), Some(25), Some(100)) with BPRCostFunction
              for {
                link <- test.linkCostFlow
                min <- test.freeFlowCostFlow
                max <- test.capacityCostFlow
              } {
                link should (be <= max and be >= min)
              }
          }
        }
      }
    }
  }
}
