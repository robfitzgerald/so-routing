package cse.fitzgero.sorouting.roadnetwork.edge

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate
import cse.fitzgero.sorouting.roadnetwork.costfunction.{BPRCostFunction, CostFunctionAttributes}
import cse.fitzgero.sorouting.util.implicits._


class MacroscopicEdgePropertyTests extends SORoutingUnitTestTemplate {
  ("MacroscopicEdgeProperty" when {
    "constructed" should {
      "construct a valid MacroscopicEdgeProperty object" in {
        val mep = MacroscopicEdgeProperty[String]("1-2", 0.0D, BPRCostFunction(CostFunctionAttributes(1000D vph, 30D mph, 10D, 3600D vph, 10D, length = 200D)))
        mep.linkCostFlow should be > 30D
      }
    }
    "copy" should {
      "update the identified attributes and return a new edge" in {
        val mep = MacroscopicEdgeProperty[String]("1-2", 0.0D, BPRCostFunction(CostFunctionAttributes(1000D vph, 30D mph, 10D, 3600D vph, 10D, length = 200D)))
        mep.assignedFlow should equal (0)
        mep.cost.fixedFlow should equal (10)
        mep.allFlow should equal (10)
        mep.linkCostFlow.toInt should equal (174)
//        println(mep)
        val result = mep.copy(flowUpdate = 20D)
        result.assignedFlow should equal (20D)
        result.cost.fixedFlow should equal (10)
        result.allFlow should equal (30)
        result.linkCostFlow.toInt should equal (310211)
        result.cost.costFlow(result.assignedFlow).toInt should equal (310211)
//        println(result)
      }
    }
  }) (org.scalactic.source.Position.here)
}
