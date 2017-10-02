package cse.fitzgero.sorouting.roadnetwork.costfunction

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate

class BPRCostFunctionTests extends SORoutingUnitTestTemplate {
  "BPRCostFunction" when {
    "generated" should {
      "generates a BPR Cost Function which, when all coefficients are 1, returns 1.15D" in {
        val costFunction = BPRCostFunction(CostFunctionAttributes(1, 1))
        costFunction.costFlow(1) should equal (115D)
      }
      "generates a BPR Cost Function which is always positive for a range of values" in {
        val costFunction = BPRCostFunction(CostFunctionAttributes(10, 10))
        (-100 to 100 by 10).foreach(n=> {
          costFunction.costFlow(n) should be > 0D
        })
      }
      "generates a BPR Cost Function whose domain increases monotonically" in {
        val costFunction = BPRCostFunction(CostFunctionAttributes(500, 65))
        (0 to 100 by 5).map(costFunction.costFlow(_)).sliding(2).foreach(tuple => {
          tuple(0) should be <= tuple(1)
        })
      }
      "generates a BPR Cost Function whose freeFlowCost is based on a flow of zero from the default attribute" in {
        val costFunction = BPRCostFunction(CostFunctionAttributes(1, 1))
        costFunction.freeFlowCost should equal (100D)
      }
      "generates a BPR Cost Function whose freeFlowCost is based on a passed in flow value" in {
        val costFunction = BPRCostFunction(CostFunctionAttributes(capacity = 10, freespeed = 1, flow = 10))
        costFunction.freeFlowCost should equal (115D)
      }
    }
  }
}
