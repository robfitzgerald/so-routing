package cse.fitzgero.sorouting.matsimrunner.population

case class RoutedLeg(mode: String = "car", srcVertex: Long, dstVertex: Long, srcLink: String, dstLink: String, path: List[String]) extends MATSimLeg with ConvertsToXml {
  // is it correct to bookend the path with the starting values? that's assuming that our SSSP only outputs the connecting edges
  // for what is currently running as a vertex-oriented search, that is not the case
  override def toXml: xml.Elem =
    <leg mode={mode}><route type="links">{bookendedPath.mkString(" ")}</route></leg>
    private def bookendedPath: List[String] =
      if (path.isEmpty)
        List()
      else
        (if (path.head != srcLink) List(srcLink) else List.empty[String]) :::
          path :::
          (if (path.last != dstLink) List(dstLink) else List.empty[String])

  //  override def toXml: xml.Elem =
  //    <leg mode={mode}><route type="links">{path.mkString(" ")}</route></leg>
}
