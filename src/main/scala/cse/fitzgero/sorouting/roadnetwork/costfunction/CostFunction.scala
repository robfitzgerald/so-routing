package cse.fitzgero.sorouting.roadnetwork.costfunction

/**
  * a list of cost function arguments, such as capacity and free flow speed
  */
abstract class CostFunctionCoefficients {}

abstract class CostFunction [T <: CostFunctionCoefficients] {
  def generate (args: T): (Double) => Double
}
