package cse.fitzgero.sorouting.matsimrunner.snapshot.linkdata

abstract class LinkData [T] {
  def add(veh: T): LinkData[T]
  def remove(veh: T): LinkData[T]
  def flow: Int
}

