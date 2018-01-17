package cse.fitzgero.sorouting.model.roadnetwork.costfunction

object BasicCostFunctionType extends CostFunctionType

/**
  * Evaluates the costFlow at 1.0, which equates to algorithms which simply count the number of edges used
  */
trait BasicCostFunction extends CostFunction {
  def costFlow(flow: Double): Option[Double] = Some(1)
  def linkCostFlow: Option[Double] = Some(1)
}
