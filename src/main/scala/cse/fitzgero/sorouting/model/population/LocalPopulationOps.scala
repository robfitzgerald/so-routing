package cse.fitzgero.sorouting.model.population

import java.time.LocalTime
import java.time.format.DateTimeFormatter

import cse.fitzgero.graph.population.BasicPopulationOps
import cse.fitzgero.sorouting.model.path.SORoutingPathSegment
import cse.fitzgero.sorouting.model.roadnetwork.local.{LocalGraph, LocalODPair}
import scala.annotation.tailrec
import scala.collection.{GenMap, GenSeq}
import scala.util.Random
import scala.util.matching.Regex
import scala.xml.XML

object LocalPopulationOps extends BasicPopulationOps {
  // graph types
  override type EdgeId = String
  override type VertexId = String
  override type Graph = LocalGraph

  // population types
  override type Path = List[SORoutingPathSegment]
  override type PopulationConfig = LocalPopulationConfig
  override type Request = LocalRequest
  override type Response = LocalResponse

  // helpers
  case class LocalPopulationConfig(n: Int, meanDepartureTime: LocalTime, departureTimeRange: Option[LocalTime] = None, randomSeed: Option[Int] = None)
  val HHmmssFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")


  def generateXMLRequests(graph: Graph, requests: GenSeq[LocalRequest]): xml.Elem =
    <population>{requests.map(req => generateXML(graph, req))}</population>

  def generateXMLResponses(graph: Graph, responses: GenSeq[LocalResponse]): xml.Elem =
    <population>{responses.map(req => generateXML(graph, req))}</population>

  /**
    * method to generate a collection of requests based on the graph topology
    *
    * @param graph  underlying graph structure
    * @param config information to constrain the generated data
    * @return a set of requests
    */
  override def generateRequests(graph: Graph, config: PopulationConfig): GenSeq[Request] = {

    val odPairGenerator = nonRepeatingVertexIdGenerator(graph, config.randomSeed)
    val offsetGenerator = timeDepartureOffsetGenerator()

    1 to config.n map (n => {

      val (src, dst) = odPairGenerator()

      val personId: String = s"$n-$src#$dst"

      val timeDepartureOffset = offsetGenerator(config.departureTimeRange)

      val time: LocalTime = config.meanDepartureTime.plusSeconds(timeDepartureOffset)

      LocalRequest(personId, LocalODPair(personId, src, dst), time)
    })
  }


  /**
    * transforms an xml population into a sequence of LocalRequests. invariant: IDs match the SrcDst Regex (See below)
    * @param population an xml file of DocType http://www.matsim.org/files/dtd/population_v6.dtd
    * @return a Scala Sequence collection of LocalRequest types
    */
  def fromXML(population: xml.Elem): GenSeq[LocalRequest] = {

    def extractVertices(id: String): Option[(String, String)] = {
      val SrcDst: Regex = "\\d+-(\\d+)#(\\d+)".r
      id match {
        case SrcDst(g1, g2) => Some((g1, g2))
        case _ => None
      }
    }

    (population \ "person").flatMap {person =>
      person.attribute("id") match {
        case None => None
        case Some(idAttr) =>
          extractVertices(idAttr.toString) match {
            case None => None
            case Some(vertices) =>
              val (src, dst) = vertices
              (person \ "plan" \ "activity").head.attribute("end_time") match {
                case None => None
                case Some(timeAttr) =>
                  val id = idAttr.toString
                  val requestTime = LocalTime.parse(timeAttr.toString)
                  Some(LocalRequest(id, LocalODPair(id, src, dst), requestTime))
              }
          }
      }
    }
  }



  /**
    * turns a request into its MATSim XML representation
    *
    * @param graph   underlying graph structure
    * @param request request data
    * @return request in xml format
    */
  override def generateXML(graph: Graph, request: Request): xml.Elem = {
    val (srcX: String, srcY: String) = graph.vertexById(request.od.src) match {
      case Some(v) => (v.x.toString, v.y.toString)
      case None => ("0.0", "0.0")
    }
    val (dstX: String, dstY: String) = graph.vertexById(request.od.dst) match {
      case Some(v) => (v.x.toString, v.y.toString)
      case None => ("0.0", "0.0")
    }

    <person id={request.id}>
      <plan selected="yes">
        <activity type="home" x={srcX} y={srcY} end_time={request.requestTime.format(HHmmssFormat)}/>
        <leg mode="car"></leg>
        <activity type="work" x={dstX} y={dstY} end_time={request.requestTime.plusHours(9).toString}/>
      </plan>
    </person>
  }

  /**
    * turns a response into its MATSim XML representation
    * @param graph    underlying graph structure
    * @param response response data
    * @return response in xml format
    */
  override def generateXML(graph: Graph, response: Response): xml.Elem = {
    // duplicating code since xml does not have good support for manipulation in Scala
    val (srcX: String, srcY: String) = graph.vertexById(response.request.od.src) match {
      case Some(v) => (v.x.toString, v.y.toString)
      case None => ("0.0", "0.0")
    }
    val (dstX: String, dstY: String) = graph.vertexById(response.request.od.dst) match {
      case Some(v) => (v.x.toString, v.y.toString)
      case None => ("0.0", "0.0")
    }

    <person id={response.request.id}>
      <plan selected="yes">
        <activity type="home" x={srcX} y={srcY} end_time={response.request.requestTime.format(HHmmssFormat)}/>
          <leg mode="car">
            <route type="links">
              {response.path.map(_.edgeId).mkString(" ")}
            </route>
          </leg>
        <activity type="work" x={dstX} y={dstY} end_time={response.request.requestTime.plusHours(9).toString}/>
      </plan>
    </person>
  }

  def timeDepartureOffsetGenerator(): (Option[LocalTime]) => Long = {

    val random = new Random

    (departureTimeOffset: Option[LocalTime]) => departureTimeOffset match {
      case Some(range) =>
        val posRange: Int = range.toSecondOfDay
        random.nextInt(2 * posRange) - posRange
      case None => 0L
    }
  }

  /**
    * creates a pair of vertex ids where src does not equal dst
    * @param graph underlying graph
    * @param randomSeed an optional random seed value
    * @return
    */
  def nonRepeatingVertexIdGenerator(graph: Graph, randomSeed: Option[Int] = None): () => (String, String) = {

    val vertexIds: GenMap[Int, String] =
      graph.vertices.keys
        .zipWithIndex
        .map(tup => tup._2 -> tup._1)
        .toMap

    val numVertices: Int = vertexIds.size

    val random: Random = randomSeed match {
      case Some(seed) => new Random(seed)
      case None => new Random
    }

    () => {
      val src: String = vertexIds(random.nextInt(numVertices))

      // TODO: possible test for proximity here (comparison on graph reachability minimum)
      @tailrec def _findDestination(): String = {
        val dst: String = vertexIds(random.nextInt(numVertices))
        if (dst != src)
          dst
        else
          _findDestination()
      }

      (src, _findDestination())
    }
  }
}
