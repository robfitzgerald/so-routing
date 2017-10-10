package cse.fitzgero.sorouting.roadnetwork.edge

import cse.fitzgero.sorouting.roadnetwork.costfunction.{CostFunction, TestCostFunction}


/**
  * basic requirements for a traffic assignment optimization framework
  *
  * @param id the MATSim edge id, used when converting back to MATSim
  * @param assignedFlow current flow assignment on edge (vehicles/time), not including the snapshot flows
  * @param cost a cost/flow function such as BPL, which is a function of
  *                 free flow capacity, free flow speed, and the current snapshot flow
  */
case class MacroscopicEdgeProperty [Id] (
  id: Id,
  assignedFlow: Double = 0.0D,
  cost: CostFunction = TestCostFunction())  // default cost function is identity function of flow variable
  extends EdgeProperty {
  override type T = MacroscopicEdgeProperty[Id]
  override def copy(flowUpdate: Double = assignedFlow, costUpdate: CostFunction = cost): MacroscopicEdgeProperty[Id] =
    MacroscopicEdgeProperty[Id](id, flowUpdate, costUpdate)

  override def toString: String =
    s"-[$id]-> assignedFlow $assignedFlow costFn $cost linkCostFlow $linkCostFlow"
  override def linkCostFlow: Double =
    cost.costFlow(assignedFlow)

  def allFlow: Double = assignedFlow + this.cost.fixedFlow
}