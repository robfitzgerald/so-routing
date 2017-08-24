package cse.fitzgero.sorouting.matsimrunner.snapshot.linkdata

import org.matsim.api.core.v01.Id
import org.matsim.vehicles.Vehicle

trait SimpleLinkData extends LinkData[Id[Vehicle]] {
  override def add(veh: Id[Vehicle]): SimpleLinkData
  override def remove(veh: Id[Vehicle]): SimpleLinkData
}

case class NonEmptyLink(vehicles: Set[Id[Vehicle]]) extends SimpleLinkData {
  override def add(veh: Id[Vehicle]): NonEmptyLink = NonEmptyLink(vehicles + veh)
  override def remove(veh: Id[Vehicle]): SimpleLinkData = {
    val result: Set[Id[Vehicle]] = vehicles - veh
    if (result.isEmpty) EmptyLink else NonEmptyLink(result)
  }
  def flow: Int = vehicles.size
}

case object EmptyLink extends SimpleLinkData {
  override def add(veh: Id[Vehicle]): NonEmptyLink = NonEmptyLink(Set(veh))
  override def remove(veh: Id[Vehicle]): Nothing =
    throw new java.lang.ArrayIndexOutOfBoundsException(s"attempted to remove vehicle $veh from an EmptyLink")
  def flow: Int = 0
}
