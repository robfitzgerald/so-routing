package cse.fitzgero.sorouting.matsimrunner.snapshot

import java.io.{File, PrintWriter}

import cse.fitzgero.sorouting.matsimrunner.snapshot.linkdata.{SimpleLinkData, _}
import org.matsim.api.core.v01.Id
import org.matsim.api.core.v01.network.Link
import org.matsim.vehicles.Vehicle

import scala.collection.immutable.Map
import scala.util.Try
import scala.xml.{Elem, PrettyPrinter}

/**
  * models the state of the network at the current time through this iteration, designed for easy exporting of a network flow snapshot
  * @param networkState a map of link ids which each point to a set of vehicle ids. private constructor parameter. use the object NetworkStateCollector() as a factory for new instantiations
  */
class NetworkStateCollector private ( val networkState: Map[Id[Link], SimpleLinkData] = Map.empty[Id[Link], SimpleLinkData], val name: String = "untitled") {

  def addDriver(link: Id[Link], vehicle: Id[Vehicle]): NetworkStateCollector = {
    val thisLink: SimpleLinkData = networkState.getOrElse(link, EmptyLink)
    new NetworkStateCollector(networkState.updated(link, thisLink.add(vehicle)))
  }

  def removeDriver(link: Id[Link], vehicle: Id[Vehicle]): NetworkStateCollector = {
    val thisLink: SimpleLinkData = networkState.getOrElse(link, EmptyLink)
    new NetworkStateCollector(networkState.updated(link, thisLink.remove(vehicle)))
  }

  def update(e: SnapshotEventData): NetworkStateCollector = e match {
    case LinkEnterData(t, link, veh) =>
      networkState.get(link) match {
        case None =>
          println(s"network state collector is missing link $link")
          this
        case Some(thisLink) =>
          new NetworkStateCollector(networkState.updated(link, thisLink.add(veh)))
      }

    case LinkLeaveData(t, link, veh) =>
      networkState.get(link) match {
        case None =>
          println(s"network state collector is missing link $link")
          this
        case Some(EmptyLink) =>
          println(s"network state collector attempting to remove driver $veh from empty link $link")
          this
        case Some(thisLink: SimpleLinkData) =>
          new NetworkStateCollector(networkState.updated(link, thisLink.remove(veh)))
      }
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

  def getLink(link: Id[Link]): SimpleLinkData = networkState.getOrElse(link, EmptyLink)
}

object NetworkStateCollector {
  def apply(): NetworkStateCollector = new NetworkStateCollector(Map.empty[Id[Link], SimpleLinkData])
  def apply(links: scala.collection.mutable.Map[Id[Link], _]): NetworkStateCollector = {
    new NetworkStateCollector(
      links.keys.foldLeft(Map.empty[Id[Link], SimpleLinkData])((network, linkId) => {
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

  /**
    * saves as an xml file to a directory and with the default name of "snapshot.xml"
    * @param path path to directory for writing xml file
    * @param network a NetworkStateCollector we wish to save
    * @param fileName optionally give this a unique file name
    * @return
    */
  def toXMLFile(path: String, network: NetworkStateCollector, fileName: String = "snapshot.xml"): Try[String] = {
    Try({
      val filePath: String = s"$path/$fileName"
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