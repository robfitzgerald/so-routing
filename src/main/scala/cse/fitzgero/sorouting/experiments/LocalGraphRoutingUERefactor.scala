package cse.fitzgero.sorouting.experiments

import java.time.LocalTime
import java.time.format.DateTimeFormatter

import cse.fitzgero.sorouting.algorithm.local.mssp.MSSPLocalDijkstrasService
import cse.fitzgero.sorouting.algorithm.pathsearch.sssp.localgraphsimplesssp.LocalGraphMATSimSSSP
import cse.fitzgero.sorouting.app.SORoutingApplicationConfig
import cse.fitzgero.sorouting.matsimrunner.{ArgsNotMissingValues, MATSimRunnerConfig, MATSimSingleSnapshotRunnerModule}
import cse.fitzgero.sorouting.model.population.{LocalPopulationOps, LocalRequest, LocalResponse}
import cse.fitzgero.sorouting.model.roadnetwork.costfunction.BPRCostFunctionType
import cse.fitzgero.sorouting.model.roadnetwork.local.{LocalGraph, LocalGraphOps}
import cse.fitzgero.sorouting.util._

import scala.collection.GenSeq
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.xml.XML

//case class LocalGraphRoutingResultRunTimes(ksp: List[Long] = List(), fw: List[Long] = List(), selection: List[Long] = List(), overall: List[Long] = List())

///**
//  * return tuple containing results from the completion of the UE routing algorithm
//  * @param population the complete population, with selfish routes applied
//  */
//case class LocalGraphUERoutingRefactorResult(population: GenSeq[LocalResponse])

object LocalGraphRoutingUERefactor extends ClassLogging {

  val StartOfDay = 0
  val RoutingAlgorithmTimeout: Duration = 600 seconds
  val HHmmssFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
  case class TimeGroup (startRange: Int, endRange: Int)

  val sssp = LocalGraphMATSimSSSP()

  /**
    * solves the routing over a provided network with a provided population
    * @param conf
    * @param fileHelper
    * @param population
    * @return
    */
  def routeAllRequestedTimeGroups(conf: SORoutingApplicationConfig, fileHelper: SORoutingFilesHelper, population: GenSeq[LocalRequest]): GenSeq[LocalResponse] = {

    val timeGroups: Iterator[TimeGroup] =
      (StartOfDay +: (LocalTime.parse(conf.startTime).toSecondOfDay until LocalTime.parse(conf.endTime).toSecondOfDay by conf.timeWindow))
        .sliding(2)
        .map(vec => TimeGroup(vec(0), vec(1)))

    // run UE algorithm for each time window, updating the population data with their routes while stepping through time windows
    timeGroups.foldLeft(GenSeq.empty[LocalResponse])((acc, timeGroupSecs) => {
      val (timeGroupStart, timeGroupEnd) =
        (LocalTime.ofSecondOfDay(timeGroupSecs.startRange),
          LocalTime.ofSecondOfDay(timeGroupSecs.endRange))

      val groupToRoute: GenSeq[LocalRequest] = population.filter(req => {
        timeGroupStart.compareTo(req.requestTime) <= 0 &&
        req.requestTime.compareTo(timeGroupEnd) < 0
      })

      println("~~~ time group ~~~")
      println(s"start: ${timeGroupStart.toString}, end: ${timeGroupEnd.toString}")
      println("~~~ group to route ~~~")
      groupToRoute.foreach(println)


      if (groupToRoute.isEmpty) acc
      else {
        val snapshotPopulation: GenSeq[LocalResponse] = acc
        val ueGraph: LocalGraph = LocalGraphOps.readMATSimXML(fileHelper.getNetwork, None, BPRCostFunctionType, conf.timeWindow)
        val uePopulationXML: xml.Elem = LocalPopulationOps.generateXMLResponses(ueGraph, snapshotPopulation)
        val snapshotDirectory: String = fileHelper.scaffoldSnapshotRef(uePopulationXML, timeGroupStart, timeGroupEnd)

        // ----------------------------------------------------------------------------------------
        // 1. run MATSim snapshot for the populations associated with all previous time groups
        val matsimSnapshotRun = MATSimSingleSnapshotRunnerModule(MATSimRunnerConfig(
          s"$snapshotDirectory/config-snapshot.xml",
          s"$snapshotDirectory/matsim-output",
          conf.timeWindow,
          conf.startTime,
          timeGroupStart.format(HHmmssFormat),
          ArgsNotMissingValues
        ))

        // ----------------------------------------------------------------------------------------
        // 2. run routing algorithm for the UE routed population for current time group, using snapshot (mssp)

        val networkFilePath: String = s"$snapshotDirectory/network-snapshot.xml"
        val snapshotFilePath: String = matsimSnapshotRun.filePath
        val snapshotXML: xml.Elem = XML.loadFile(snapshotFilePath)
        val snapshotGraph: LocalGraph = LocalGraphOps.readMATSimXML(fileHelper.getNetwork, Some(snapshotXML), BPRCostFunctionType, conf.timeWindow)

        fileHelper.removeSnapshotFiles(timeGroupStart)

        val msspFuture = MSSPLocalDijkstrasService.runService(snapshotGraph, groupToRoute)

        Await.result(msspFuture, 10 seconds) match {
          case Some(serviceResult) =>
            acc ++ serviceResult.result
          case None => acc
        }
      }
    })
  }
}
