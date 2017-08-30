package cse.fitzgero.sorouting.matsimrunner.util

import java.time.LocalTime

import scala.util.{Failure, Success}
import cse.fitzgero.sorouting.algorithm.pathsearch.sssp.localgraphsimplesssp.LocalGraphMATSimSSSP
import cse.fitzgero.sorouting.matsimrunner.population.PopulationOneTrip
import cse.fitzgero.sorouting.util.{SORoutingApplicationConfig1, SORoutingFilesHelper}
import cse.fitzgero.sorouting.roadnetwork.costfunction.BPRCostFunction
import cse.fitzgero.sorouting.roadnetwork.localgraph.{LocalGraphMATSim, LocalGraphMATSimFactory}
import cse.fitzgero.sorouting.util.{FullUEExp, FullUEPopulation}


/**
  * TODO: did these changes (commented-out code) correct the algorithm? if so, remove these comments
  */
object GenerateSelfishPopulationFile {
  def apply(populationFull: PopulationOneTrip, conf: SORoutingApplicationConfig1, fileHelper: SORoutingFilesHelper): Int = {

    val SomeParallelProcessesSetting = 2

    //    val (populationSO, populationPartial) = populationFull.subsetPartition(conf.routePercentage)
    val (populationFullUE, numberOfRoutes) = {
      val sssp = LocalGraphMATSimSSSP()
      val graphWithNoFlows: LocalGraphMATSim =
        LocalGraphMATSimFactory(BPRCostFunction, AlgorithmFlowRate = conf.algorithmTimeWindow.toDouble)
          .fromFile(fileHelper.thisNetworkFilePath) match {
          case Success(g) => g
          case Failure(e) => throw new Error(s"failed to load network file ${fileHelper.thisNetworkFilePath}")
        }
      //      val populationDijkstrasRoutes =
      //        if (SomeParallelProcessesSetting == 1)  // TODO again, parallel config here.
      //          populationPartial.exportAsODPairsByEdge.map(sssp.shortestPath(graphWithNoFlows, _))
      //        else
      //          populationPartial.exportAsODPairsByEdge.par.map(sssp.shortestPath(graphWithNoFlows.par, _))
      val populationDijkstrasRoutes =
      if (SomeParallelProcessesSetting == 1)  // TODO again, parallel config here.
        populationFull
          .exportTimeGroup(LocalTime.parse(conf.startTime), LocalTime.parse(conf.endTime))
          .exportAsODPairsByEdge
          .map(sssp.shortestPath(graphWithNoFlows, _))
      else
        populationFull
          .exportTimeGroup(LocalTime.parse(conf.startTime), LocalTime.parse(conf.endTime))
          .exportAsODPairsByEdge
          .par
          .map(sssp.shortestPath(graphWithNoFlows.par, _))

      //      populationDijkstrasRoutes.foldLeft(populationPartial)(_.updatePerson(_))
      val populationUpdated = populationDijkstrasRoutes.foldLeft(populationFull)(_.updatePerson(_))
      (populationUpdated, populationDijkstrasRoutes.size)
    }
    //    val populationUERouted = populationFullUE.reintegrateSubset(populationSO)
    //    fileHelper.savePopulation(populationUERouted, FullUEExp, FullUEPopulation)
    fileHelper.savePopulation(populationFullUE, FullUEExp, FullUEPopulation)
    numberOfRoutes // overall number of routes (one per activity)
  }
}
