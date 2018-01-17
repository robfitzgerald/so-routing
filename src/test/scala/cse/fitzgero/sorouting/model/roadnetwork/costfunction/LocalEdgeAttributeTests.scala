package cse.fitzgero.sorouting.model.roadnetwork.costfunction

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate
import cse.fitzgero.sorouting.model.roadnetwork.local.LocalEdgeAttribute

class LocalEdgeAttributeTests extends SORoutingUnitTestTemplate {
  "new LocalEdgeAttributeTests" when {
    "apply with non-empty arguments" should {
      "construct a valid attribute and cost function" in new TestAssets.ValidArgs {
        val result = new LocalEdgeAttribute(fixedFlow, capacity, freeFlowSpeed, distance) with BPRCostFunction
        // 10 vehicles on a 1000 ft road with 100 car capacity and 50 ft per second freeFlowSpeed
        // should result in something very close to 20 seconds (1000 / 50)
        result.linkCostFlow.get.toInt should equal (20)
      }
    }
    "apply with a None argument" should {
      "construct a valid attribute and a cost function that, when missing some data, computes correctly" in new TestAssets.ValidArgs {
        val result1 = new LocalEdgeAttribute(None, capacity, freeFlowSpeed, distance) with BPRCostFunction
        val result2 = new LocalEdgeAttribute(fixedFlow, None, freeFlowSpeed, distance) with BPRCostFunction
        val result3 = new LocalEdgeAttribute(fixedFlow, capacity, None, distance) with BPRCostFunction
        val result4 = new LocalEdgeAttribute(fixedFlow, capacity, freeFlowSpeed, None) with BPRCostFunction
        val result5 = new LocalEdgeAttribute(None, None, None, None) with BPRCostFunction

        // can still calculate the cost at free flow speed which is 1000 / 50 = 20
        result1.linkCostFlow should equal (Some(20.0D))

        // these should fall through to None
        for {
          r <- Seq(result2, result3, result4, result5)
        } {
          r.linkCostFlow should equal (None)
        }
      }
    }
  }
}
