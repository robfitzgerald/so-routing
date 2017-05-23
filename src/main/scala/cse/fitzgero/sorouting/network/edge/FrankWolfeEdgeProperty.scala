package cse.fitzgero.sorouting.network.edge

/**
  * basic requirements for a traffic assignment optimization framework
  * @param id the MATSim edge id, used when converting back to MATSim
  * @param flow current flow on edge (vehicles/time)
  * @param costFlow a cost/flow function such as BPL, which is a function of
  *                 free flow capacity, free flow speed, and the current flow
  */
case class FrankWolfeEdgeProperty (
  id: String,
  flow: Double,
  costFlow: (Double) => Double)
  extends EdgeProperty {
  def cost (): Double = costFlow(flow)
}