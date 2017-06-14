package cse.fitzgero.sorouting.roadnetwork.costfunction

import scala.math.pow

/**
  * latency function from the Bureau of Public Roads, taken from
  * U.S. Bureau of Public Roads. Traffic Assignment Manual. U.S. Department of Commerce, Washington, D.C (1964)
  */
class BPRCostFunction (capacity: Double, freeFlowSpeed: Double) extends CostFunction {
  override def generate: (Double) => Double = {
    // S_a(v_a) = t_a(1 + 0.15(v_a/c_a)^4)
    (flow: Double) => {
      freeFlowSpeed * (1 + 0.15D * pow(flow / capacity, 4))
    }
  }
}

object BPRCostFunction extends CostFunctionFactory {
  val defaultCapacity: String = 100.toString
  val defaultFreeFlow: String = 50.toString
  def apply(attributes: Map[String, String]): BPRCostFunction = {
    val cap: Double = attributes.getOrElse("capacity", defaultCapacity).toDouble
    val free: Double = attributes.getOrElse("freespeed", defaultFreeFlow).toDouble
    new BPRCostFunction(cap, free)
  }
}