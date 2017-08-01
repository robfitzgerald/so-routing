package cse.fitzgero.sorouting.roadnetwork.costfunction


abstract class CostFunction extends Serializable {
  def snapshotFlow: Double
  def freeFlowCost: Double
  def costFlow(flow: Double): Double
  def marginalCost(flow: Double): Double
  override def toString: String = s"CostFunction with snapshotFlow: $snapshotFlow freeflowCost: $freeFlowCost costFlow(0): ${costFlow(0)}"
}

abstract class CostFunctionFactory {
  def apply(attributes: Map[String, String]): CostFunction
  def apply(attributes: CostFunctionAttributes): CostFunction
}