package cse.fitzgero.sorouting.matsimrunner.population

import cse.fitzgero.sorouting.roadnetwork.vertex.Euclidian
import org.apache.spark.graphx.VertexId

import scala.xml.NodeSeq

object ActivityLocation {

  val random = new scala.util.Random(System.currentTimeMillis)
  def setSeed(s: Long): Unit = random.setSeed(s)

  def pickRandomLocation(activities: ActivityLocations): (VertexId, Euclidian) = {
    activities(random.nextInt(activities.length))
  }

  def takeAllLocations(g: xml.Elem): ActivityLocations = {
//    require((g \ "links").nonEmpty)
    require((g \ "nodes").nonEmpty)
//    val nodes: Map[String, Map[String, String]] = (g \ "nodes" \ "node").map(node => node.attribute("id").get.text -> node.attributes.asAttrMap).toMap
//    val edges = g \ "links" \ "link"
//    edges.map(e => {
//      val srcId: String = e.attribute("from").get.text
//      (e.attribute("id").get.text,
//        Euclidian(nodes(srcId)("x").toDouble, nodes(srcId)("y").toDouble))
//    }).toArray
    val nodes: NodeSeq = g \ "nodes" \ "node"
    nodes.map(node => {
      (
        node.attribute("id").get.text.toLong,
        Euclidian(
          node.attribute("x").get.text.toDouble,
          node.attribute("y").get.text.toDouble
        )
      )
    }).toArray
  }
}