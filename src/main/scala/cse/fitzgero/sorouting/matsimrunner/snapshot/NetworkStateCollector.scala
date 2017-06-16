package cse.fitzgero.sorouting.matsimrunner.snapshot

import java.io.{File, PrintWriter}

import org.matsim.api.core.v01.Id
import org.matsim.api.core.v01.network.Link
import org.matsim.vehicles.Vehicle

import scala.collection.immutable.Map
import scala.util.Try
import scala.xml.{Elem, PrettyPrinter}

case class WriterData(path: String, iteration: Int, timeGroup: String)

/**
  * models the state of the network at the current time through this iteration, designed for easy exporting of a network flow snapshot
  * @param networkState a map of link ids which each point to a set of vehicle ids. private constructor parameter. use the object NetworkStateCollector() as a factory for new instantiations
  */
class NetworkStateCollector private ( val networkState: Map[Id[Link], LinkData[Id[Vehicle]]] = Map.empty[Id[Link], LinkData[Id[Vehicle]]], val name: String = "untitled") {

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
    case other => throw new IllegalArgumentException(s"passed a ${other.getClass}, but update() only handles LinkEnterData and LinkLeaveData")
  }


  override def toString: String = {
    networkState.map(t => {
      s"${t._1} ${t._2.flow}"
    }).mkString("\n")
  }

  def toXML: Elem =
  <network name={name}>
    <links>{networkState.map(t => {
      <link id={t._1.toString} flow={t._2.flow.toString}></link>
    })}</links>
  </network>

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

  def toRawFile(dest: WriterData, network: NetworkStateCollector): Try[String] = {
    Try({
      val filePath: String = s"${dest.path}/${dest.iteration.toString}/snapshot-${dest.timeGroup}.nscData"
      val file = new File(filePath)
      file.getParentFile.mkdirs
      val writer: PrintWriter = new PrintWriter(file)
      writer.write(network.toString)
      writer.close()
      filePath
    })
  }

  def toXMLFile(dest: WriterData, network: NetworkStateCollector): Try[String] = {
    Try({
      val filePath: String = s"${dest.path}/${dest.iteration.toString}/snapshot-${dest.timeGroup}.xml"
      val file = new File(filePath)
      file.getParentFile.mkdirs
      val pretty: String = new PrettyPrinter(80, 2).format(network.toXML)
      val writer: PrintWriter = new PrintWriter(file)
      writer.write(pretty)
      writer.close()
      filePath
    })
  }
}