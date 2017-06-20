package cse.fitzgero.sorouting.roadnetwork.edge

/**
  * Base class for Edge property classes
  */
abstract class EdgeProperty () extends Serializable {
  def flow: Double
  def cost: Double
}