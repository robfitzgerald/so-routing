package cse.fitzgero.sorouting.matsimrunner.population
import java.time.LocalTime

import cse.fitzgero.sorouting.algorithm.mssp.graphx.simplemssp._
import cse.fitzgero.sorouting.roadnetwork.edge.EdgeIdType
import org.apache.spark.graphx.VertexId

import scala.xml.Elem

case class PersonNode (id: String, mode: String, homeAM: MorningActivity, work: List[MiddayActivity], homePM: EveningActivity, legsParam: List[LegNode] = List.empty[LegNode]) extends ConvertsToXml {
  private val legList: List[LegNode] =
    if (legsParam.nonEmpty) legsParam
    else
      (homeAM.vertex +: work.map(_.vertex) :+ homePM.vertex)
        .sliding(2).toList
        .map((odPair) => LegNode(mode, odPair(0), odPair(1)))

  def legs: List[LegNode] = legList

  def updatePath(src: VertexId, dst: VertexId, path: List[EdgeIdType]): PersonNode = {
    val legIdx: Int = legList.indexWhere(leg => leg.source == src && leg.destination == dst)
    if (legIdx == -1) {println(s"PersonNode.updatePath failed to find leg with src $src and dst $dst with path values ${path.mkString(" ")}"); this }
    else {
      val newLegVal: LegNode = legList(legIdx).copy(path = path)
      this.copy(legsParam = legList.updated(legIdx, newLegVal))
    }
  }

  def stripedLegsAndActivities: Seq[ConvertsToXml] = {
    if (work.isEmpty) Seq()
    else {
      val legNodes: List[LegNode] =
        if (legList.nonEmpty) legList
        else
        // constructs a default set of LegNode objects based on the links where each activity occurs
          (homeAM.vertex +: work.map(_.vertex) :+ homePM.vertex)
            .sliding(2).toList
            .map((odPair) => LegNode(mode, odPair(0), odPair(1)))
      def stripe(remLegs: List[ConvertsToXml], remWork: List[ConvertsToXml]): List[ConvertsToXml] = {
        if (remWork.isEmpty) List(remLegs.head)
        else remLegs.head :: remWork.head :: stripe(remLegs.tail, remWork.tail)
      }
      stripe(legNodes, work)
    }
  }

  /**
    * construct OD Pair objects from any trips in the range [low, high)
    * @param low lower bound, inclusive
    * @param high upper bound, exclusive
    * @return
    */
  def unpackTrips(low: LocalTime, high: LocalTime): ODPairs = {
    val srcVerticesInTimeRange: Seq[VertexId] = ((homeAM.vertex, homeAM.opts) +: (work.map(a => (a.vertex, a.opts))))
      .map({
        case (vertex, EndTime(time)) => Some((vertex, time))
        case _ => None
      })
      .filter(_.isDefined)
      .filter(tuple => {
        val thisTime = tuple.get._2
        (thisTime == low || thisTime.isAfter(low)) && thisTime.isBefore(high)
      })
      .map(_.get._1)
    legs.filter(srcVerticesInTimeRange contains _.source).map(asODPair)
  }

  def asODPair(leg: LegNode): SimpleMSSP_ODPair =
    SimpleMSSP_ODPair(
      id,
      leg.source,
      leg.destination
    )

  override def toXml: Elem =
    <person id={id}>
      <plan>
        {homeAM.toXml}
        {stripedLegsAndActivities.map(_.toXml)}
        {homePM.toXml}
      </plan>
    </person>
}
