package cse.fitzgero.sorouting.model.roadnetwork.costfunction

object BasicCongestionFunctionType extends CostFunctionType

/**
  * Evaluates the costFlow at 1.0 + flow, which should have a trivial congestion awareness
  */
trait BasicCongestionFunction extends CostFunction {
  def costFlow(flow: Double): Option[Double] = Some(1 + math.pow(2, flow))
  def linkCostFlow: Option[Double] = Some(1)
}
