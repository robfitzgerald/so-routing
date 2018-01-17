package cse.fitzgero.sorouting.model.roadnetwork.costfunction

object BasicCongestionFunctionType extends CostFunctionType

/**
  * This cost function grows exponentially with added flow
  */
trait BasicCongestionFunction extends CostFunction {
  def costFlow(flow: Double): Option[Double] = Some(1 + math.pow(2, flow))
  def linkCostFlow: Option[Double] = Some(1)
}
