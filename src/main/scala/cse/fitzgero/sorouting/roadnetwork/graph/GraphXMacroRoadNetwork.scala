package cse.fitzgero.sorouting.roadnetwork.graph

import java.io.IOException

import scala.xml.{Elem, XML}
import scala.util.{Failure, Success, Try}
import org.apache.spark.SparkContext
import org.apache.spark.graphx.{Graph, _}
import cse.fitzgero.sorouting.roadnetwork.costfunction._
import cse.fitzgero.sorouting.roadnetwork.edge.{MacroscopicEdgeProperty, _}
import cse.fitzgero.sorouting.roadnetwork.vertex.{CoordinateVertexProperty, _}
import org.apache.spark.rdd.RDD

case class GraphXMacroRoadNetwork (sc: SparkContext, costFunctionFactory: CostFunctionFactory) extends CanReadNetworkFiles with CanReadFlowSnapshotFiles {
  type RoadNetwork = Graph[CoordinateVertexProperty, MacroscopicEdgeProperty]

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
          val attrs: Map[String,String] = link.attributes.asAttrMap

          Edge(
            srcId = attrs("from").toLong,
            dstId = attrs("to").toLong,
            attr = MacroscopicEdgeProperty(
              attrs("id"),
              linkFlows.getOrElse(attrs("id"), 0D),
              costFunctionFactory(attrs).generate
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

// failed at defining the generic trait correctly:
// see graph.HasRoadNetworkOps
object GraphXMacroRoadNetwork {
  def getCostFlowValues[RoadNetwork](graph: RoadNetwork, samplePercentage: Double): Seq[Double] = ???
  def shortestPath (graph: Graph[CoordinateVertexProperty, MacroscopicEdgeProperty], odPairs: Seq[(VertexId, VertexId)]): (Graph[Map[VertexId, Double], MacroscopicEdgeProperty], Seq[(VertexId, VertexId, List[String])]) = ???
}
