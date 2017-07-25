package cse.fitzgero.sorouting.roadnetwork.localgraph.edge

import cse.fitzgero.sorouting.roadnetwork.costfunction.{CostFunction, TestCostFunction}


/**
  * basic requirements for a traffic assignment optimization framework
  *
  * @param id the MATSim edge id, used when converting back to MATSim
  * @param flow current flow on edge (vehicles/time)
  * @param cost a cost/flow function such as BPL, which is a function of
  *                 free flow capacity, free flow speed, and the current flow
  */
case class MacroscopicEdgeProperty (
  id: EdgeIdType = "",
  flow: Double = 1.0D,
  cost: CostFunction = TestCostFunction())  // default cost function is identity function of flow variable
  extends EdgeProperty {
  def linkCostFlow: Double = this.cost.costFlow(flow)
  def allFlow: Double = flow + this.cost.zeroValue
}