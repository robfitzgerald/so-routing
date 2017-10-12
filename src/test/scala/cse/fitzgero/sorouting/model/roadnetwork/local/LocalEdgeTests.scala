package cse.fitzgero.sorouting.model.roadnetwork.local

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate
import cse.fitzgero.sorouting.model.roadnetwork.costfunction.BPRCostFunctionType

class LocalEdgeTests extends SORoutingUnitTestTemplate {
  "LocalEdge" when {
    "constructed with a valid set of arguments" should {
      "bust out a super-fly Edge that has a cost function" in new TestAssets.LocalEdgeValidArgs1 {
        val result = LocalEdge(id, src, dst, flow, capacity, freeFlowSpeed, distance)
        result.attribute.linkCostFlow.get should equal (1.0D)
        result.id should equal (id)
        result.src should equal (src)
        result.dst should equal (dst)
        result.attribute.flow should equal (flow)
        result.attribute.capacity should equal (capacity)
        result.attribute.freeFlowSpeed should equal (freeFlowSpeed)
        result.attribute.distance should equal (distance)
      }
    }
    "constructed with some None types" should {
      "still make an Edge but have predictable computations" in new TestAssets.LocalEdgeValidArgs1 {
        val result1 = LocalEdge(id, src, dst, None, capacity, freeFlowSpeed, distance, Some(BPRCostFunctionType))
        val result2 = LocalEdge(id, src, dst, flow, None, freeFlowSpeed, distance, Some(BPRCostFunctionType))
        val result3 = LocalEdge(id, src, dst, flow, capacity, None, distance, Some(BPRCostFunctionType))
        val result4 = LocalEdge(id, src, dst, flow, capacity, freeFlowSpeed, None, Some(BPRCostFunctionType))
        val result5 = LocalEdge(id, src, dst, None, None, None, None, Some(BPRCostFunctionType))

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
