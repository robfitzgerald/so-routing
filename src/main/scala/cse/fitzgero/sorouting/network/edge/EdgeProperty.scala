package cse.fitzgero.sorouting.network.edge

/**
  * Base class for Edge property classes
  */
private abstract class EdgeProperty {
  def flow: Double
  def cost(): Double
}