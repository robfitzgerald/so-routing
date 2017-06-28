package cse.fitzgero.sorouting.matsimrunner.population
import scala.xml.Elem

case class PersonNode (id: String, mode: String, homeAM: MorningActivity, work: MiddayActivity, homePM: EveningActivity) extends PopulationDataThatConvertsToXml {
  val leg = LegNode(mode)
  override def toXml: Elem = <person id={id}>{homeAM.toXml}{leg.toXml}{work.toXml}{leg.toXml}{homePM.toXml}</person>
}
