package cse.fitzgero.sorouting.matsimrunner.population
import cse.fitzgero.sorouting.roadnetwork.edge.EdgeIdType

import scala.xml.Elem

case class PersonNode (id: String, mode: String, homeAM: MorningActivity, work: List[MiddayActivity], homePM: EveningActivity, legs: List[LegNode] = List.empty[LegNode]) extends ConvertsToXml {
  val legList: List[LegNode] =
    if (legs.nonEmpty) legs
    else
      (homeAM.link +: work.map(_.link) :+ homePM.link)
        .sliding(2).toList
        .map((odPair) => LegNode(mode, odPair(0), odPair(1)))

  def updatePath(src: EdgeIdType, dst: EdgeIdType, path: List[EdgeIdType]): PersonNode = {
    val legIdx: Int = legList.indexWhere(leg => leg.source == src && leg.destination == dst)
    if (legIdx == -1) this
    else {
      val newLegVal: LegNode = legList(legIdx).copy(path = path)
      this.copy(legs = legList.updated(legIdx, newLegVal))
    }
  }
  def stripedLegsAndActivities: Seq[ConvertsToXml] = {
    if (work.isEmpty) Seq()
    else {
      val legNodes: List[LegNode] =
        if (legList.nonEmpty) legList
        else
        // constructs a default set of LegNode objects based on the links where each activity occurs
          (homeAM.link +: work.map(_.link) :+ homePM.link)
            .sliding(2).toList
            .map((odPair) => LegNode(mode, odPair(0), odPair(1)))
      def stripe(remLegs: List[ConvertsToXml], remWork: List[ConvertsToXml]): List[ConvertsToXml] = {
        if (remWork.isEmpty) List(remLegs.head)
        else remLegs.head :: remWork.head :: stripe(remLegs.tail, remWork.tail)
      }
      stripe(legNodes, work)
    }
  }
  override def toXml: Elem =
    <person id={id}>
      {homeAM.toXml}
      {stripedLegsAndActivities.map(_.toXml)}
      {homePM.toXml}
    </person>
}
