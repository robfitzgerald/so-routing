package cse.fitzgero.sorouting.model.roadnetwork.costfunction

trait CostFunctionType

/**
  * the methods that a cost function should expose
  */
trait CostFunction {
  def costFlow(flowEvaluation: Double): Option[Double]
  def linkCostFlow: Option[Double]
}
