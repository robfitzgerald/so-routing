package cse.fitzgero.sorouting.algorithm.routing.localgraph

import java.time.Instant

import cse.fitzgero.sorouting.algorithm.pathsearch.ksp._

import scala.collection.GenSeq
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
import cse.fitzgero.sorouting.algorithm.pathsearch.ksp.localgraphsimpleksp.{KSPLocalGraphMATSimResult, LocalGraphKSPSearchTree}
import cse.fitzgero.sorouting.algorithm.routing._
import cse.fitzgero.sorouting.algorithm.flowestimation.TrafficAssignmentResult
import cse.fitzgero.sorouting.algorithm.flowestimation.localgraph.LocalGraphFWSolverResult
import cse.fitzgero.sorouting.matsimrunner.population.PopulationOneTrip
import cse.fitzgero.sorouting.roadnetwork.localgraph._
import cse.fitzgero.sorouting.util.Logging


/**
  * runs concurrent fw + ksp, then selects routes based on the two results
  */
object LocalGraphRouting01 extends Routing[LocalGraphMATSim, PopulationOneTrip] with Logging {
  override def route(g: LocalGraphMATSim, odPairs: PopulationOneTrip, config: RoutingConfig): Future[RoutingResult] = {
    val startTime = Instant.now().toEpochMilli

    // TODO better encapsulation of KSP calls
    val kShortestAsync: Future[GenSeq[(KSPLocalGraphMATSimResult, KSPSearchRoot[VertexId, EdgeId])]] = LocalGraphRoutingMethods.findKShortest(g, odPairs.exportAsODPairsByEdge, config).map(_.flatMap(result => LocalGraphKSPSearchTree(result.paths) match {
      case KSPEmptySearchTree => None
      case x => Some((result, x.asInstanceOf[KSPSearchRoot[VertexId, EdgeId]]))
    }))
    val trafficAssignmentOracleFlowAsync: Future[TrafficAssignmentResult] = LocalGraphRoutingMethods.trafficAssignmentOracleFlow(g, odPairs.exportAsODPairsByVertex, config)

    val promise = Promise[RoutingResult]()

    Future {
      kShortestAsync onComplete {
        case Success(kShortestPaths: GenSeq[(KSPLocalGraphMATSimResult, KSPSearchTree)]) =>

          val (kspResults, kspPaths) =
            kShortestPaths
              .unzip

//          more interesting results can be processed here, but summing ksp runtimes will not be a true overall runtime since they can be parallelized
//          val kspRunTime = kspResults.map(_.runTime).sum
          val kspRunTime = Instant.now().toEpochMilli - startTime

          trafficAssignmentOracleFlowAsync onComplete {
            case Success(fwResult: TrafficAssignmentResult) =>
              fwResult match {
                case LocalGraphFWSolverResult(macroscopicFlowEstimate, fwIterations, fwRunTime, relGap) =>
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

                case _ => promise.failure(new IllegalStateException())
              }
            case Failure(e) =>
              promise.failure(new IllegalStateException(e))
          }
        case Failure(error) =>
          promise.failure(new IllegalStateException(error)) // alternatives exist - see RoutingResult.scala for returning NoRoutingSolution(UnknownRoutingFailure)
      }
    }
    promise.future
  }
}
