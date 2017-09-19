package cse.fitzgero.sorouting.roadnetwork.costfunction

abstract class CostFunction extends Serializable {
  def fixedFlow: Double
  def freeFlowCost: Double
  def costFlow(flow: Double): Double
  def marginalCost(flow: Double): Double
  override def toString: String = s"CostFunction with snapshotFlow: $fixedFlow freeflowCost: $freeFlowCost"
}

abstract class CostFunctionFactory {
  def apply(attributes: Map[String, String]): CostFunction
  def apply(attributes: CostFunctionAttributes): CostFunction
}