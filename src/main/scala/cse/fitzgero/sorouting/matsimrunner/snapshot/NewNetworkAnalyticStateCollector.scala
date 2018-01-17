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



  /**
    * directs calls to update link values in the snapshot representation of the current network state
    * @param coll the current snapshot
    * @param e the event handler package passed from MATSim
    * @return an updated snapshot
    */
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

  /**
    * no premature averaging here.. ??
    * @param coll the current snapshot
    * @return the average average link travel time cost
    */
  def networkAverageTravelTime(coll: SnapshotCollector): Double = {
//     this is just the "average", but we likely don't want that, since the observations
//     for each link are not symmetric; they will occur when a new driver appears on that link
//    TODO: thoughts?
//    val allTravelTimes: Iterable[BigDecimal] = coll.values.flatMap(_.travelTime.map(time => BigDecimal(time)))
//    (allTravelTimes.sum / allTravelTimes.size).toDouble

    // this version is the average of averages. still monotonic in observations..?
    coll.values.flatMap(_.mean).sum / coll.size
  }



  /**
    * converts an entire snapshot collection into its xml tree representation
    * @param coll the current snapshot
    * @return xml representation
    */
  def toXml(coll: SnapshotCollector): xml.Elem =
    <network>
      <global avgtraveltime={networkAverageTravelTime(coll).toString}></global>
      <links>
        {
          coll.map(link => {
            <link id={link._1.toString} flow={link._2.flow.toString}>
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



  /**
    * factory for building network snapshots
    * @param network a collection of case classes representing the MATSim network.xml document type
    * @param costFunctionType enum for selecting a cost function
    * @param algorithmFlowRate the window duration of our algorithm, for scaling the capacity from vehicles/hour to vehicles/time window
    * @return
    */
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
