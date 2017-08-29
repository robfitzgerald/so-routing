package cse.fitzgero.sorouting.app

import java.time.LocalTime

import cse.fitzgero.sorouting.algorithm.pathsearch.sssp.localgraphsimplesssp.LocalGraphMATSimSSSP
import cse.fitzgero.sorouting.matsimrunner._
import cse.fitzgero.sorouting.matsimrunner.population._
import cse.fitzgero.sorouting.algorithm.routing.localgraphrouting.{LocalGraphRoutingModule, LocalGraphRoutingModuleResult}
import cse.fitzgero.sorouting.matsimrunner.network.MATSimNetworkToCollection
import cse.fitzgero.sorouting.roadnetwork.costfunction.BPRCostFunction
import cse.fitzgero.sorouting.roadnetwork.localgraph.{LocalGraphMATSim, LocalGraphMATSimFactory}
import cse.fitzgero.sorouting.util._
import cse.fitzgero.sorouting.util.convenience._

import scala.util.{Failure, Success}



object SORoutingLocalGraphInlineApplication extends App {

//  Logger.getRootLogger.setLevel(Level.WARN)
//  val log = Logger.getLogger(this.getClass)

  val conf: SORoutingApplicationConfig1 = SORoutingApplicationConfigParseArgs(args)
  val SomeParallelProcessesSetting = 2

  println(conf)

  val fileHelper = SORoutingFilesHelper(conf)
  val networkData = MATSimNetworkToCollection(fileHelper.thisNetworkFilePath)

//  val commuterPopulationSize: Int = (conf.populationSize * 0.8).toInt
//  val otherPopulationSize: Int = conf.populationSize - commuterPopulationSize

  // Create a population from the road network topology
  // from that population, partition it to drivers that will receive our routing and drivers that will not
  val populationFull: PopulationOneTrip =
    PopulationOneTrip
      .generateRandomOneTripPopulation(
        fileHelper.getNetwork,
        RandomPopulationOneTripConfig(
          conf.populationSize,
          Seq(
            ActivityConfig2(
              "home",
              LocalTime.parse("09:00:00") endTime,
              30 minutesDeviation),
            ActivityConfig2(
              "work",
              LocalTime.parse("17:00:00") endTime,
              30 minutesDeviation),
            ActivityConfig2(
              "home",
              LocalTime.MIDNIGHT endTime,
              30 minutesDeviation)
          ),
          Seq(ModeConfig("car"))
        )
      )

//  val otherPopulation: PopulationOneTrip =
//    PopulationOneTrip
//      .generateRandomOneTripPopulation(
//        fileHelper.getNetwork,
//        RandomPopulationOneTripConfig(
//          commuterPopulationSize,
//          Seq(
//            ActivityConfig2(
//              "home",
//              LocalTime.parse("10:00:00") endTime,
//              30 minutesDeviation),
//            ActivityConfig2(
//              "work",
//              LocalTime.parse("12:00:00") endTime,
//              30 minutesDeviation)),
//          Seq(ModeConfig("car"))
//        )
//      )

//  val populationFull = PopulationOneTrip(commuterPopulation.persons ++ otherPopulation.persons)


  val uePopSize: Int =
  {
    val (populationSO, populationPartial) = populationFull.subsetPartition(conf.routePercentage)

    val populationFullUE = {
      val sssp = LocalGraphMATSimSSSP()
      // assign shortest path search to all UE drivers
      val graphWithNoFlows: LocalGraphMATSim =
        LocalGraphMATSimFactory(BPRCostFunction, AlgorithmFlowRate = conf.algorithmTimeWindow.toDouble)
          .fromFile(fileHelper.thisNetworkFilePath) match {
          case Success(g) => g.par
          case Failure(e) => throw new Error(s"failed to load network file ${fileHelper.thisNetworkFilePath}")
        }
      val populationDijkstrasRoutes =
        if (SomeParallelProcessesSetting == 1)  // TODO again, parallel config here.
          populationPartial.exportAsODPairsByEdge.map(sssp.shortestPath(graphWithNoFlows, _))
        else
          populationPartial.exportAsODPairsByEdge.par.map(sssp.shortestPath(graphWithNoFlows, _))

      populationDijkstrasRoutes.foldLeft(populationPartial)(_.updatePerson(_))
    }

    fileHelper.savePopulation(populationFullUE, FullUEExp, FullUEPopulation)
    populationFullUE.persons.size // number of UE routes (one per activity)
  }


  //----------------------------------------------------------------------------------------------
  //  1. Run 100% UE Simulation, get overall congestion (measure?)
  //----------------------------------------------------------------------------------------------
  MATSimSingleAnalyticSnapshotRunnerModule(
    MATSimRunnerConfig(
      fileHelper.finalConfigFilePath(FullUEExp),
      fileHelper.experimentPath(FullUEExp),
      conf.algorithmTimeWindow,
      conf.startTime,
      conf.endTime,
      ArgsNotMissingValues
    ),
    networkData,
    BPRCostFunction
  )



  //----------------------------------------------------------------------------------------------
  //  3. For each snapshot, load and run our algorithm
  //----------------------------------------------------------------------------------------------

  val routingResult: LocalGraphRoutingModuleResult = LocalGraphRoutingModule.routeAllRequestedTimeGroups(conf, fileHelper, populationFull)
  fileHelper.savePopulation(routingResult.population, CombinedUESOExp, CombinedUESOPopulation)
  val soPopSize = routingResult.vehiclesRouted.size


  //----------------------------------------------------------------------------------------------
  //  4. Run 1-p% UE UNION p% SO Simulation, get overall congestion (measure?)
  //----------------------------------------------------------------------------------------------
  MATSimSingleAnalyticSnapshotRunnerModule(
    MATSimRunnerConfig(
      fileHelper.finalConfigFilePath(CombinedUESOExp),
      fileHelper.experimentPath(CombinedUESOExp),
      conf.algorithmTimeWindow,
      conf.startTime,
      conf.endTime,
      ArgsNotMissingValues
    ),
    networkData,
    BPRCostFunction
  )

  //----------------------------------------------------------------------------------------------
  //  5. Analyze Results (what kinds of analysis?)
  //----------------------------------------------------------------------------------------------
  // TODO: determine how to measure results

  // 1. compile travel times
  // 2. add data on routing times
  // 3. save, and print to screen

  fileHelper.appendToReportFile(PrintToResultFile(
    conf.populationSize,
    uePopSize,
    soPopSize,
    (conf.routePercentage * 100).toInt,
    conf.algorithmTimeWindow.toInt,
    routingResult.runTime,
    fileHelper.getPopulationAvgTravelTime(FullUEExp).getOrElse(-1D),
    fileHelper.getPopulationAvgTravelTime(CombinedUESOExp).getOrElse(-1D),
    fileHelper.getNetworkAvgTravelTime(FullUEExp).getOrElse(-1D),
    fileHelper.getNetworkAvgTravelTime(CombinedUESOExp).getOrElse(-1D)
  ))
}
