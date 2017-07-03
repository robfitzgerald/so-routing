package cse.fitzgero.sorouting.matsimrunner.population
import cse.fitzgero.sorouting.roadnetwork.edge.EdgeIdType

import scala.xml.Elem

case class LegNode(mode: String = "car", source: EdgeIdType, destination: EdgeIdType, path: List[EdgeIdType] = List.empty[EdgeIdType]) extends ConvertsToXml {
  override def toXml: Elem =
    if (path.isEmpty) <leg mode={mode}></leg>
    else <leg mode={mode}><route type="links">{path.mkString(" ")}</route></leg>
}