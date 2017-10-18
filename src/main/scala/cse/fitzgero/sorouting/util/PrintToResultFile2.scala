package cse.fitzgero.sorouting.util

case class PrintToResultFile2 (
  populationSize: Int,
  totRouteRequests: Int,
  ueRouteRequests: Int,
  soRouteRequests: Int,
  routePercent: Int,
  windowDur: Int,
  popAvgTripUE: Double,
  popAvgTripUESO: Double,
  netAvgTripUE: Double,
  netAvgTripUESO: Double
) extends PrintToResultFile {

  val popTravelTimeImprovement: Double = (popAvgTripUE - popAvgTripUESO) / popAvgTripUE
  val netTravelTimeImprovement: Double = (netAvgTripUE - netAvgTripUESO) / netAvgTripUE

  def quickStats[A](list: Seq[A])(implicit num: Numeric[A]): String = {
    import num._
    list.head + list.last
    val mean = list.sum.toInt / list.size
    f"${list.sum.toInt/1000D}%.3f,${list.min.toInt/1000D}%.3f,${mean/1000D}%.3f,${list.max.toInt/1000D}%.3f,${math.sqrt(list.map(n=>(n.toDouble-mean)*(n.toDouble-mean)).sum / list.size) / 1000D}%.3f"
  }

  override def toString: String = {
    // fullPop uePop soPop route% window totalAlgRunTime minRouteTime meanRouteTime maxRouteTime stdDevRouteTime avgTripUE avgTripUESO
    // fullPop: number of people
    // ueRouteRequests: number of route requests belonging to UE agents
    // soRouteRequests: number of route requests belonging to SO agents
    // routePercent: percentage of population that will be given SO routes
    // windowDur: duration (seconds) of the batching time for SO-routed agents
    // routeStats: total/min/mean/max/stddev travel time for each aspect of the algorithm
    // avgTripUE: MATSim-reported average travel time, all drivers assigned via Dijkstra's Algorithm (selfish)
    // avgTripUESO: MATSim-reported average travel time, drivers assigned via Dijkstra's and some % of the population routed via our SO routing algorithm
    // netAvgTripUE: for each link, the average time it took for a car to pass through it, averaged across the network, for the selfish drivers
    // netTripUESO: network avg travel time (as above), but for the mixed population
    f"$populationSize,$totRouteRequests,$ueRouteRequests,$soRouteRequests,$routePercent%%,$windowDur,$popAvgTripUE%.3f,$popAvgTripUESO%.3f,$netAvgTripUE%.3f,$netAvgTripUESO%.3f,$popTravelTimeImprovement%.4f,$netTravelTimeImprovement%.4f"
  }
}

object PrintToResultFile2 {
  def resultFileHeader: String = "populationSize,totalRouteRequests,routeRequestsUE,routeRequestsSO,routePercent,windowDuration,popAvgTripUE,popAvgTripUESO,netAvgTripUE,netAvgTripUESO,popTravelTimeImprovement,netTravelTimeImprovement"
}
