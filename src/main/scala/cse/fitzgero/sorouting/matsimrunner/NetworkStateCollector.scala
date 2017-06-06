package cse.fitzgero.sorouting.matsimrunner

import scala.collection.immutable.Map

import org.matsim.api.core.v01.Id
import org.matsim.api.core.v01.network.Link
import org.matsim.vehicles.Vehicle

/**
  * models the state of the network at the current time through this iteration, designed for easy exporting of a network flow snapshot
  * @param networkState a map of link ids which each point to a set of vehicle ids. private constructor parameter. use the object NetworkStateCollector() as a factory for new instantiations
  */
class NetworkStateCollector private ( val networkState: Map[Id[Link], LinkData[Id[Vehicle]]] = Map.empty[Id[Link], LinkData[Id[Vehicle]]]) {

  def addDriver(link: Id[Link], vehicle: Id[Vehicle]): NetworkStateCollector = {
    val thisLink: LinkData[Id[Vehicle]] = networkState.getOrElse(link, EmptyLink)
    new NetworkStateCollector(networkState.updated(link, thisLink.add(vehicle)))
  }

  def removeDriver(link: Id[Link], vehicle: Id[Vehicle]): NetworkStateCollector = {
    val thisLink: LinkData[Id[Vehicle]] = networkState.getOrElse(link, EmptyLink)
    new NetworkStateCollector(networkState.updated(link, thisLink.remove(vehicle)))
  }

  override def toString: String = {
    networkState.map(t => {
      t._1 + " " + t._2.flow
    }).mkString("\n")
  }

  def getLink(link: Id[Link]): LinkData[Id[Vehicle]] = networkState.getOrElse(link, EmptyLink)
}

object NetworkStateCollector {
  def apply(): NetworkStateCollector = new NetworkStateCollector(Map.empty[Id[Link], LinkData[Id[Vehicle]]])
}