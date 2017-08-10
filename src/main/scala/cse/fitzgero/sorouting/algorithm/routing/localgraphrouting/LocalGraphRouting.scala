package cse.fitzgero.sorouting.algorithm.routing.localgraphrouting

import scala.collection.GenSeq
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
import cse.fitzgero.sorouting.algorithm.pathsearch.ksp.localgraphsimpleksp.LocalGraphSimpleKSP
import cse.fitzgero.sorouting.algorithm.pathsearch.od.localgraph._
import cse.fitzgero.sorouting.algorithm.routing._
import cse.fitzgero.sorouting.algorithm.trafficassignment.{NoTrafficAssignmentSolution, TerminationCriteria, TrafficAssignmentResult}
import cse.fitzgero.sorouting.algorithm.trafficassignment.localgraph.{LocalGraphFWSolverResult, LocalGraphFrankWolfe}
import cse.fitzgero.sorouting.roadnetwork.localgraph.{EdgeMATSim, LocalGraphMATSim, VertexMATSim}


object LocalGraphRouting extends Routing[LocalGraphMATSim, LocalGraphODPair] {

  val ksp: LocalGraphSimpleKSP[LocalGraphMATSim, VertexMATSim, EdgeMATSim] = LocalGraphSimpleKSP[LocalGraphMATSim, VertexMATSim, EdgeMATSim]()

  override def route(g: LocalGraphMATSim, odPairs: Seq[LocalGraphODPair], config: RoutingConfig): Future[RoutingResult] = {

    val kShortestAsync: Future[GenSeq[GenSeq[LocalGraphODPath]]] = findKShortest(g, odPairs, config)
    val trafficAssignmentOracleFlowAsync: Future[TrafficAssignmentResult] = trafficAssignmentOracleFlow(g, odPairs, config.fwBounds)

    val promise = Promise[RoutingResult]()

    Future {
      kShortestAsync onComplete {
        case Success(kShortestPaths) =>
          trafficAssignmentOracleFlowAsync onSuccess {
            case LocalGraphFWSolverResult(network, iter, time, relGap) =>

              println(s"fw iters: $iter time: $time relGap: $relGap network:")
              println(s"$network")
              println(s"kShortest")
              println(s"$kShortestPaths")

              // TODO: call method to select routes using flow estimation as a heuristic
              promise.success(LocalGraphRoutingResult(selectRoutes()))

            case NoTrafficAssignmentSolution(iter, time) =>
              promise.failure(new IllegalStateException())
          }
        case Failure(error) =>
          promise.failure(new IllegalStateException()) // alternatives exist - see RoutingResult.scala for returning NoRoutingSolution(UnknownRoutingFailure)
      }
    }
    promise.future
  }


  def selectRoutes(): Seq[LocalGraphODPath] = ???




  def findKShortest(g: LocalGraphMATSim, odPairs: Seq[LocalGraphODPair], config: RoutingConfig): Future[GenSeq[GenSeq[LocalGraphODPath]]] = {
    config match {
      case ParallelRoutingConfig(k, kspBounds, _, procs, blockSize) =>
        Future {
          odPairs.grouped(blockSize).flatMap(_.par.map(od => ksp.kShortestPaths(g, od, k, kspBounds))).toSeq
        }
      case LocalRoutingConfig(k, kSPBounds, _) =>
        Future {
          odPairs.map(od => ksp.kShortestPaths(g, od, k, kSPBounds))
        }
    }
  }

  def trafficAssignmentOracleFlow(g: LocalGraphMATSim, odPairs: GenSeq[LocalGraphODPair], terminationCriteria: TerminationCriteria): Future[TrafficAssignmentResult] =
    Future {
      LocalGraphFrankWolfe.solve(g, odPairs, terminationCriteria)
    }

}
