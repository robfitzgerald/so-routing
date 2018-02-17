package cse.fitzgero.sorouting.model.roadnetwork.local

trait LocalEdgeAttribute {
  def flow: Option[Double]
  def capacity: Option[Double]
  def freeFlowSpeed: Option[Double]
  def distance: Option[Double]
}
