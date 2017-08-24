package cse.fitzgero.sorouting.matsimrunner.snapshot

import java.io.{File, PrintWriter}

import cse.fitzgero.sorouting.matsimrunner.snapshot.linkdata._
import cse.fitzgero.sorouting.roadnetwork.costfunction.CostFunction
import org.matsim.api.core.v01.Id
import org.matsim.api.core.v01.network.Link

import scala.collection.immutable.Map
import scala.util.Try
import scala.xml.{Elem, PrettyPrinter}

/**
  * models the state of the network at the current time through this iteration, designed for easy exporting of a network flow snapshot
  * @param networkState a map of link ids which each point to a set of vehicle ids. private constructor parameter. use the object NetworkAnalyticStateCollector() as a factory for new instantiations
  */
class NetworkAnalyticStateCollector private
( val cost: CostFunction,
  val networkState: Map[Id[Link], AnalyticLink] = Map.empty[Id[Link], AnalyticLink],
  val name: String = "untitled"
  ) {

  def update(e: SnapshotEventData): NetworkAnalyticStateCollector = e match {
    case LinkEnterData(t, link, veh) =>
      val thisLink: AnalyticLink = networkState.getOrElse(link, AnalyticLink(cost))
      new NetworkAnalyticStateCollector(cost, networkState.updated(link, thisLink.add(AnalyticLinkDataUpdate(veh, t))), name)
    case LinkLeaveData(t, link, veh) =>
      val thisLink: AnalyticLink = networkState.getOrElse(link, AnalyticLink(cost))
      new NetworkAnalyticStateCollector(cost, networkState.updated(link, thisLink.remove(AnalyticLinkDataUpdate(veh, t))), name)
    case other => throw new IllegalArgumentException(s"passed a ${other.getClass}, but update() only handles LinkEnterData and LinkLeaveData")
  }

  lazy val networkAverageTravelTime: Double = networkState.values.map(_.mean).sum / networkState.size

  override def toString: String = {
    networkState.map(t => {
      s"${toXml.toString}"
    }).mkString("\n")
  }

  def toXml: Elem =
  <network name={name}>
    <global avgtraveltime={networkAverageTravelTime}></global>
    <links>
      {networkState.map(link => {
        <link id={link._1.toString}>
          {link._2.travelTimeXml}
          {link._2.congestionXml}
        </link>
      })}
    </links>
  </network>

  def getLink(link: Id[Link]): AnalyticLink = networkState.getOrElse(link, AnalyticLink(cost))
}

object NetworkAnalyticStateCollector {
  def apply(links: Iterable[Id[Link]], cost: CostFunction): NetworkAnalyticStateCollector = {
    new NetworkAnalyticStateCollector(
      cost,
      links.aggregate(Map.empty[Id[Link], AnalyticLink])(
        (acc, linkId) => acc.updated(linkId, AnalyticLink(cost)),
        (a, b) => a ++ b
      )
    )
  }

  def toRawFile(dest: WriterData, network: NetworkAnalyticStateCollector): Try[String] = {
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

  def toXMLFile(dest: WriterData, network: NetworkAnalyticStateCollector): Try[String] = {
    Try({
      val filePath: String = s"${dest.path}/${dest.iteration.toString}/snapshot-${dest.timeGroup}.xml"
      val file = new File(filePath)
      file.getParentFile.mkdirs
      val pretty: String = new PrettyPrinter(80, 2).format(network.toXml)
      val writer: PrintWriter = new PrintWriter(file)
      writer.write(pretty)
      writer.close()
      filePath
    })
  }

  /**
    * saves as an xml file to a directory and with the default name of "snapshot.xml"
    * @param path path to directory for writing xml file
    * @param network a NetworkAnalyticStateCollector we wish to save
    * @param fileName optionally give this a unique file name
    * @return
    */
  def toXMLFile(path: String, network: NetworkAnalyticStateCollector, fileName: String = "snapshot.xml"): Try[String] = {
    Try({
      val filePath: String = s"$path/$fileName"
      val file = new File(filePath)
      file.getParentFile.mkdirs
      val pretty: String = new PrettyPrinter(80, 2).format(network.toXml)
      val writer: PrintWriter = new PrintWriter(file)
      writer.write(pretty)
      writer.close()
      filePath
    })
  }
}