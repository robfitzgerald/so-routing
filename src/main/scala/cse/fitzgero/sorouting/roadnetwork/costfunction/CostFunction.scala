package cse.fitzgero.sorouting.roadnetwork.costfunction


abstract class CostFunction {
  def generate: (Double) => Double
}

abstract class CostFunctionFactory {
  def apply(attributes: Map[String, String]): CostFunction
}