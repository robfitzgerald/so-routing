package cse.fitzgero.sorouting.matsimrunner.population

import java.time.LocalTime

import cse.fitzgero.sorouting.algorithm.pathsearch.mssp.graphx.simplemssp.SimpleMSSP_ODPair
import cse.fitzgero.sorouting.algorithm.pathsearch.od.localgraph.LocalGraphODPair

import scala.xml.Elem

abstract class MATSimPerson[A <: MATSimActivity, L <: MATSimLeg] extends ConvertsToXml {
  def id: PersonID
  def mode: String
  def act1: A
  def act2: A
  def leg: L

  def updatePath(path: List[String]): MATSimPerson[A, _]

  /**
    * calculates the time for trip departure
    */
  private val activityTime: LocalTime = act1.opts match {
    case EndTime(time) => time
    case _ => LocalTime.MIDNIGHT
  }

  /**
    * reports whether the end of the first activity (and subsequent travel segment) falls within a time bounds
    * @param low lower time bounds, inclusive
    * @param high upper time bounds, exclusive
    * @return boolean if within bounds
    */
  def activityInTimeGroup(low: LocalTime, high: LocalTime): Boolean = {
    (activityTime == low || activityTime.isAfter(low)) && activityTime.isBefore(high)
  }

  /**
    * export this trip leg as a route request for the multiple shortest paths search algorithm
    * @return
    */
  def toSimpleMSSP_ODPair: SimpleMSSP_ODPair =
    SimpleMSSP_ODPair(
      id.toString,
      leg.srcVertex,
      leg.dstVertex
    )

  /**
    * export this trip leg as a route request for the LocalGraph family of search algorithms
    * @return
    */
  def toLocalGraphODPair: LocalGraphODPair =
    LocalGraphODPair(
      id.toString,
      leg.srcVertex,
      leg.dstVertex
    )


  /**
    * export this person as an xml person object, in the format described by "http://www.matsim.org/files/dtd/population_v6.dtd"
    * @return
    */
  override def toXml: Elem =
    <person id={id.toString}>
      <plan selected="yes">
        {act1.toXml}
        {leg.toXml}
        {act2.toXml}
      </plan>
    </person>

}
