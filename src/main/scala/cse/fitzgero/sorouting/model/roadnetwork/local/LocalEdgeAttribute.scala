package cse.fitzgero.sorouting.model.roadnetwork.local

import cse.fitzgero.sorouting.model.roadnetwork.costfunction._

sealed trait LocalEdgeAttribute {
  def fixedFlow: Option[Double]
  def capacity: Option[Double]
  def freeFlowSpeed: Option[Double]
  def distance: Option[Double]
  def costFlow(flow: Double): Option[Double]
  def linkCostFlow: Option[Double]
}
sealed trait LocalEdgeAttributeType

case class LocalEdgeAttributeBasic (fixedFlow: Option[Double] = None, capacity: Option[Double] = None, freeFlowSpeed: Option[Double] = None, distance: Option[Double] = None) extends LocalEdgeAttribute with BasicCostFunction {
  def linkCostFlow: Option[Double] = Some(1)
}
case object LocalEdgeAttributeBasic extends LocalEdgeAttributeType

case class LocalEdgeAttributeBPR (fixedFlow: Option[Double], capacity: Option[Double], freeFlowSpeed: Option[Double], distance: Option[Double]) extends LocalEdgeAttribute with BPRCostFunction {
  def linkCostFlow: Option[Double] =
    fixedFlow match {
      case Some(ff) => costFlow(ff)
      case None => costFlow(0D)
    }
}
case object LocalEdgeAttributeBPR extends LocalEdgeAttributeType