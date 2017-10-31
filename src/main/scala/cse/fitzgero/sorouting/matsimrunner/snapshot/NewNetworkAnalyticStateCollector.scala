package cse.fitzgero.sorouting.matsimrunner.snapshot

import java.io.{File, PrintWriter}

import cse.fitzgero.sorouting.matsimrunner.network.Network
import cse.fitzgero.sorouting.matsimrunner.snapshot.linkdata.{AnalyticLinkDataUpdate, NewAnalyticLinkData}
import cse.fitzgero.sorouting.model.roadnetwork.costfunction._
import cse.fitzgero.sorouting.model.roadnetwork.local.LocalEdgeAttribute

import scala.util.Try
import scala.xml.PrettyPrinter

object NewNetworkAnalyticStateCollector {
  val MATSimFlowRate: Double = 3600D
  type SnapshotCollector = Map[String, NewAnalyticLinkData]
  def update(coll: SnapshotCollector, e: SnapshotEventData): SnapshotCollector = {
    e match {
      case LinkEnterData(t, link, veh) =>
        if (coll.isDefinedAt(link.toString)) {
          val thisLink = coll(link.toString)
          coll.updated(link.toString, thisLink.add(AnalyticLinkDataUpdate(veh.toString, t)))
        } else coll
      case LinkLeaveData(t, link, veh) =>
        if (coll.isDefinedAt(link.toString)) {
          val thisLink = coll(link.toString)
          coll.updated(link.toString, thisLink.remove(AnalyticLinkDataUpdate(veh.toString, t)))
        } else coll
    case _ => coll
    }

  }
  def networkAverageTravelTime(coll: SnapshotCollector): Double = coll.values.flatMap(_.mean).sum / coll.size
  def toString(coll: SnapshotCollector): String = {
    coll.map(t => {
      s"${toXml(coll).toString}"
    }).mkString("\n")
  }
  def toXml(coll: SnapshotCollector): xml.Elem =
    <network>
      <global avgtraveltime={networkAverageTravelTime(coll).toString}></global>
      <links>
        {
          coll.map(link => {
            <link id={link._1.toString}>
              {link._2.travelTimeXml}
              {link._2.congestionXml}
            </link>
          }
        )}
      </links>
    </network>

  /**
    * saves as an xml file to a directory and with the default name of "snapshot.xml"
    * @param path path to directory for writing xml file
    * @param network a NetworkAnalyticStateCollector we wish to save
    * @param fileName optionally give this a unique file name
    * @return
    */
  def toXMLFile(path: String, network: SnapshotCollector, fileName: String = "snapshot.xml"): Try[String] = {
    Try({
      val filePath: String = s"$path/$fileName"
      val file = new File(filePath)
      file.getParentFile.mkdirs
      val pretty: String = new PrettyPrinter(80, 2).format(toXml(network))
      val writer: PrintWriter = new PrintWriter(file)
      writer.write(pretty)
      writer.close()
      filePath
    })
  }

  def apply(network: Network, costFunctionType: CostFunctionType, algorithmFlowRate: Int): SnapshotCollector = {
    network.links.aggregate(Map.empty[String, NewAnalyticLinkData])(
      (acc, link) => {
        val relativeCapacity: Double = link._2.capacity * (algorithmFlowRate / MATSimFlowRate)
        val edge = costFunctionType match {
          case BasicCostFunctionType => new LocalEdgeAttribute(
            None,
            Some(relativeCapacity),
            Some(link._2.freespeed),
            Some(link._2.length)
          ) with BasicCostFunction
          case BPRCostFunctionType => new LocalEdgeAttribute(
            None,
            Some(relativeCapacity),
            Some(link._2.freespeed),
            Some(link._2.length)
          ) with BPRCostFunction
        }
        acc.updated(link._1, NewAnalyticLinkData(edge = edge))
      },
      (a, b) => a ++ b
    )
  }
}
