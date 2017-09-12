package cse.fitzgero.sorouting.roadnetwork.costfunction

import scala.math.pow

/**
  * latency function from the Bureau of Public Roads, taken from
  * U.S. Bureau of Public Roads. Traffic Assignment Manual. U.S. Department of Commerce, Washington, D.C (1964)
  * @param capacity capacity, adjusted to the current algorithm flow rate aka time window size
  * @param freeFlowSpeed speed at free flow, in coordinate units per time unit
  * @param distance distance in the coordinate system of choice
  * @param fixedFlow flow values that are fixed. for example, reported current link flows which are not under control.
  */
class BPRCostFunction (capacity: Double, freeFlowSpeed: Double, distance: Double, val fixedFlow: Double = 0D) extends CostFunction {
  lazy val freeFlowTravelTime: Double = distance / freeFlowSpeed
  val costTerm1: Double = freeFlowTravelTime
  lazy val costTerm2: Double = freeFlowTravelTime * 0.15D
  lazy val marginalCostTerm1: Double = costTerm2 * 4

  /**
    * calculates the link travel time, via S_a(v_a) = t_a(1 + 0.15(v_a/c_a)^4) = t_a + 0.15t_a(v_a/c_a)^4 = costTerm1 + costTerm2 * expTerm
    * @param flow the current value for flow, which will be added to the fixed flow
    * @return travel time cost
    */
  override def costFlow(flow: Double): Double = {
    val expTerm = pow(flow + fixedFlow / capacity, 4)
    costTerm1 + costTerm2 * expTerm
  }

  /**
    * travel time cost function based only on the fixed flow
    * @return travel time cost
    */
  override def freeFlowCost: Double = this.costFlow(0D)

  /**
    * marginal cost at the provided flow value (the first derivative of the cost function)
    * @param flow the current value for flow, which will be added to the fixed flow
    * @return
    */
  override def marginalCost(flow: Double): Double = marginalCostTerm1 * pow(flow / capacity, 3)
}

object BPRCostFunction extends CostFunctionFactory {
  val expectedAttributes = Map("capacity" -> 100D, "freespeed" -> 50D, "flow" -> 0D, "flowRate" -> 3600D, "algorithmFlowRate" -> 3600D, "length" -> 100D)
  def apply(attributes: Map[String, String] = Map()): BPRCostFunction = {
    parseAttributes(attributes, expectedAttributes) match {
      case cap :: freeFlowSpeed :: steadyStateNetworkFlow :: flowRate :: algorithmFlowRate :: distance :: _ =>
        println(s"$cap $freeFlowSpeed $steadyStateNetworkFlow $flowRate $algorithmFlowRate $distance")
        if (cap == 0D)
          new BPRCostFunction(1D, freeFlowSpeed, steadyStateNetworkFlow)
        else {
          val capacity = cap * (flowRate / algorithmFlowRate)
          new BPRCostFunction(capacity, freeFlowSpeed, distance, steadyStateNetworkFlow)
        }
      case _ => throw new IllegalArgumentException(s"Unable to parse expected arguments ${expectedAttributes.keys.mkString("")} from ${attributes.keys.mkString("")}")
    }
  }
  def apply(attr: CostFunctionAttributes): BPRCostFunction = {
    val capacity = attr.capacity * (attr.algorithmFlowRate / attr.flowRate)
    new BPRCostFunction(capacity, attr.freespeed, attr.length, attr.flow)
  }
}
