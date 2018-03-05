package cse.fitzgero.sorouting.model.roadnetwork.local

import cse.fitzgero.sorouting.model.path.SORoutingPathSegment
import cse.fitzgero.sorouting.model.roadnetwork.costfunction._

object LocalGraphOps {

  sealed trait GraphFlowType
  case object EdgesWithFlows extends GraphFlowType
  case object EdgesWithDrivers extends GraphFlowType

  /**
    * loads a network.xml and optional snapshot.xml and produces a LocalGraph
    * @param graphFlowType EdgesWithFlows holds and updates macroscopic flow values; EdgesWithDrivers holds and updates Sets of Drivers
    * @param network the MATSim XML network file
    * @param snapshotXML an optional snapshot, to load flow data
    * @param costFunctionType a trait describing how we evaluate link attributes to produce cost function values
    * @param algorithmFlowRate the number of seconds we are using for a time window, to calculate flow from distance over time units and to compare them to each other
    * @return
    */
  def readMATSimXML(graphFlowType: GraphFlowType, network: xml.Elem, snapshotXML: Option[xml.Elem] = None, costFunctionType: CostFunctionType = BPRCostFunctionType, algorithmFlowRate: Double = 10D): LocalGraph = {

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
      val capacity: Option[Double] = linkData.get("capacity").map(toPosNonZeroValue)
      val freespeed: Option[Double] = linkData.get("freespeed").map(toPosNonZeroValue)
      val length: Option[Double] = linkData.get("length").map(safeDistance)

      val edge: LocalEdge = graphFlowType match {
        case EdgesWithFlows => LocalEdge(id, src, dst, flow, capacity, freespeed, length, Some(costFunctionType), algorithmFlowRate)
        case EdgesWithDrivers => LocalEdge.ofDrivers(id, src, dst, capacity, freespeed, length, Some(costFunctionType), algorithmFlowRate)
      }

      graph.updateEdge(id, edge)
    })
  }

  val MinNetworkDistance: Double = 10D

  def safeDistance(d: String): Double = {
    val value = d.toDouble
    if (value <= MinNetworkDistance) MinNetworkDistance else value
  }

  private def toPosNonZeroValue(d: String): Double = {
    val value = d.toDouble
    if (value <= 0D) Double.MinPositiveValue else value
  }

  /**
    * adds flows to a graph
    * @param edgesAndFlows edge ids and flows to add to that edge
    * @param graph the graph to modify
    * @return an updated graph
    */
  def updateGraph(edgesAndFlows: Map[SORoutingPathSegment.EdgeId, Int], graph: LocalGraph): LocalGraph = {
    edgesAndFlows.foldLeft(graph){
      (g, data) => {
        g.edgeById(data._1) match {
          case None => g
          case Some(e) =>
            val updated = LocalEdge.modifyFlow(e, data._2)
            g.updateEdge(data._1, updated)
        }
      }
    }
  }
}
