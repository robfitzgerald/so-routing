package cse.fitzgero.sorouting.algorithm.routing.localgraph

import java.time.Instant

import cse.fitzgero.sorouting.algorithm.pathsearch.ksp._
import cse.fitzgero.sorouting.algorithm.pathsearch.ksp.localgraphsimpleksp.{KSPLocalGraphMATSimResult, LocalGraphKSPSearchTree}
import cse.fitzgero.sorouting.algorithm.routing._
import cse.fitzgero.sorouting.algorithm.flowestimation.TrafficAssignmentResult
import cse.fitzgero.sorouting.algorithm.flowestimation.localgraph.LocalGraphFWSolverResult
import cse.fitzgero.sorouting.algorithm.pathselection.PathSelectionResult
import cse.fitzgero.sorouting.algorithm.pathselection.localgraph.{LocalGraphPathSelection, LocalGraphPathSelectionResult}
import cse.fitzgero.sorouting.matsimrunner.population.PopulationOneTrip
import cse.fitzgero.sorouting.roadnetwork.localgraph._
import cse.fitzgero.sorouting.util.ClassLogging

import scala.collection.GenSeq
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}


/**
  * runs fw, runs ksp on resulting graph, then selects routes based on the two results using a combinatorial search
  */
object LocalGraphRouting02 extends Routing[LocalGraphMATSim, PopulationOneTrip] with ClassLogging {

  override def route(g: LocalGraphMATSim, odPairs: PopulationOneTrip, config: RoutingConfig): Future[RoutingResult] = {

    val startTime = Instant.now().toEpochMilli
    val promise = Promise[RoutingResult]()

    Future {

      LocalGraphRoutingMethods.trafficAssignmentOracleFlow(g, odPairs.exportAsODPairsByVertex, config) onComplete {
        case Success(fwResult: TrafficAssignmentResult) =>
          fwResult match {
            case LocalGraphFWSolverResult(macroscopicFlowEstimate, fwIterations, fwRunTime, relGap) =>
              LocalGraphRoutingMethods.findKShortest(macroscopicFlowEstimate, odPairs.exportAsODPairsByEdge, config) map {
                case x: GenSeq[KSPLocalGraphMATSimResult] =>
                  val kTimesNPaths = x.map(_.paths)
                  val kspRunTime = Instant.now().toEpochMilli - startTime
                  (kspRunTime, LocalGraphPathSelection.run(kTimesNPaths, g))
                case _ => None
              } onComplete {
                case Success(result: (Long, LocalGraphPathSelectionResult)) =>
                  val kspRunTime = result._1
                  val routeSelectionRunTime = result._2.runTime
                  val routes = result._2.paths
                  val overallRunTime = Instant.now().toEpochMilli - startTime

                  promise.success(
                    LocalGraphRoutingResult(
                      routes = routes,
                      kspRunTime = kspRunTime,
                      fwRunTime = fwRunTime,
                      routeSelectionRunTime = routeSelectionRunTime,
                      overallRunTime = overallRunTime
                    )
                  )
                case Failure(e) =>
                  promise.failure(new IllegalStateException(e))
              }
          }
        case _ => promise.failure(new IllegalStateException())
      }
    }
    promise.future
  }
}
