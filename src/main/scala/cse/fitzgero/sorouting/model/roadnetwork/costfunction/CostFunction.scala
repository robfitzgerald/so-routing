package cse.fitzgero.sorouting.model.roadnetwork.costfunction

trait CostFunction {
  def costFlow(flow: Double): Option[Double]
}
