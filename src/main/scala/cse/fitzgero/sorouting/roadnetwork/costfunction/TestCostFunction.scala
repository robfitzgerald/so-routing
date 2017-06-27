package cse.fitzgero.sorouting.roadnetwork.costfunction

/**
  * a cost function that evaluates any link as having a cost of "1"
  */
class TestCostFunction extends CostFunction {
  override def costFlow(flow: Double): Double = 1
  override def freeFlowCost: Double = 1
  override def marginalCost(flow: Double): Double = 1
}

object TestCostFunction extends CostFunctionFactory {
  def apply(attributes: Map[String, String]): TestCostFunction = new TestCostFunction
  def apply(): TestCostFunction = new TestCostFunction
}