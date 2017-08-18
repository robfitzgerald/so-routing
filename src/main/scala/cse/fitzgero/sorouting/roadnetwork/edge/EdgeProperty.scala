package cse.fitzgero.sorouting.roadnetwork.edge

import cse.fitzgero.sorouting.roadnetwork.costfunction._

/**
  * Base class for Edge property classes
  */
abstract class EdgeProperty () extends Serializable {
  type T <: EdgeProperty
  def assignedFlow: Double
  def linkCostFlow: Double
  def cost: CostFunction
  def copy (flowUpdate: Double = assignedFlow, costUpdate: CostFunction = cost): T
}