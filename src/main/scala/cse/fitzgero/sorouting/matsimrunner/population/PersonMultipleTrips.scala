package cse.fitzgero.sorouting.matsimrunner.population
import java.time.LocalTime

import cse.fitzgero.sorouting.algorithm.pathsearch.mssp.graphx.simplemssp._
import cse.fitzgero.sorouting.roadnetwork.localgraph.EdgeId
import org.apache.spark.graphx.VertexId

import scala.xml.Elem

case class PersonMultipleTrips (id: String, mode: String, homeAM: MorningActivity, work: List[MiddayActivity], homePM: EveningActivity, legsParam: List[UnroutedLeg] = List.empty[UnroutedLeg]) extends ConvertsToXml {
//  private val legList: List[LegNode] =
//    if (legsParam.nonEmpty) legsParam
//    else generateLegs

  lazy val legs: List[UnroutedLeg] =
    if (legsParam.nonEmpty) legsParam
    else generateLegs

  def updatePath(src: VertexId, dst: VertexId, path: List[EdgeId]): PersonMultipleTrips = {
    val legIdx: Int = legs.indexWhere(leg => leg.srcVertex == src && leg.dstVertex == dst)
    if (legIdx == -1) {println(s"PersonNode.updatePath failed to find leg with src $src and dst $dst with path values ${path.mkString(" ")}"); this }
    else {
      val newLegVal: UnroutedLeg = legs(legIdx).copy(path = path)
      this.copy(legsParam = legs.updated(legIdx, newLegVal))
    }
  }

  def hasActivityWithVertices(src: VertexId, dst: VertexId): Boolean = {
    legs.indexWhere(leg => leg.srcVertex == src && leg.dstVertex == dst) != -1
  }

  def hasBeenAssignedPathWithVertices(src: VertexId, dst: VertexId): Boolean = {
    val legIdx = legs.indexWhere(leg => leg.srcVertex == src && leg.dstVertex == dst)
    if (legIdx != -1)
      legs(legIdx).path.nonEmpty
    else false
  }

  private def generateLegs: List[UnroutedLeg] =
    ((homeAM.link, homeAM.vertex) +:
      work.map(w=> (w.link, w.vertex)) :+
      (homePM.link, homePM.vertex)
    ).sliding(2)
      .toList
    .map((odPair) => UnroutedLeg(mode, odPair(0)._2, odPair(1)._2, odPair(0)._1, odPair(1)._1))


  def stripedLegsAndActivities: Seq[ConvertsToXml] = {
    if (work.isEmpty) Seq()
    else {
//      val legNodes: List[LegNode] =
//        if (legList.nonEmpty) legList
//        else generateLegs
        // constructs a default set of LegNode objects based on the links where each activity occurs
//          (homeAM.link +: work.map(_.link) :+ homePM.link)
//            .sliding(2).toList
//            .map((odPair) => LegNode(mode, odPair(0), odPair(1)))
      def stripe(remLegs: List[ConvertsToXml], remWork: List[ConvertsToXml]): List[ConvertsToXml] = {
        if (remWork.isEmpty) List(remLegs.head)
        else remLegs.head :: remWork.head :: stripe(remLegs.tail, remWork.tail)
      }
      stripe(legs, work)
    }
  }

  /**
    * generic function used to export legs using a time group mapping function
    * @param expFn a function used to map from LegNode to T
    * @param low lower time bound, inclusive
    * @param high upper time bound, exclusive
    * @tparam T type of export object
    * @return a list of leg values exported as type T
    */
  def exportLegsByTimeGroup[T](expFn: (UnroutedLeg)=>T)(low: LocalTime, high: LocalTime): List[T] = {
    val srcVerticesInTimeRange: Seq[VertexId] = ((homeAM.vertex, homeAM.opts) +: work.map(a => (a.vertex, a.opts)))
      .map({
        case (vertex, EndTime(time)) => Some((vertex, time))
        case _ => None
      })
      .filter(tuple => {
        if (tuple.isDefined) {
          val thisTime = tuple.get._2
          (thisTime == low || thisTime.isAfter(low)) && thisTime.isBefore(high)
        } else false
      })
      .map(_.get._1)
    legs.filter(srcVerticesInTimeRange contains _.srcVertex).map(expFn)
  }

  /**
    * construct OD Pair objects from any trips in the range [low, high)
    * @param low lower bound, inclusive
    * @param high upper bound, exclusive
    * @return
    */
  def unpackTrips(low: LocalTime, high: LocalTime): ODPairs =
    exportLegsByTimeGroup[SimpleMSSP_ODPair](asODPair)(low, high)


  def asODPair(leg: UnroutedLeg): SimpleMSSP_ODPair =
    SimpleMSSP_ODPair(
      id,
      leg.srcVertex,
      leg.dstVertex
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
