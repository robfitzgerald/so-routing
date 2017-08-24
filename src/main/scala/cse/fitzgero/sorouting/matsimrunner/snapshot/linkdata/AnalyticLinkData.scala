package cse.fitzgero.sorouting.matsimrunner.snapshot.linkdata

import cse.fitzgero.sorouting.roadnetwork.costfunction.CostFunction
import org.matsim.api.core.v01.Id
import org.matsim.vehicles.Vehicle

case class AnalyticLinkDataUpdate(veh: Id[Vehicle], t: Int)

abstract class AnalyticLinkData extends LinkData[AnalyticLinkDataUpdate] {
  def vehicles: Map[Id[Vehicle], Int]
  def congestion: List[(Int, Int, Double)]
  def travelTime: List[Int]
  def cost: CostFunction
  val hasAnalytics: Boolean = travelTime.nonEmpty

  def updateCongestion(vehiclesUpdate: Map[Id[Vehicle], Int], data: AnalyticLinkDataUpdate): List[(Int, Int, Double)] =
    congestion :+ (data.t, vehiclesUpdate.size, cost.costFlow(vehiclesUpdate.size))

  def updateTravelTime(data: AnalyticLinkDataUpdate): List[Int] =
    travelTime :+ data.t - vehicles(data.veh)

  def congestionXml: xml.Elem =
      <congestion>
        {congestion.map(c => {
        <timestep time={c._1.toString} flow={c._2.toString} costflow={c._3.toString}></timestep>
      })}
      </congestion>
}

trait AnalyticMethods extends AnalyticLinkData {
  private def safeReport(f: () => Double): Double = if (hasAnalytics) f() else 0D
  def count: Int = travelTime.size
  lazy val min: Double = safeReport(()=>travelTime.min)
  lazy val max: Double = safeReport(()=>travelTime.max)
  lazy val mean: Double =  safeReport(()=>travelTime.sum.toDouble / travelTime.size)
  lazy val variance: Double = safeReport(()=>travelTime.map(n => (n.toDouble - mean) * (n.toDouble - mean)).sum / travelTime.size)
  lazy val stdDev: Double = safeReport(()=>math.sqrt(variance))
  def travelTimeXml: xml.Elem = <travel-time count={count.toString} min={min.toString} mean={mean.toString} max={max.toString} stddev={stdDev.toString}></travel-time>
}

case class AnalyticLink(cost: CostFunction, vehicles: Map[Id[Vehicle], Int] = Map(), congestion: List[(Int, Int, Double)] = List(), travelTime: List[Int] = List()) extends AnalyticLinkData with AnalyticMethods {

  override def add(data: AnalyticLinkDataUpdate): AnalyticLink = {
      val vehicleUpdate = vehicles.updated(data.veh, data.t)
      val congestionUpdate = updateCongestion(vehicleUpdate, data)
      this.copy(vehicles = vehicleUpdate, congestion = congestionUpdate)
  }

  override def remove(data: AnalyticLinkDataUpdate): AnalyticLink = {
      val vehicleUpdate = vehicles - data.veh
      val congestionUpdate = updateCongestion(vehicleUpdate, data)
      val travelTimeUpdate = updateTravelTime(data)
      this.copy(vehicles = vehicleUpdate, congestion = congestionUpdate, travelTime = travelTimeUpdate)
  }

  def flow: Int = vehicles.size

//  def toXml: xml.Elem =
//    <report>
//      {travelTimeXml}
//    </report>
}