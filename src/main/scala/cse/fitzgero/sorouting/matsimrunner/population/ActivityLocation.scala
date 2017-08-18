package cse.fitzgero.sorouting.matsimrunner.population

import cse.fitzgero.sorouting.roadnetwork.vertex.Euclidian

object ActivityLocation {

  val random = new scala.util.Random(System.currentTimeMillis)
  def setSeed(s: Long): Unit = random.setSeed(s)

  def takeRandomLocation(g: xml.Elem): ActivityData = {
    require((g \ "nodes").nonEmpty)
    require((g \ "links").nonEmpty)
    val link: xml.Node = (g \ "links" \ "link")(random.nextInt((g \ "links" \ "link").length))
    val linkSrc: String = link.attribute("from").get.text
    val node: xml.Node = (g \ "nodes" \ "node")
      .find(_.attribute("id").get.text == linkSrc) match {
      case Some(n) => n
      case _ => throw new NoSuchElementException(s"failed to find node $linkSrc associated with the link ${link.toString}")
    }
    (
      node.attribute("id").get.text.toLong,
      Euclidian(
        node.attribute("x").get.text.toDouble,
        node.attribute("y").get.text.toDouble
      ),
      link.attribute("id").get.text
    )
  }
}