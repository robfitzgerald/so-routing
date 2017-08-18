package cse.fitzgero.sorouting.roadnetwork.scalagraph.edge

import cse.fitzgero.sorouting.roadnetwork.costfunction._

/**
  * Base class for Edge property classes
  */
abstract class EdgeProperty() extends Serializable {
  def flow: Double
  def cost: CostFunction
}