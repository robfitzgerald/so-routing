package cse.fitzgero.sorouting.matsimrunner.population
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
    if (legIdx == -1) this
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
  override def toXml: Elem =
    <person id={id}>
      {homeAM.toXml}
      {stripedLegsAndActivities.map(_.toXml)}
      {homePM.toXml}
    </person>
}
