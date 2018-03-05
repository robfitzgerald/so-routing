package cse.fitzgero.sorouting.matsimrunner.network

import scala.xml.XML

import cse.fitzgero.sorouting.model.roadnetwork.local.LocalGraphOps

case class Node(id: String, x: Double, y: Double)
case class Link(id: String, from: String, to: String, length: Double, freespeed: Double, capacity: Double)
case class Network(nodes: Map[String, Node], links: Map[String, Link])

object MATSimNetworkToCollection {
  def apply(network: xml.Elem): Network = {
    require((network \ "nodes").nonEmpty)
    require((network \ "nodes" \ "node").nonEmpty)
    require((network \ "links").nonEmpty)
    require((network \ "links" \ "link").nonEmpty)

    val nodes = (network \ "nodes" \ "node")
      .aggregate(Map.empty[String, Node])(
        (nodes, node) => {
          val attrs: Map[String, String] = node.attributes.asAttrMap
          val id: String = attrs("id")
          val x: Double = attrs("x").toDouble
          val y: Double = attrs("y").toDouble
          nodes.updated(id, Node(id, x, y))
        },
        (a, b) => a ++ b
      )

    def euclidianDistance(a: Node, b: Node): Double =
      math.sqrt(((a.x - b.x) * (a.x - b.x)) + ((a.y - b.y) * (a.y - b.y)))

    val links = (network \ "links" \ "link")
      .aggregate(Map.empty[String, Link])(
        (links, link) => {
          val attrs: Map[String, String] = link.attributes.asAttrMap
          val id: String = attrs("id")
          val from: String = attrs("from")
          val to: String = attrs("to")
          val length: Double =
            if (attrs.isDefinedAt("length")) LocalGraphOps.safeDistance(attrs("length"))
            else euclidianDistance(nodes(from), nodes(to))
          val freespeed: Double = attrs("freespeed").toDouble
          val capacity: Double = attrs("capacity").toDouble

          links.updated(id, Link(id, from, to, length, freespeed, capacity))
        },
        (a, b) => a ++ b
      )

    Network(nodes, links)
  }

  def apply(path: String): Network =
    apply(XML.load(new java.io.InputStreamReader(new java.io.FileInputStream(path), "UTF-8")))
}
