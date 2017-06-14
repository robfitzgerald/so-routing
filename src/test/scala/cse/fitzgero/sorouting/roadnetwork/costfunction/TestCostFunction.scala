package cse.fitzgero.sorouting.roadnetwork.costfunction

class TestCostFunction extends CostFunction {
  override def generate: (Double) => Double = (x: Double) => x
}

object TestCostFunction extends CostFunctionFactory {
  def apply(attributes: Map[String, String]): TestCostFunction = new TestCostFunction
  def apply(): TestCostFunction = new TestCostFunction
}