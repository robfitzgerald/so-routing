package cse.fitzgero.sorouting.roadnetwork.localgraph

import java.io.IOException

import cse.fitzgero.sorouting.roadnetwork.costfunction._
import cse.fitzgero.sorouting.roadnetwork.graph._
import cse.fitzgero.sorouting.roadnetwork.edge._
import cse.fitzgero.sorouting.roadnetwork.vertex._

import scala.util.{Failure, Success, Try}
import scala.xml.{Elem, NodeSeq}

class LocalGraphMATSimFactory (
  var costFunctionFactory: CostFunctionFactory,
  var MATSimFlowRate: Double,
  var AlgorithmFlowRate: Double
) extends
  CanReadNetworkFiles[LocalGraphMATSim] with
  CanReadFlowSnapshotFiles[LocalGraphMATSim] {

  override def fromFile(fileName: String): Try[LocalGraphMATSim] =
    fromFileAndSnapshot(fileName)

  override def fromFileAndSnapshot(networkFilePath: String, snapshotFilePath: String = ""): Try[LocalGraphMATSim] = {
    Try[xml.Elem]({
      if (snapshotFilePath == "") <network><links></links></network>
      else xml.XML.loadFile(snapshotFilePath)
    }) match {
      case Failure(err) => throw new IOException(s"unable to read snapshot file $snapshotFilePath")
      case Success(snapshot) =>
        Try({
          val network = xml.XML.loadFile(networkFilePath)
          val newGraph = LocalGraphMATSim()
          val nodeList = grabVertices(newGraph, network)
          grabEdges(nodeList, network, snapshot)
        })
    }
  }

  private def grabVertices(graph: LocalGraphMATSim, xmlData: Elem): LocalGraphMATSim = {
    require((xmlData \ "nodes").nonEmpty)
    require((xmlData \ "nodes" \ "node").nonEmpty)
    (xmlData \ "nodes" \ "node").foldLeft(graph)((graph, node) => {
      val attrs: Map[String,String] = node.attributes.asAttrMap
      val name: VertexId = attrs("id").toLong
      val x: Double = attrs("x").toDouble
      val y: Double = attrs("y").toDouble
      val prop: VertexMATSim = CoordinateVertexProperty(position = Euclidian(x, y))
      graph.addVertex(name, prop).asInstanceOf[LocalGraphMATSim]
    })
  }

  private def grabEdges(graph: LocalGraphMATSim, xmlData: Elem, flowData: Elem): LocalGraphMATSim = {
    Try({
      val links: xml.NodeSeq = flowData \ "links" \ "link"
      if (links.isEmpty) Map.empty[String, Double]
      else links.map(link => ((link \ "@id").toString, (link \ "@flow").toString.toDouble)).toMap
    }) match {
      case Failure(err) => throw new IOException(s"snapshot flow data was malformed in ${flowData.toString}\n$err")
      case Success(linkFlows: Map[String, Double]) =>
        (xmlData \ "links" \ "link").foldLeft(graph)((acc, link) => {
          val linkData: Map[String, String] = link.attributes.asAttrMap
          val linkId: String = linkData("id").toString
          val attrsObject: CostFunctionAttributes = CostFunctionAttributes(
            linkData.getOrElse("capacity", "100").toDouble,
            linkData.getOrElse("freespeed", "50").toDouble,
            linkFlows.getOrElse(linkId, 0D),
            MATSimFlowRate,
            AlgorithmFlowRate
          )
          val newTriplet: Triplet =
            Triplet(
              o = linkData("from").toLong,
              e = linkId,
              d = linkData("to").toLong
            )
          acc.addEdge(
            newTriplet,
            MacroscopicEdgeProperty(
              linkId,
              0D,
              costFunctionFactory(attrsObject)
            )
          )
        })
    }
  }
}

object LocalGraphMATSimFactory{
  def apply(
    costFunctionFactory: CostFunctionFactory,
    MATSimFlowRate: Double = 3600D,
    AlgorithmFlowRate: Double = 3600D): LocalGraphMATSimFactory =
    new LocalGraphMATSimFactory(costFunctionFactory, MATSimFlowRate, AlgorithmFlowRate)
}