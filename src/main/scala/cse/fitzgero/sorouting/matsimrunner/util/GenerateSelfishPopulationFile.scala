package cse.fitzgero.sorouting.matsimrunner.util

import java.time.LocalTime

import scala.util.{Failure, Success}
import cse.fitzgero.sorouting.algorithm.pathsearch.sssp.localgraphsimplesssp.LocalGraphMATSimSSSP
import cse.fitzgero.sorouting.app.SORoutingApplicationConfig
import cse.fitzgero.sorouting.matsimrunner.population.PopulationOneTrip
import cse.fitzgero.sorouting.util._
import cse.fitzgero.sorouting.roadnetwork.costfunction.BPRCostFunction
import cse.fitzgero.sorouting.roadnetwork.localgraph.{LocalGraphMATSim, LocalGraphMATSimFactory}


/**
  * TODO: did these changes (commented-out code) correct the algorithm? if so, remove these comments
  */
object GenerateSelfishPopulationFile {
  def apply(populationFull: PopulationOneTrip, conf: SORoutingApplicationConfig, fileHelper: SORoutingFilesHelper): Int = {

    val numProcs = conf.processes match {
      case AllProcs => Runtime.getRuntime.availableProcessors()
      case OneProc => 1
      case NumProcs(n) => n
    }

    val (populationFullUE, numberOfRoutes) = {
      val sssp = LocalGraphMATSimSSSP()
      val graphWithNoFlows: LocalGraphMATSim =
        LocalGraphMATSimFactory(BPRCostFunction, AlgorithmFlowRate = conf.timeWindow)
          .fromFile(fileHelper.thisNetworkFilePath) match {
          case Success(g) => g
          case Failure(e) => throw new Error(s"failed to load network file ${fileHelper.thisNetworkFilePath}")
        }

      val populationDijkstrasRoutes =
        if (numProcs == 1)
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

      val populationUpdated = populationDijkstrasRoutes.foldLeft(populationFull)(_.updatePerson(_))
      (populationUpdated, populationDijkstrasRoutes.size)
    }

    fileHelper.savePopulation(populationFullUE, FullUEExp, FullUEPopulation)
    numberOfRoutes // overall number of routes (one per activity)
  }
}
