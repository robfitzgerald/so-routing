package cse.fitzgero.sorouting.matsimrunner.population
import scala.xml.Elem

case class PersonNode (id: String, mode: String, homeAM: MorningActivity, work: Seq[MiddayActivity], homePM: EveningActivity) extends ConvertsToXml {
  val leg = LegNode(mode)
  override def toXml: Elem =
    <person id={id}>
      {homeAM.toXml}
      {if (work.nonEmpty) leg.toXml}
      {work.map(act => {act.toXml ++ leg.toXml})}
      {homePM.toXml}
    </person>
}
