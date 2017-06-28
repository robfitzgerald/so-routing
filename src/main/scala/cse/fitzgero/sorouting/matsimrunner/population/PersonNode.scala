package cse.fitzgero.sorouting.matsimrunner.population
import scala.xml.Elem

case class PersonNode (id: String, mode: String, homeAM: ActivityNode, work: ActivityNode, homePM: ActivityNode) extends PopulationDataThatConvertsToXml {
  val leg = LegNode(mode)
  override def toXml: Elem = ???
}
