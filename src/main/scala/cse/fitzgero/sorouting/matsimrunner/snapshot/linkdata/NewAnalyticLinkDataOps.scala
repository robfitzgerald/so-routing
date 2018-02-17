package cse.fitzgero.sorouting.matsimrunner.snapshot.linkdata

import cse.fitzgero.sorouting.matsimrunner.snapshot.linkdata.NewAnalyticLinkData.TravelTime
import cse.fitzgero.sorouting.model.roadnetwork.costfunction.CostFunction
import cse.fitzgero.sorouting.model.roadnetwork.local.LocalEdgeFlowAttribute

trait NewAnalyticLinkDataOps { ops: NewAnalyticLinkData =>

  def updateCongestion(updatedEdge: LocalEdgeFlowAttribute with CostFunction, data: AnalyticLinkDataUpdate): List[CongestionData] = {
    val currentFlow: Int = updatedEdge.flow match {
      case None => 0
      case Some(f) => f.toInt
    }
    val currentCost: Double = updatedEdge.linkCostFlow.get

    congestion :+ CongestionData(data.t, currentFlow, currentCost)
  }

  def updateTravelTime(data: AnalyticLinkDataUpdate): List[TravelTime] = {
    if (vehicles.isDefinedAt(data.veh)) {
      val timePassedOnLink = data.t - vehicles(data.veh)
      travelTime :+ timePassedOnLink
    } else travelTime
  }

  def congestionXml: xml.Elem =
    <congestion>
      {congestion.map(c => {
      <timestep time={c.time.toString} flow={c.vehicleCount.toString} costflow={c.congestionCost.toString}></timestep>
    })}
    </congestion>

  def travelTimeXml: xml.Elem =
    <travel-time count={count} min={min.orNull} mean={meanStr.orNull} max={max.orNull} stddev={stdDev.orNull}></travel-time>

  private def count: String = travelTime.size.toString
  private def min: Option[String] = if (travelTime.nonEmpty) Some(travelTime.min.toString) else None
  private def max: Option[String] = if (travelTime.nonEmpty) Some(travelTime.max.toString) else None
  def mean: Option[Double] = if (travelTime.nonEmpty) Some(travelTime.sum.toDouble / travelTime.size) else None
  private def meanStr: Option[String] = mean.map(_.toString)
  private def variance: Option[Double] = if (travelTime.nonEmpty) {
    val mean: Double = travelTime.sum.toDouble / travelTime.size
    val variance = travelTime.map(n => (n.toDouble - mean) * (n.toDouble - mean)).sum / travelTime.size
    Some(variance)
  } else None
  private def stdDev: Option[String] = variance match {
    case None => Some("0")
    case Some(variance) =>
      Some(math.sqrt(variance).toString)
  }
}