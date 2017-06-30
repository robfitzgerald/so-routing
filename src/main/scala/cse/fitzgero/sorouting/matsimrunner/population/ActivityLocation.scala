package cse.fitzgero.sorouting.matsimrunner.population

import cse.fitzgero.sorouting.roadnetwork.edge.EdgeIdType
import cse.fitzgero.sorouting.roadnetwork.vertex.Euclidian

object ActivityLocation {

  val random = new scala.util.Random(System.currentTimeMillis)
  def setSeed(s: Long): Unit = random.setSeed(s)

  def pickRandomLocation(activities: ActivityLocations): (EdgeIdType, Euclidian) = {
    activities(random.nextInt(activities.length))
  }

  def takeAllLocations(g: xml.Elem): ActivityLocations = {
    require((g \ "links").nonEmpty)
    require((g \ "nodes").nonEmpty)
    val nodes: Map[String, Map[String, String]] = (g \ "nodes" \ "node").map(node => node.attribute("id").get.text -> node.attributes.asAttrMap).toMap
    val edges = g \ "links" \ "link"
    edges.map(e => {
      val srcId: String = e.attribute("from").get.text
      (e.attribute("id").get.text,
        Euclidian(nodes(srcId)("x").toDouble, nodes(srcId)("y").toDouble))
    }).toArray
  }
}