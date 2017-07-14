package cse.fitzgero.sorouting.matsimrunner.population
import java.time.LocalTime

import cse.fitzgero.sorouting.roadnetwork.edge.EdgeIdType
import org.apache.spark.graphx.VertexId

import scala.xml.Elem

case class LegNode(mode: String = "car", srcVertex: VertexId, dstVertex: VertexId, srcLink: EdgeIdType, dstLink: EdgeIdType, path: List[EdgeIdType] = List.empty[EdgeIdType]) extends ConvertsToXml {
  override def toXml: Elem =
    if (path.isEmpty) <leg mode={mode}></leg>
    else <leg mode={mode}><route type="links">{path.mkString(" ")}</route></leg>
}