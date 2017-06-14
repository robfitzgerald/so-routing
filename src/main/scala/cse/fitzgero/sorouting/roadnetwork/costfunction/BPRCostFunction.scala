package cse.fitzgero.sorouting.roadnetwork.costfunction

import scala.math.pow

case class BPRCoefficients (capacity: Double, freeFlowSpeed: Double) extends CostFunctionCoefficients

/**
  * latency function from the Bureau of Public Roads, taken from
  * U.S. Bureau of Public Roads. Traffic Assignment Manual. U.S. Department of Commerce, Washington, D.C (1964)
  */
object BPRCostFunction extends CostFunction[BPRCoefficients] {
  override def generate (coef: BPRCoefficients): (Double) => Double = {
    // S_a(v_a) = t_a(1 + 0.15(v_a/c_a)^4)
    (flow: Double) => {
      coef.freeFlowSpeed * (1 + 0.15D * pow(flow / coef.capacity, 4))
    }
  }
}
