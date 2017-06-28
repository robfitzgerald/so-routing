package cse.fitzgero.sorouting.roadnetwork.costfunction


abstract class CostFunction extends Serializable {
  def zeroValue: Double
  def costFlow(flow: Double): Double
  def freeFlowCost: Double
  def marginalCost(flow: Double): Double
}

abstract class CostFunctionFactory {
  def apply(attributes: Map[String, String]): CostFunction
  def apply(attributes: CostFunctionAttributes): CostFunction
}