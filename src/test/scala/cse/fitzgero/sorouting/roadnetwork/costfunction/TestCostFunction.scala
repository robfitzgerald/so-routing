package cse.fitzgero.sorouting.roadnetwork.costfunction

/**
  * a cost function that evaluates any link as having a cost of "1"
  */
class TestCostFunction extends CostFunction {
  override def generate: (Double) => Double = (x: Double) => 1
}

object TestCostFunction extends CostFunctionFactory {
  def apply(attributes: Map[String, String]): TestCostFunction = new TestCostFunction
  def apply(): TestCostFunction = new TestCostFunction
}