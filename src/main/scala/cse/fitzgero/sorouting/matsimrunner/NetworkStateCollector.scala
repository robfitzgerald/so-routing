package cse.fitzgero.sorouting.matsimrunner

import scala.collection.mutable.Map

import org.matsim.api.core.v01.Id
import org.matsim.api.core.v01.network.Link
import org.matsim.vehicles.Vehicle

class NetworkStateCollector (val networkState: Map[Id[Link], LinkData[Id[Vehicle]]] = Map.empty[Id[Link], LinkData[Id[Vehicle]]]) {
  def update(e: SnapshotEvent): NetworkStateCollector = e match {
    case LinkEnterData(link, veh) => {
      val thisLink: LinkData[Id[Vehicle]] = networkState.getOrElse(link, EmptyLink)
      new NetworkStateCollector(networkState.updated(link, thisLink.add(veh)))
    }
    case LinkLeaveData(link, veh) => {
      val thisLink: LinkData[Id[Vehicle]] = networkState.getOrElse(link, EmptyLink)
      new NetworkStateCollector(networkState.updated(link, thisLink.remove(veh)))
    }
  }
  override def toString: String = {
    networkState.map(t => {
      t._1 + " " + t._2.flow
    }).mkString("\n")
  }
}

object NetworkStateCollector {
  def apply(): NetworkStateCollector = new NetworkStateCollector(Map.empty[Id[Link], LinkData[Id[Vehicle]]])
}