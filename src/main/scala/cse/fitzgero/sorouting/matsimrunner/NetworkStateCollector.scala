package cse.fitzgero.sorouting.matsimrunner

import scala.collection.immutable.Map

import org.matsim.api.core.v01.Id
import org.matsim.api.core.v01.network.Link
import org.matsim.vehicles.Vehicle

class NetworkStateCollector {
  private var networkState: Map[Id[Link], LinkData[Id[Vehicle]]] = Map.empty[Id[Link], LinkData[Id[Vehicle]]]

  def link(link: Id[Link])(): NetworkStateCollector = {
    val thisLink: LinkData[Id[Vehicle]] = networkState.getOrElse(link, EmptyLink)

    this
  }

}

object NetworkStateCollector {
  def apply(): NetworkStateCollector = new NetworkStateCollector()
}