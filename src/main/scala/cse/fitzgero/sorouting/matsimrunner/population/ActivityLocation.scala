package cse.fitzgero.sorouting.matsimrunner.population

import cse.fitzgero.sorouting.roadnetwork.graphx.vertex.Euclidian
import org.apache.spark.graphx.VertexId

import scala.xml.NodeSeq

object ActivityLocation {

  val random = new scala.util.Random(System.currentTimeMillis)
  def setSeed(s: Long): Unit = random.setSeed(s)

//  def pickRandomLocation(activities: ActivityLocations): ActivityData = {
//    activities(random.nextInt(activities.length))
//  }

//  def takeAllLocations(g: xml.Elem): ActivityLocations = {
//    require((g \ "nodes").nonEmpty)
//    val nodes: NodeSeq = g \ "nodes" \ "node"
//    nodes.map(node => {
//      (
//        node.attribute("id").get.text.toLong,
//        Euclidian(
//          node.attribute("x").get.text.toDouble,
//          node.attribute("y").get.text.toDouble
//        )
//      )
//    }).toArray
//  }

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

  // given a road network g as a matsim network_v2 file,
  //   select a random link
  //   grab the x and y from the node associated with the "from" attribute of this link
  //   compile these as an activity location of the form
  //     (linkId, Euclidian(x, y))
}