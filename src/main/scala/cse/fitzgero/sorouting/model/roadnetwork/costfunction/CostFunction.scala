package cse.fitzgero.sorouting.model.roadnetwork.costfunction

trait CostFunctionType

/**
  * the methods that a cost function should expose
  */
trait CostFunction {

  /**
    * evaluates the cost of the current link, based on any stored flow values, +- the flowEvaluation argument
    * @param flowEvaluation some value to add to whatever base flow value is stored on the link
    * @return
    */
  def costFlow(flowEvaluation: Double): Option[Double]

  /**
    * evaluates the cost of the current link with no flows assigned (i.e., the free flow cost)
    * @return
    */
  def freeFlowCostFlow: Option[Double]

  /**
    * evaluates the cost of the current link, based on stored flow values
    * @return
    */
  def linkCostFlow: Option[Double]

  /**
    * evaluates the cost of the current link, based on the provided capacity value
    * @return
    */
  def capacityCostFlow: Option[Double]
}
