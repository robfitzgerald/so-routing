package cse.fitzgero.sorouting.matsimrunner.population
import scala.xml.Elem

case class LegNode(mode: String = "car") extends ConvertsToXml {
  override def toXml: Elem = <leg mode={mode}></leg>
}
