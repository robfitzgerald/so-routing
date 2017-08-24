package cse.fitzgero.sorouting.matsimrunner.snapshot.linkdata

import org.matsim.api.core.v01.Id
import org.matsim.vehicles.Vehicle

abstract class LinkData [T] {
  def add(veh: T): LinkData[T]
  def remove(veh: T): LinkData[T]
  def flow: Int
}

