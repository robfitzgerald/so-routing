package cse.fitzgero.sorouting.model.roadnetwork.local

import cse.fitzgero.sorouting.model.roadnetwork.costfunction._

object LocalGraphOps {

  def readMATSimXML(network: xml.Elem, snapshotXML: Option[xml.Elem] = None, costFunctionType: CostFunctionType = BPRCostFunctionType, algorithmFlowRate: Double = 10D): LocalGraph = {

    // get (optional) flow data in xml snapshot format
    val flowData: Map[String, Double] = snapshotXML match {
      case Some(snapshot) =>
        val links: xml.NodeSeq = snapshot \ "links" \ "link"
        if (links.isEmpty)
          Map.empty[String, Double]
        else
          links.map(link => ((link \ "@id").toString, (link \ "@flow").toString.toDouble)).toMap
      case None =>
        Map.empty[String, Double]
    }

    // add vertices to an empty graph
    val networkFileVertices = network \ "nodes" \ "node"
    val verticesGraph: LocalGraph = networkFileVertices.foldLeft(LocalGraph())((graph, node) => {
      val attrs: Map[String,String] = node.attributes.asAttrMap
      val name: String = attrs("id").toString
      val x: Double = attrs("x").toDouble
      val y: Double = attrs("y").toDouble
      graph.updateVertex(name, LocalVertex(name, x, y))
    })

    // add edges to the graph with vertices in it, and return the result
    val networkFileEdges = network \ "links" \ "link"
    networkFileEdges.foldLeft(verticesGraph)((graph, link) => {

      val linkData: Map[String, String] = link.attributes.asAttrMap
      val id: String = linkData("id").toString
      val src: String = linkData("from").toString
      val dst: String = linkData("to").toString
      val flow: Option[Double] = flowData.get(id)
      val capacity: Option[Double] = linkData.get("capacity").map(_.toDouble)
      val freespeed: Option[Double] = linkData.get("freespeed").map(_.toDouble)
      val length: Option[Double] = linkData.get("length").map(_.toDouble)

      val edge: LocalEdge = LocalEdge(id, src, dst, flow, capacity, freespeed, length, Some(costFunctionType), algorithmFlowRate)

      graph.updateEdge(id, edge)
    })
  }
}
