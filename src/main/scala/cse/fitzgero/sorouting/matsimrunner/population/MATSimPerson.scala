package cse.fitzgero.sorouting.matsimrunner.population

abstract class MATSimPerson[A <: MATSimActivity, L <: MATSimLeg] extends ConvertsToXml {
  def id: String
  def mode: String
  def act1: A
  def act2: A
  def leg: L

  def updatePath(path: List[String]): PersonOneTripNode

}
