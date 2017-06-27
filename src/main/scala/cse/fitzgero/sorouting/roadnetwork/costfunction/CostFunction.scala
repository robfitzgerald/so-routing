package cse.fitzgero.sorouting.roadnetwork.costfunction


abstract class CostFunction extends Serializable {
  def costFlow(flow: Double): Double
  def freeFlowCost: Double
  def marginalCost(flow: Double): Double
}

abstract class CostFunctionFactory {
  def apply(attributes: Map[String, String]): CostFunction
}