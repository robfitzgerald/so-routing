package cse.fitzgero.sorouting.roadnetwork.graph

import java.io.IOException

import scala.xml.{Elem, XML}
import scala.util.{Failure, Success, Try}
import org.apache.spark.{SparkContext, graphx}
import org.apache.spark.graphx._
import cse.fitzgero.sorouting.roadnetwork._
import cse.fitzgero.sorouting.roadnetwork.edge._
import cse.fitzgero.sorouting.roadnetwork.path.Path
import cse.fitzgero.sorouting.roadnetwork.vertex._
import org.apache.spark.rdd.RDD

class GraphXMacroRoadNetwork (val g: Graph[CoordinateVertexProperty, MacroscopicEdgeProperty]) extends RoadNetworkWrapper[CoordinateVertexProperty, MacroscopicEdgeProperty, graphx.VertexId] {
  type IdType = graphx.VertexId
  def getCostFlowValues (linkIds: Seq[IdType]): Seq[Double] = ???
  def shortestPath (OD: Seq[(CoordinateVertexProperty, CoordinateVertexProperty)]): Seq[Path[IdType]] = ???
}

case class GraphXMacroFactory (sc: SparkContext) extends CanReadNetworkFiles with CanReadFlowSnapshotFiles {
  def fromFile (fileName : String): Try[GraphXMacroRoadNetwork] = {
    Try ({
      XML.loadFile(fileName)
    }) match {
      case Failure(err) => throw new IOException(s"$fileName is not a valid network filename. \n ${err.getStackTrace}")
      case Success(file: Elem) =>
        Try({
          val noSnapshotData: Elem = <network><links></links></network>
          val edgeSet: RDD[Edge[MacroscopicEdgeProperty]] = grabEdges(file, noSnapshotData)
          val vertexSet: RDD[(VertexId, CoordinateVertexProperty)] = grabVertices(file)
          new GraphXMacroRoadNetwork(Graph[CoordinateVertexProperty, MacroscopicEdgeProperty](vertexSet, edgeSet))
        })
    }
  }

  def fromFileAndSnapshot (networkFileName: String, snapshotFileName: String): Try[GraphXMacroRoadNetwork] = {
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
              new GraphXMacroRoadNetwork(Graph[CoordinateVertexProperty, MacroscopicEdgeProperty](vertexSet, edgeSet))
            })
        }
    }
  }

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
          val attrs: Map[String,String] = link.attributes.asAttrMap
          Edge(
            srcId = attrs("from").toLong,
            dstId = attrs("to").toLong,
            attr = MacroscopicEdgeProperty(
              attrs("id"),
              linkFlows.getOrElse(attrs("id"), 0D),
              (_: Double) => 10D)) // TODO: this should be a valid cost function from (marginal ?) flow to cost
        })
    }
  }

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