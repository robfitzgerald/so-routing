package cse.fitzgero.sorouting.roadnetwork.graph

import java.io.IOException

import cse.fitzgero.sorouting.algorithm.shortestpath.ODPaths

import scala.xml.{Elem, XML}
import scala.util.{Failure, Success, Try}
import org.apache.spark.SparkContext
import org.apache.spark.graphx.{Graph, _}
import cse.fitzgero.sorouting.roadnetwork.costfunction._
import cse.fitzgero.sorouting.roadnetwork.edge.{MacroscopicEdgeProperty, _}
import cse.fitzgero.sorouting.roadnetwork.vertex.{CoordinateVertexProperty, _}
import org.apache.spark.rdd.RDD

class GraphXMacroRoadNetwork (sc: SparkContext, costFunctionFactory: CostFunctionFactory, algorithmFlowRate: Double = 3600D) extends CanReadNetworkFiles with CanReadFlowSnapshotFiles {
  val MATSimFlowRate = 3600D // vehicles per hour is used to represent flow data

  /**
    * loads a road network from an xml file written in MATSim's network format
    * @note all network flows will be zeroes
    * @param fileName location of file
    * @return
    */
  def fromFile (fileName : String): Try[RoadNetwork] = {
    Try ({
      XML.loadFile(fileName)
    }) match {
      case Failure(err) => throw new IOException(s"$fileName is not a valid network filename. \n ${err.getStackTrace}")
      case Success(file: Elem) =>
        Try({
          val noSnapshotData: Elem = <network><links></links></network>
          val edgeSet: RDD[Edge[MacroscopicEdgeProperty]] = grabEdges(file, noSnapshotData)
          val vertexSet: RDD[(VertexId, CoordinateVertexProperty)] = grabVertices(file)
          Graph[CoordinateVertexProperty, MacroscopicEdgeProperty](vertexSet, edgeSet)
        })
    }
  }

  /**
    * loads a road network from an xml file written in MATSim's network format and combines it with network flows from a snapshot file written in a locally described format
    * @param networkFileName location of network file
    * @param snapshotFileName location of snapshot file
    * @return
    */
  def fromFileAndSnapshot (networkFileName: String, snapshotFileName: String): Try[RoadNetwork] = {
    Try ({
      XML.loadFile(networkFileName)
    }) match {
      case Failure(err) => throw new IOException(s"$networkFileName is not a valid network filename. \n ${err.getStackTrace}")
      case Success(file: Elem) =>
        Try ({
          XML.loadFile(snapshotFileName)
        }) match {
          case Failure(err) => throw new IOException(s"$snapshotFileName is not a valid snapshot filename. \n ${err.getStackTrace}")
          case Success(flows: Elem) =>
            Try({
              val edgeSet: RDD[Edge[MacroscopicEdgeProperty]] = grabEdges(file, flows)
              val vertexSet: RDD[(VertexId, CoordinateVertexProperty)] = grabVertices(file)
              Graph[CoordinateVertexProperty, MacroscopicEdgeProperty](vertexSet, edgeSet)
            })
        }
    }
  }

  /**
    * processes xml network and snapshot data to produce the graph's edge list
    * @param xmlData network xml element
    * @param flowData snapshot xml element
    * @return
    */
  private def grabEdges (xmlData: Elem, flowData: Elem): RDD[Edge[MacroscopicEdgeProperty]] = {
    require((xmlData \ "links").nonEmpty)
    require((xmlData \ "links" \ "link").nonEmpty)
    Try({
      val links: xml.NodeSeq = flowData \ "links" \ "link"
      if (links.isEmpty) Map.empty[String, Double]
      else links.map(link => ((link \ "@id").toString, (link \ "@flow").toString.toDouble)).toMap
    }) match {
      case Failure(err) => throw new IOException(s"snapshot flow data was malformed in ${flowData.toString}\n$err")
      case Success(linkFlows: Map[String, Double]) =>
        sc.parallelize(for (link <- xmlData \ "links" \ "link") yield {
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
          Edge(
            srcId = allAttrs("from").toLong,
            dstId = allAttrs("to").toLong,
            attr = MacroscopicEdgeProperty(
              linkId,
              0D,
              costFunctionFactory(attrsObject)
          ))
        })
    }
  }



  /**
    * processes xml network to produce the graph's vertex list
    * @param xmlData network xml element
    * @return
    */
  private def grabVertices (xmlData: Elem): RDD[(VertexId, CoordinateVertexProperty)] = {
    require((xmlData \ "nodes").nonEmpty)
    require((xmlData \ "nodes" \ "node").nonEmpty)
    sc.parallelize(for (node <- xmlData \ "nodes" \ "node") yield {
      val attrs: Map[String,String] = node.attributes.asAttrMap
      val name: VertexId = attrs("id").toLong
      val x: Double = attrs("x").toDouble
      val y: Double = attrs("y").toDouble
      val prop: CoordinateVertexProperty = CoordinateVertexProperty(position = Euclidian(x, y))
      (name, prop)
    })
  }
}


object GraphXMacroRoadNetwork {
  def apply (sc: SparkContext, costFunctionFactory: CostFunctionFactory): GraphXMacroRoadNetwork = new GraphXMacroRoadNetwork(sc, costFunctionFactory)
  /**
    * updates the flow values for edges based on a set of path assignments
    * @param graph a road network
    * @param paths tuples where the 3rd element is a list of edge Ids
    * @return a new road network with those values assigned to the flow attributes of the edges
    */
  def updateEdges(graph: RoadNetwork, paths: ODPaths): Graph[CoordinateVertexProperty, MacroscopicEdgeProperty] = {
    val updateList: Map[EdgeIdType, Double] = paths.flatMap(_.path).groupBy(identity).map(group => (group._1, group._2.size.toDouble))
    graph.mapEdges  (edge =>
      if (updateList.isDefinedAt(edge.attr.id))
        edge.attr.copy(
          flow = edge.attr.flow + updateList(edge.attr.id)
        )
      else edge.attr
    )
  }
}
