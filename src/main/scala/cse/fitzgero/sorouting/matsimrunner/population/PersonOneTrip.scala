package cse.fitzgero.sorouting.matsimrunner.population

import java.time.LocalTime

import cse.fitzgero.sorouting.algorithm.pathsearch.mssp.graphx.simplemssp._
import cse.fitzgero.sorouting.algorithm.pathsearch.od.localgraph.LocalGraphODPair
import cse.fitzgero.sorouting.roadnetwork.graphx.edge.EdgeIdType

import scala.xml.Elem

/**
  * this represents a person, or more specifically, an instance of a person with a single $leg between two activities
  * @param id a single identifier for a person, or, one that combines a person's id with an instance number, for when we break a person up into multiple, separate trip representations
  * @param mode mode(s) of transportation, separated by a comma
  * @param act1 starting activity. end time of this activity is used for triggering a trip.
  * @param act2 destination activity
  */
case class PersonOneTrip (id: PersonID, mode: String, act1: MATSimActivity, act2: MATSimActivity, leg: MATSimLeg) extends MATSimPerson[MATSimActivity, MATSimLeg] {

  /**
    * sets the trip leg's route
    * @param path a list of edges to traverse
    * @return a new PersonOneTrip instance with the described route
    */
  def updatePath(path: List[EdgeIdType]): PersonOneTrip = {
    this.copy(leg = RoutedLeg(leg.mode, leg.srcVertex, leg.dstVertex, leg.srcLink, leg.dstLink, path))
  }
}

case object PersonOneTrip {
  def apply(id: PersonID, mode: String, act1: MATSimActivity, act2: MATSimActivity): PersonOneTrip = {
    val leg = UnroutedLeg(mode, act1.vertex, act2.vertex, act1.link, act2.link)
    PersonOneTrip(id, mode, act1, act2, leg)
  }
}