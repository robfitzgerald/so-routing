package cse.fitzgero.sorouting.roadnetwork.edge

/**
  * basic requirements for a traffic assignment optimization framework
  * @param id the MATSim edge id, used when converting back to MATSim
  * @param flow current flow on edge (vehicles/time)
  * @param costFlow a cost/flow function such as BPL, which is a function of
  *                 free flow capacity, free flow speed, and the current flow
  */
case class MacroscopicEdgeProperty (
  id: EdgeIdType = "",
  flow: Double = 1.0D,
  costFlow: (Double) => Double = identity[Double])  // default cost function is identity function of flow variable
  extends EdgeProperty {
  override def cost: Double = costFlow(flow)
}