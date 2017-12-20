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
import scala.xml.{NodeSeq, XML}

trait LocalPopulationOps extends BasicPopulationOps {
  // graph types
  override type EdgeId = String
  override type VertexId = String
  override type Graph = LocalGraph

  // population types
  override type Path = List[SORoutingPathSegment]
  override type PopulationConfig = LocalPopulationConfig
  override type Request = LocalRequest
  override type Response = LocalResponse

  /**
    * the configuration used to generate a population
    * @param n the size of the population
    * @param meanDepartureTime mean value for random distribution of driver departure times
    * @param departureTimeRange the width of the distribution of driver departure times
    * @param randomSeed seed value for the random number generator
    */
  case class LocalPopulationConfig(n: Int, meanDepartureTime: LocalTime, departureTimeRange: Option[LocalTime] = None, randomSeed: Option[Int] = None)
  val HHmmssFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")


  /**
    * generate XML of this population. since it is requests, they will not have proper start/end links, just vertex positions, which are not used for routing by MATSim
    * @param graph the underlying graph structure
    * @param requests the LocalRequests to export to XML
    * @return
    */
  def generateXMLRequests(graph: Graph, requests: GenSeq[LocalRequest]): xml.Elem =
    <population>{requests.map(req => generateXML(graph, req))}</population>

  /**
    * generate XML of this population. since it is responses, they will be written with link attributes for routing instructions.
    * @param graph the underlying graph structure
    * @param responses the LocalRequests to export to XML
    * @return
    */
  def generateXMLResponses(graph: Graph, responses: GenSeq[LocalResponse]): xml.Elem =
    <population>{responses.map(res => generateXML(graph, res))}</population>



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
    val (src: String, dst: String) =
      if (response.path.isEmpty) {
        println("[LocalPopulationOps:generateXML] a response with an empty path!")
        ("","")
      }
      else if (response.path.size == 1) {
        (response.path.head.edgeId, response.path.head.edgeId)
      } else {
        (response.path.head.edgeId, response.path.last.edgeId)
      }

    <person id={response.request.id}>
      <plan selected="yes">
        <activity type="home" link={src} end_time={response.request.requestTime.format(HHmmssFormat)}/>
        <leg mode="car">
          <route type="links" start_link={src} end_link={dst}>
            {response.path.map(_.edgeId).mkString(" ")}
          </route>
        </leg>
        <activity type="work" link={dst} end_time={response.request.requestTime.plusHours(9).toString}/>
      </plan>
    </person>

  }

  /**
    * returns a function which can be used to create random time offsets in a normal distribution
    * @return
    */
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
