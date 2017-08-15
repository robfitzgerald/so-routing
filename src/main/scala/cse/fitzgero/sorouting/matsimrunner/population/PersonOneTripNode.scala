package cse.fitzgero.sorouting.matsimrunner.population

import java.time.LocalTime

import cse.fitzgero.sorouting.algorithm.pathsearch.mssp.graphx.simplemssp._
import cse.fitzgero.sorouting.algorithm.pathsearch.od.localgraph.LocalGraphODPair
import cse.fitzgero.sorouting.roadnetwork.graphx.edge.EdgeIdType
import org.apache.spark.graphx.VertexId

import scala.xml.Elem

case class PersonOneTripNode (id: String, mode: String, act1: MiddayActivity, act2: MiddayActivity, leg: LegNode) extends MATSimPerson[MiddayActivity, LegNode]{

  val activityTime: LocalTime = act1.opts match {
    case EndTime(time) => time
    case _ => LocalTime.MIDNIGHT
  }

  def updatePath(path: List[EdgeIdType]): PersonOneTripNode = {
    this.copy(leg = leg.copy(path = path))
  }

  def activityInTimeGroup(low: LocalTime, high: LocalTime): Boolean = {
    (activityTime == low || activityTime.isAfter(low)) && activityTime.isBefore(high)
  }



  def toSimpleMSSP_ODPair: SimpleMSSP_ODPair =
    SimpleMSSP_ODPair(
      id,
      leg.srcVertex,
      leg.dstVertex
    )

  def toLocalGraphODPair: LocalGraphODPair =
    LocalGraphODPair(
      id,
      leg.srcVertex,
      leg.dstVertex
    )

  override def toXml: Elem =
    <person id={id}>
      <plan>
        {act1.toXml}
        {leg.toXml}
        {act2.toXml}
      </plan>
    </person>
}

object PersonOneTripNode {
  def generateLeg(mode: String, act1: MATSimActivity, act2: MATSimActivity)
    = LegNode(mode, act1.vertex, act2.vertex, act1.link, act2.link)
}