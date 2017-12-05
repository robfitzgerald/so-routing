package cse.fitzgero.sorouting.model.roadnetwork.costfunction

object BPRCostFunctionType extends CostFunctionType

/**
  * decorates an object with the latency function designed by the Bureau of Public Roads, taken from
  * U.S. Bureau of Public Roads. Traffic Assignment Manual. U.S. Department of Commerce, Washington, D.C (1964)
  */
trait BPRCostFunction extends CostFunction {
  self: {
    def flow: Option[Double]
    def capacity: Option[Double]
    def freeFlowSpeed: Option[Double]
    def distance: Option[Double]
  } =>

  private lazy val freeFlowTravelTime: Option[Double] =
    for {
      d <- distance
      f <- freeFlowSpeed
    } yield d / f


  private lazy val costTerm1: Option[Double] = freeFlowTravelTime
  private lazy val costTerm2: Option[Double] =
    freeFlowTravelTime match {
      case Some(f) => Some(f * 0.15D)
      case None => None
    }

  /**
    * calculates the link travel time, via S_a(v_a) = t_a(1 + 0.15(v_a/c_a)^4) = t_a + 0.15t_a(v_a/c_a)^4 = costTerm1 + costTerm2 * expTerm
    *
    * @param flowEvaluation the current value for flow, which will be added to the fixed flow
    * @return travel time cost
    */
  def costFlow(flowEvaluation: Double): Option[Double] = {

    val allFlow: Double = flow match {
      case Some(ff) => ff + flowEvaluation
      case None => flowEvaluation
    }

    for {
      cap <- capacity
      c1 <- costTerm1
      c2 <- costTerm2
    } yield {
      val e1: Double = math.pow(allFlow / cap, 4)
      c1 + c2 * e1
    }
  }

  /**
    * shorthand method for getting the cost flow of the current link flow
    * @return
    */
  def linkCostFlow: Option[Double] = costFlow(0D)
//    flow match {
//      case Some(ff) => costFlow(ff)
//      case None => costFlow(0D)
//    }
}