package cse.fitzgero.sorouting.matsimrunner.population

import scala.xml.Elem

case class UnroutedLeg(mode: String = "car", srcVertex: Long, dstVertex: Long, srcLink: String, dstLink: String, path: List[String] = Nil) extends MATSimLeg with ConvertsToXml {
  override def toXml: Elem =
      <leg mode={mode}></leg>
}
