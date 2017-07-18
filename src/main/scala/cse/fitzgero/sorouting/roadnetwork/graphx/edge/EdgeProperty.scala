package cse.fitzgero.sorouting.roadnetwork.graphx.edge

import cse.fitzgero.sorouting.roadnetwork.graphx.costfunction._

/**
  * Base class for Edge property classes
  */
abstract class EdgeProperty () extends Serializable {
  def flow: Double
  def cost: CostFunction
}