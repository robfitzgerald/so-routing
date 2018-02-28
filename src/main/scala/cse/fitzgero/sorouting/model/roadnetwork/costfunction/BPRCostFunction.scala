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
    * calculates the link travel time, via
    * @param flowEvaluation some value to add to whatever base flow value is stored on the link
    * @return
    */
  override def costFlow(flowEvaluation: Double): Option[Double] = {

    val allFlow: Option[Double] = flow match {
      case Some(ff) => Some(ff + flowEvaluation)
      case None => Some(flowEvaluation)
    }

    bprCostFunction(allFlow)
  }

  override def freeFlowCostFlow: Option[Double] = bprCostFunction()


  /**
    * calculates the convex, monotonically increasing function S_a(v_a) = t_a(1 + 0.15(v_a/c_a)^4) = t_a + 0.15t_a(v_a/c_a)^4 = costTerm1 + costTerm2 * expTerm
    * @param flow a flow value, by default set to zero (free flow evaluation). flow = None will result in a None-valued function evaluation
    * @return
    */
  private def bprCostFunction(flow: Option[Double] = Some(0)): Option[Double] = {
    for {
      thisFlow <- flow
      cap <- capacity
      c1 <- costTerm1
      c2 <- costTerm2
    } yield {
      val e1: Double = math.pow(thisFlow / cap, 4)
      c1 + c2 * e1
    }
  }

  /**
    * shorthand method for getting the cost flow of the current link flow
    * @return
    */
  override def linkCostFlow: Option[Double] = costFlow(0D)
}