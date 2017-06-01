package cse.fitzgero.sorouting.roadnetwork.edge

/**
  * Base class for Edge property classes
  */
abstract class EdgeProperty {
  def flow: Double
  def cost(): Double
}