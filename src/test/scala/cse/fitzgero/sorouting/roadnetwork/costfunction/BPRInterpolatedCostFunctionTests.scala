package cse.fitzgero.sorouting.roadnetwork.costfunction

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate

class BPRInterpolatedCostFunctionTests extends SORoutingUnitTestTemplate {
  "BPRInterpolatedCostFunction" when {
    "generated" should {
      "generates a BPR Cost Function which, when all coefficients are 1, returns 1.15D" in {
        val cost: (Double) => Double = BPRInterpolatedCostFunction.generate(BPRInterpCoefficients(1D, 1D))
        cost(1) should equal (1.15D)
      }
      "generates a BPR Cost Function which is always positive for a range of values" in {
        val cost: (Double) => Double = BPRInterpolatedCostFunction.generate(BPRInterpCoefficients(1D, 1D))
        (-100 to 100 by 10).foreach(n=> {
          cost(n) should be > 0D
        })
      }
      "generates a BPR Cost Function whose domain increases monotonically" in {
        val typicalHighway: BPRInterpCoefficients = BPRInterpCoefficients(500D, 65D)
        val cost: (Double) => Double = BPRInterpolatedCostFunction.generate(typicalHighway)
        (0 to 100 by 5).map(cost(_)).sliding(2).foreach(tuple => {
          tuple(0) should be <= tuple(1)
        })
      }
      "interpolation with only one point should produce a line and result in predictable values" in {
        val cost: (Double) => Double = BPRInterpolatedCostFunction.generate(BPRInterpCoefficients(100D, 50D, 1))
        (1 to 500).map(cost(_)).sum.toInt / 500 should equal (50)
      }
      "interpolation with many points should produce an approximation of an exponential function" in {
        val cost: (Double) => Double = BPRInterpolatedCostFunction.generate(BPRInterpCoefficients(100D, 50D))
        cost(1) should be < cost(1000)
        cost(1) * 6 should be < cost(1000)
      }
    }
  }
}
