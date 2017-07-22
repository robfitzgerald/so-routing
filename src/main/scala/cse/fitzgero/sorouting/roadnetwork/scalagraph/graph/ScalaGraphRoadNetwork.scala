package cse.fitzgero.sorouting.roadnetwork.scalagraph.graph

import java.io.IOException

import cse.fitzgero.sorouting.roadnetwork.costfunction.{CostFunctionAttributes, CostFunctionFactory}

import scala.util.{Failure, Success, Try}
import scala.xml.{Elem, XML}
import scalax.collection.Graph
import scalax.collection.GraphPredef._
import scalax.collection.edge.WDiEdge
import scalax.collection.edge.Implicits._
import cse.fitzgero.sorouting.roadnetwork.graph._
import cse.fitzgero.sorouting.roadnetwork.scalagraph.edge._
import cse.fitzgero.sorouting.roadnetwork.scalagraph.vertex._

/**
  * Builds Road Networks from file sources
  */
class ScalaGraphRoadNetwork (costFunctionFactory: CostFunctionFactory, algorithmFlowRate: Double = 3600D) extends CanReadNetworkFiles[Graph[VertexId, Edge]] with CanReadFlowSnapshotFiles[Graph[VertexId, Edge]] {
  val MATSimFlowRate = 3600D // vehicles per hour is used to represent flow data
  val NoFlowData: xml.Elem = <network><links></links></network>

  override def fromFile(fileName: String): Try[Graph[Long, Edge]] =
    Try ({
      XML.loadFile(fileName)
    }) match {
      case Failure(e) => throw new IOException(s"$fileName is not a valid network filename. \n ${e.getStackTrace}")
      case Success(file: Elem) =>
        val nodes = grabVertices(file)
        val edges = grabEdges(file, NoFlowData)
        Try(Graph.from(nodes.map(_._1), edges))
    }

  override def fromFileAndSnapshot(networkFilePath: String, snapshotFilePath: String): Try[Graph[Long, Edge]] = ???


  /**
    * assuming an xml element which contains a "nodes" collection of "node" tags with "x" and "y" attributes, constructs a list of those as vertices
    * @param file xml data containing the list of node data
    * @return
    */
  def grabVertices(file: Elem): Seq[(VertexId, CoordinateVertexProperty)] =
    for (node <- file \ "nodes" \ "node") yield {
      val attrs = node.attributes.asAttrMap
      val x = attrs.getOrElse("x", "0").toDouble
      val y = attrs.getOrElse("y", "0").toDouble
      val id = attrs.getOrElse("id", "-1").toLong
      (id, CoordinateVertexProperty(Euclidian(x, y)))
    }

  def grabEdges(file: Elem, flowData: Elem): Seq[Edge[VertexId]] =
    Try({
      val links: xml.NodeSeq = flowData \ "links" \ "link"
      if (links.isEmpty) Map.empty[String, Double]
      else links.map(link => ((link \ "@id").toString, (link \ "@flow").toString.toDouble)).toMap
    }) match {
      case Failure(err) => throw new IOException(s"snapshot flow data was malformed in ${flowData.toString}\n$err")
      case Success(linkFlows: Map[String, Double]) =>
        for (link <- file \ "links" \ "link") yield {
          val linkData: Map[String,String] = link.attributes.asAttrMap
          val linkId: String = linkData("id").toString
          val allAttrs: Map[String,String] = linkData.updated("flow", linkFlows.getOrElse(linkId, 0D).toString)
          val attrsObject: CostFunctionAttributes = CostFunctionAttributes(
            allAttrs.getOrElse("capacity", "100").toDouble,
            allAttrs.getOrElse("freespeed", "50").toDouble,
            allAttrs("flow").toDouble,
            MATSimFlowRate,
            algorithmFlowRate
          )
          val from = allAttrs("from").toLong
          val to = allAttrs("to").toLong
          val weight = MacroscopicEdgeProperty(linkId, 0D, costFunctionFactory(attrsObject))
        Edge(from, to, weight) }
    }
}

object ScalaGraphRoadNetwork {
  def apply (costFunctionFactory: CostFunctionFactory, algorithmFlowRate: Double = 3600D) =
    new ScalaGraphRoadNetwork(costFunctionFactory, algorithmFlowRate)
}