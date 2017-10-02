package cse.fitzgero.sorouting.algorithm.routing.localgraph

import java.time.Instant

import cse.fitzgero.sorouting.algorithm.pathsearch.ksp.localgraphsimpleksp.KSPLocalGraphMATSimResult
import cse.fitzgero.sorouting.algorithm.pathselection.localgraph.{LocalGraphPathSelection, LocalGraphPathSelectionResult}
import cse.fitzgero.sorouting.algorithm.pathselection.{PathSelectionEmptySet, PathSelectionResult}
import cse.fitzgero.sorouting.algorithm.routing._
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

    //    Future {
    if (odPairs.size == 0) promise.success(RoutingEmptyRequests)
    else {
      val kspFuture: Future[GenSeq[KSPLocalGraphMATSimResult]] = LocalGraphRoutingMethods.findKShortest(g, odPairs.exportAsODPairsByEdge, config)

      kspFuture flatMap (kspResult => {
        val kTimesNPaths = kspResult.map(_.paths)
        LocalGraphPathSelection.run(kTimesNPaths, g)
      }) onComplete {

        case Failure(e) => promise.failure(e)

        case Success(selectionResult) => selectionResult match {
          case PathSelectionEmptySet => promise.success(RoutingEmptyRequests)
          case selectionResult: LocalGraphPathSelectionResult =>
//            val routes = selectionResult.paths
            val routeSelectionRunTime = selectionResult.runTime
            val overallRunTime = Instant.now().toEpochMilli - startTime

            promise.success(LocalGraphRoutingResult(
              routes = selectionResult.paths,
              kspRunTime = -1L,
              fwRunTime = -1L,
              routeSelectionRunTime = routeSelectionRunTime,
              overallRunTime = overallRunTime
            ))
          case other => promise.failure(new IllegalArgumentException(s"PathSelectionResult with incorrect type was returned: ${other.getClass}"))
        }
      }
    }
    promise.future
  }
}

//      kspFuture onComplete {
//        case Failure(e) =>
//          promise.failure(e)
//        case Success(kspResult: GenSeq[KSPLocalGraphMATSimResult]) =>
//          val kTimesNPaths = kspResult.map(_.paths)
//          val kspRunTime = Instant.now().toEpochMilli - startTime
//
//          LocalGraphPathSelection.run(kTimesNPaths, g) onComplete {
//            case Failure(e) =>
//              promise.failure(e)
//            case Success(selectionResult) =>
//              selectionResult match {
//                case PathSelectionEmptySet =>
//                  promise.success(RoutingEmptyRequests)
//                case selectionResult: LocalGraphPathSelectionResult =>
//                  val routes = selectionResult.paths
//                  val routeSelectionRunTime = selectionResult.runTime
//                  val overallRunTime = Instant.now().toEpochMilli - startTime
//                  val returnObject = LocalGraphRoutingResult(
//                    routes = routes,
//                    kspRunTime = -1L,
//                    fwRunTime = -1L,
//                    routeSelectionRunTime = routeSelectionRunTime,
//                    overallRunTime = overallRunTime
//                  )
//
//                  promise.success(returnObject)
//              }
//          }
//      }
//    }
//    }


