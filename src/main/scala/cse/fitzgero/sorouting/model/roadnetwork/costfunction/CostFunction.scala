package cse.fitzgero.sorouting.model.roadnetwork.costfunction

trait CostFunctionType

trait CostFunction {
  def costFlow(flowEvaluation: Double): Option[Double]
  def linkCostFlow: Option[Double]
}
