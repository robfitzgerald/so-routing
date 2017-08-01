package cse.fitzgero.sorouting.roadnetwork.costfunction

import scala.math.pow

/**
  * latency function from the Bureau of Public Roads, taken from
  * U.S. Bureau of Public Roads. Traffic Assignment Manual. U.S. Department of Commerce, Washington, D.C (1964)
  */
class BPRCostFunction (capacity: Double, freeFlowSpeed: Double, val snapshotFlow: Double = 0D) extends CostFunction {
  val costTerm1: Double = freeFlowSpeed
  val costTerm2: Double = freeFlowSpeed * 0.15D
  val marginalCostTerm: Double = costTerm2 * 4

  // S_a(v_a) = t_a(1 + 0.15(v_a/c_a)^4)
  override def costFlow(flow: Double): Double = costTerm1 + costTerm2 * pow((flow + snapshotFlow) / capacity, 4)
  override def freeFlowCost: Double = this.costFlow(0D)
  override def marginalCost(flow: Double): Double = marginalCostTerm * pow(flow / capacity, 3)
}

object BPRCostFunction extends CostFunctionFactory {
  val expectedAttributes = Map("capacity" -> 100D, "freespeed" -> 50D, "flow" -> 0D, "flowRate" -> 3600D, "algorithmFlowRate" -> 3600D)
  def apply(attributes: Map[String, String]): BPRCostFunction = {
    parseAttributes(attributes, expectedAttributes) match {
      case cap :: freeFlowSpeed :: steadyStateNetworkFlow :: flowRate :: algorithmFlowRate :: _ =>
        println(s"$cap $freeFlowSpeed $steadyStateNetworkFlow $flowRate $algorithmFlowRate")
        if (cap == 0D)
          new BPRCostFunction(1D, freeFlowSpeed, steadyStateNetworkFlow)
        else {
          val capacity = cap * (flowRate / algorithmFlowRate)
          new BPRCostFunction(capacity, freeFlowSpeed, steadyStateNetworkFlow)
        }
      case _ => throw new IllegalArgumentException(s"Unable to parse expected arguments ${expectedAttributes.keys.mkString("")} from ${attributes.keys.mkString("")}")
    }
  }
  def apply(attr: CostFunctionAttributes): BPRCostFunction = {
    val capacity = attr.capacity * (attr.flowRate / attr.algorithmFlowRate)
    new BPRCostFunction(capacity, attr.freespeed, attr.flow)
  }
}
