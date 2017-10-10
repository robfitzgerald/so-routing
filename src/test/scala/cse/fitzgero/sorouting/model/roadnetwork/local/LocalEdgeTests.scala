package cse.fitzgero.sorouting.model.roadnetwork.local

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate

class LocalEdgeTests extends SORoutingUnitTestTemplate {
  "LocalEdge" when {
    "constructed with a valid set of arguments" should {
      "bust out a super-fly Edge" in new TestAssets.LocalEdgeValidArgs1 {
        val result = LocalEdge(id, src, dst, fixedFlow, capacity, freeFlowSpeed, distance, t)
        result.attribute.linkCostFlow.get.toInt should equal (20)
      }
    }
    "constructed with some None types" should {
      "still make an Edge but have predictable computations" in new TestAssets.LocalEdgeValidArgs1 {
        val result1 = LocalEdge(id, src, dst, None, capacity, freeFlowSpeed, distance, t)
        val result2 = LocalEdge(id, src, dst, fixedFlow, None, freeFlowSpeed, distance, t)
        val result3 = LocalEdge(id, src, dst, fixedFlow, capacity, None, distance, t)
        val result4 = LocalEdge(id, src, dst, fixedFlow, capacity, freeFlowSpeed, None, t)
        val result5 = LocalEdge(id, src, dst, None, None, None, None, t)

        // this is a repitition of the LocalEdgeAttributeBPRTests but with the requisite values in scope of the CostFunction mixin

        // can still calculate the cost at free flow speed which is 1000 / 50 = 20
        result1.attribute.linkCostFlow should equal (Some(20.0D))

        // these should fall through to None
        for {
          r <- Seq(result2, result3, result4, result5)
        } {
          r.attribute.linkCostFlow should equal (None)
        }
      }
    }
  }
}
