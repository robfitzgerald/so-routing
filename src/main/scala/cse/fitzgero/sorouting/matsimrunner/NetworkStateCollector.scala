package cse.fitzgero.sorouting.matsimrunner

import java.io.{File, PrintWriter}
import scala.collection.JavaConverters._
import scala.util.Try

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

  def update(e: SnapshotEventData): NetworkStateCollector = e match {
    case LinkEnterData(t, link, veh) =>
      val thisLink: LinkData[Id[Vehicle]] = networkState.getOrElse(link, EmptyLink)
      new NetworkStateCollector(networkState.updated(link, thisLink.add(veh)))
    case LinkLeaveData(t, link, veh) =>
      val thisLink: LinkData[Id[Vehicle]] = networkState.getOrElse(link, EmptyLink)
      new NetworkStateCollector(networkState.updated(link, thisLink.remove(veh)))
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
  def apply(links: scala.collection.mutable.Map[Id[Link], _]): NetworkStateCollector = {
    new NetworkStateCollector(
      links.keys.foldLeft(Map.empty[Id[Link], LinkData[Id[Vehicle]]])((network, linkId) => {
        network.updated(linkId, EmptyLink)
      })
    )
  }

  def toFile(path: String, iteration: Int, timeGroup: String, network: NetworkStateCollector): Try[Unit] = {
    Try({
      val file = new File(s"$path/${iteration.toString}/snapshot-$timeGroup.nscData")
      file.getParentFile.mkdirs
      val writer: PrintWriter = new PrintWriter(file)
      writer.write(network.toString)
      writer.close()
    })
  }
}