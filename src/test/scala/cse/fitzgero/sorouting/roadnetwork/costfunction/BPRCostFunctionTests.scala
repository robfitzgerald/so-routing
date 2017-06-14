package cse.fitzgero.sorouting.roadnetwork.costfunction

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate

class BPRCostFunctionTests extends SORoutingUnitTestTemplate {
  "BPRCostFunction" when {
    "generated" should {
      "generates a BPR Cost Function which, when all coefficients are 1, returns 1.15D" in {
        val cost: (Double) => Double = BPRCostFunction.generate(BPRCoefficients(1D, 1D))
        cost(1) should equal (1.15D)
      }
      "generates a BPR Cost Function which is always positive for a range of values" in {
        val cost: (Double) => Double = BPRCostFunction.generate(BPRCoefficients(1D, 1D))
        (-100 to 100 by 10).foreach(n=> {
          cost(n) should be > 0D
        })
      }
      "generates a BPR Cost Function whose domain increases monotonically" in {
        val typicalHighway: BPRCoefficients = BPRCoefficients(500D, 65D)
        val cost: (Double) => Double = BPRCostFunction.generate(typicalHighway)
        (0 to 100 by 5).map(cost(_)).sliding(2).foreach(tuple => {
          tuple(0) should be <= tuple(1)
        })
      }
    }
  }
}
