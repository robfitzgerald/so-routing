package cse.fitzgero.sorouting.matsimrunner.population

import cse.fitzgero.sorouting.roadnetwork.graphx.edge.EdgeIdType
import org.apache.spark.graphx.VertexId

import scala.xml.Elem

case class LegNode(mode: String = "car", srcVertex: VertexId, dstVertex: VertexId, srcLink: EdgeIdType, dstLink: EdgeIdType, path: List[EdgeIdType] = List.empty[EdgeIdType]) extends MATSimLeg with ConvertsToXml {
  override def toXml: Elem =
    if (path.isEmpty) <leg mode={mode}></leg>
    else <leg mode={mode}><route type="links">{bookendedPath.mkString(" ")}</route></leg>
  private def bookendedPath: List[EdgeIdType] =
    (if (path.head != srcLink) List(srcLink) else List.empty[EdgeIdType]) :::
    path :::
      (if (path.last != dstLink) List(dstLink) else List.empty[EdgeIdType])
}