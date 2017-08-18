package cse.fitzgero.sorouting.matsimrunner.population

case class RoutedLeg(mode: String = "car", srcVertex: Long, dstVertex: Long, srcLink: String, dstLink: String, path: List[String]) extends MATSimLeg with ConvertsToXml {
  override def toXml: xml.Elem =
    <leg mode={mode}><route type="links">{bookendedPath.mkString(" ")}</route></leg>
  private def bookendedPath: List[String] =
    (if (path.head != srcLink) List(srcLink) else List.empty[String]) :::
      path :::
      (if (path.last != dstLink) List(dstLink) else List.empty[String])
}
