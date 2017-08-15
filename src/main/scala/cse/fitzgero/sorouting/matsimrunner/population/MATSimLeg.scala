package cse.fitzgero.sorouting.matsimrunner.population

abstract class MATSimLeg {
  def mode: String
  def srcVertex: Long
  def dstVertex: Long
  def srcLink: String
  def dstLink: String
  def path: List[String]
}
