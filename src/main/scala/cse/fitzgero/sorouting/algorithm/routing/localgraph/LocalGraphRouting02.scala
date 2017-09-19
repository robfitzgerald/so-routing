package cse.fitzgero.sorouting.algorithm.routing.localgraph

import java.time.Instant

import cse.fitzgero.sorouting.algorithm.pathsearch.ksp._
import cse.fitzgero.sorouting.algorithm.pathsearch.ksp.localgraphsimpleksp.{KSPLocalGraphMATSimResult, LocalGraphKSPSearchTree}
import cse.fitzgero.sorouting.algorithm.routing._
import cse.fitzgero.sorouting.algorithm.flowestimation.TrafficAssignmentResult
import cse.fitzgero.sorouting.algorithm.flowestimation.localgraph.LocalGraphFWSolverResult
import cse.fitzgero.sorouting.matsimrunner.population.PopulationOneTrip
import cse.fitzgero.sorouting.roadnetwork.localgraph._
import cse.fitzgero.sorouting.util.Logging

import scala.collection.GenSeq
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}


/**
  * runs fw, runs ksp on resulting graph, then selects routes based on the two results
  * this is probably a bad idea!!  the flows from fw are flows that we want to fit to, not flows we want to avoid. setting up KSP to
  * use the estimated flows will make KSP avoid those flows.
  */
object LocalGraphRouting02 extends Routing[LocalGraphMATSim, PopulationOneTrip] with Logging {


  override def route(g: LocalGraphMATSim, odPairs: PopulationOneTrip, config: RoutingConfig): Future[RoutingResult] = {

    val startTime = Instant.now().toEpochMilli
    val promise = Promise[RoutingResult]()

    Future {

      LocalGraphRoutingMethods.trafficAssignmentOracleFlow(g, odPairs.exportAsODPairsByVertex, config) onComplete {
        case Success(fwResult: TrafficAssignmentResult) =>
          fwResult match {
            case LocalGraphFWSolverResult(macroscopicFlowEstimate, fwIterations, fwRunTime, relGap) =>

              LocalGraphRoutingMethods.findKShortest(macroscopicFlowEstimate, odPairs.exportAsODPairsByEdge, config).map(_.flatMap(result => LocalGraphKSPSearchTree(result.paths) match {
                case KSPEmptySearchTree => None
                case x => Some((result, x.asInstanceOf[KSPSearchRoot[VertexId, EdgeId]]))
              })) onComplete {
                case Success(kShortestPaths: GenSeq[(KSPLocalGraphMATSimResult, KSPSearchTree)]) =>
                  val (kspResults, kspPaths) =
                    kShortestPaths
                      .unzip

                  val kspRunTime = Instant.now().toEpochMilli - startTime
                  val routeSelectionStartTime = Instant.now().toEpochMilli

                  val selectedSystemOptimalRoutes = LocalGraphRouteSelection.selectRoutes(kspPaths, macroscopicFlowEstimate)

                  val routeSelectionRunTime = Instant.now().toEpochMilli - routeSelectionStartTime
                  val overallRunTime = Instant.now().toEpochMilli - startTime

                  promise.success(
                    LocalGraphRoutingResult(
                      routes = selectedSystemOptimalRoutes,
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
