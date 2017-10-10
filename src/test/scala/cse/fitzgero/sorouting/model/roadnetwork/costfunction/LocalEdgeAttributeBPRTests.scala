package cse.fitzgero.sorouting.model.roadnetwork.costfunction

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate
import cse.fitzgero.sorouting.model.roadnetwork.local.LocalEdgeAttributeBPR

class LocalEdgeAttributeBPRTests extends SORoutingUnitTestTemplate {
  "LocalEdgeAttributeBPRTests" when {
    "apply with non-empty arguments" should {
      "construct a valid attribute and cost function" in new TestAssets.ValidArgs {
        val result = LocalEdgeAttributeBPR(fixedFlow, capacity, freeFlowSpeed, distance)
        // 10 vehicles on a 1000 ft road with 100 car capacity and 50 ft per second freeFlowSpeed
        // should result in something very close to 20 seconds (1000 / 50)
        result.linkCostFlow.get.toInt should equal (20)
      }
    }
    "apply with a None argument" should {
      "construct a valid attribute and a cost function that evaluates to None" in new TestAssets.ValidArgs {
        val result1 = LocalEdgeAttributeBPR(None, capacity, freeFlowSpeed, distance)
        val result2 = LocalEdgeAttributeBPR(fixedFlow, None, freeFlowSpeed, distance)
        val result3 = LocalEdgeAttributeBPR(fixedFlow, capacity, None, distance)
        val result4 = LocalEdgeAttributeBPR(fixedFlow, capacity, freeFlowSpeed, None)
        val result5 = LocalEdgeAttributeBPR(None, None, None, None)

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
