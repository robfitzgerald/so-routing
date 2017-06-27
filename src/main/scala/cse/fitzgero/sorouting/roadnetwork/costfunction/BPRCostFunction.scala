package cse.fitzgero.sorouting.roadnetwork.costfunction

import scala.math.pow

/**
  * latency function from the Bureau of Public Roads, taken from
  * U.S. Bureau of Public Roads. Traffic Assignment Manual. U.S. Department of Commerce, Washington, D.C (1964)
  */
class BPRCostFunction (capacity: Double, freeFlowSpeed: Double) extends CostFunction {
  val costTerm1: Double = freeFlowSpeed
  val costTerm2: Double = freeFlowSpeed * 0.15D
  val marginalCostTerm: Double = costTerm2 * 4

  // S_a(v_a) = t_a(1 + 0.15(v_a/c_a)^4)
  override def costFlow(flow: Double): Double = costTerm1 + costTerm2 * pow(flow / capacity, 4)
  override def freeFlowCost: Double = this.costFlow(0)
  override def marginalCost(flow: Double): Double = marginalCostTerm * pow(flow / capacity, 3)
}

object BPRCostFunction extends CostFunctionFactory {
  val expectedAttributes = Map("capacity" -> 100D, "freespeed" -> 50D)
  def apply(attributes: Map[String, String]): BPRCostFunction = {
    parseAttributes(attributes, expectedAttributes) match {
      case cap :: free :: _ =>
        if (cap == 0)
          new BPRCostFunction(1, free)
        else
          new BPRCostFunction(cap, free)
      case _ => throw new IllegalArgumentException(s"Unable to parse expected arguments ${expectedAttributes.keys.mkString("")} from ${attributes.keys.mkString("")}")
    }
  }
}