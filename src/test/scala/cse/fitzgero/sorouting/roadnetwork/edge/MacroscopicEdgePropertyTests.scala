package cse.fitzgero.sorouting.roadnetwork.edge

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate
import cse.fitzgero.sorouting.roadnetwork.costfunction.{BPRCostFunction, CostFunctionAttributes}
import cse.fitzgero.sorouting.util.convenience._


class MacroscopicEdgePropertyTests extends SORoutingUnitTestTemplate {
  "MacroscopicEdgeProperty" when {
    "constructed" should {
      "construct a valid MacroscopicEdgeProperty object" in {
        val mep = MacroscopicEdgeProperty[String]("1-2", 0.0D, BPRCostFunction(CostFunctionAttributes(1000D vph, 30D mph, 10D, 3600D vph, 10D)))
        mep.linkCostFlow should be > 30D
      }
    }
  }
}
