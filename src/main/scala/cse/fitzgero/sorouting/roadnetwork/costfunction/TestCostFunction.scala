package cse.fitzgero.sorouting.roadnetwork.costfunction

/**
  * a cost function that evaluates any link as having a cost of "1"
  */
class TestCostFunction(val zeroValue: Double = 0.0D) extends CostFunction {
  override def costFlow(flow: Double): Double = 1
  override def freeFlowCost: Double = 1
  override def marginalCost(flow: Double): Double = 1
}

object TestCostFunction extends CostFunctionFactory {
  def apply(attributes: Map[String, String]): TestCostFunction = new TestCostFunction(attributes.getOrElse("flow", "0").toDouble)
  def apply(attributes: CostFunctionAttributes): TestCostFunction = new TestCostFunction(attributes.flow)
  def apply(): TestCostFunction = new TestCostFunction
}