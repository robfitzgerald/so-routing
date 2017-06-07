package cse.fitzgero.sorouting.matsimrunner

import org.matsim.api.core.v01.Id
import org.matsim.vehicles.Vehicle

abstract class LinkData [T] {
  def add(veh: T): LinkData[T]
  def remove(veh: T): LinkData[T]
  def flow: Int
}

case class NonEmptyLink(vehicles: Set[Id[Vehicle]]) extends LinkData[Id[Vehicle]] {
  def add(veh: Id[Vehicle]): NonEmptyLink = NonEmptyLink(vehicles + veh)
  def remove(veh: Id[Vehicle]): LinkData[Id[Vehicle]] = {
    val result: Set[Id[Vehicle]] = vehicles - veh
    if (result.isEmpty) EmptyLink else NonEmptyLink(result)
  }
  def flow: Int = vehicles.size
}
case object EmptyLink extends LinkData[Id[Vehicle]] {
  def add(veh: Id[Vehicle]): NonEmptyLink = NonEmptyLink(Set(veh))
  def remove(veh: Id[Vehicle]): Nothing =
    throw new java.lang.ArrayIndexOutOfBoundsException(s"attempted to remove vehicle $veh from an EmptyLink")
  def flow: Int = 0
}