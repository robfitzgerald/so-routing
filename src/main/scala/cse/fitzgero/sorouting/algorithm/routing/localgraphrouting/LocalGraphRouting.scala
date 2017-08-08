package cse.fitzgero.sorouting.algorithm.routing.localgraphrouting

import scala.collection.GenSeq
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
import cse.fitzgero.sorouting.algorithm.pathsearch.ksp.localgraphsimpleksp.SimpleKSP
import cse.fitzgero.sorouting.algorithm.pathsearch.od.localgraph._
import cse.fitzgero.sorouting.algorithm.routing._
import cse.fitzgero.sorouting.algorithm.trafficassignment.{NoTrafficAssignmentSolution, TerminationCriteria, TrafficAssignmentResult}
import cse.fitzgero.sorouting.algorithm.trafficassignment.localgraph.{LocalGraphFWSolverResult, LocalGraphFrankWolfe}
import cse.fitzgero.sorouting.roadnetwork.localgraph.{EdgeMATSim, LocalGraphMATSim, VertexMATSim}


object LocalGraphRouting extends Routing[LocalGraphMATSim, LocalGraphODPair] {

  val ksp: SimpleKSP[LocalGraphMATSim, VertexMATSim, EdgeMATSim] = SimpleKSP[LocalGraphMATSim, VertexMATSim, EdgeMATSim]()

  override def route(g: LocalGraphMATSim, odPairs: Seq[LocalGraphODPair], config: RoutingConfig): Future[RoutingResult] = {
    val kShortestPromise: Future[GenSeq[GenSeq[LocalGraphODPath]]] = findKShortest(g, odPairs, config)
    val trafficAssignmentOracleFlowPromise: Future[TrafficAssignmentResult] = trafficAssignmentOracleFlow(g, odPairs, config.fwBounds)

    val p = Promise[RoutingResult]()
    val f = p.future

    Future {
      kShortestPromise onComplete {
        case Success(kShortest) =>
          trafficAssignmentOracleFlowPromise onSuccess {
            case LocalGraphFWSolverResult(network, iter, time, relGap) =>

              println(s"fw iters: $iter time: $time relGap: $relGap network:")
              println(s"$network")
              println(s"kShortest")
              println(s"$kShortest")
              // TODO: call method to select routes using flow estimation as a heuristic
              p.success(LocalGraphRoutingResult(Seq()))

            case NoTrafficAssignmentSolution(iter, time) =>
              p.failure(new IllegalStateException())
          }
        case Failure(error) =>
          p.failure(new IllegalStateException()) // alternatives exist - see RoutingResult.scala for returning NoRoutingSolution(UnknownRoutingFailure)
      }
    }
    p.future
  }


  // TODO: where do we worry about parallelism? should i expect odPairs are local at this point, or would we like paralellism to be declared by the user who choses a par collection?
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
