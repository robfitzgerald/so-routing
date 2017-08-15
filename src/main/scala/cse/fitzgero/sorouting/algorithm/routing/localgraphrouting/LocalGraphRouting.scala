package cse.fitzgero.sorouting.algorithm.routing.localgraphrouting

import java.time.Instant

import cse.fitzgero.sorouting.algorithm.pathsearch.ksp._

import scala.reflect.runtime.universe._
import scala.collection.{GenIterable, GenMap, GenSeq}
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
import cse.fitzgero.sorouting.algorithm.pathsearch.ksp.localgraphsimpleksp.{LocalGraphKSPSearchTree, LocalGraphSimpleKSP}
import cse.fitzgero.sorouting.algorithm.pathsearch.od.localgraph._
import cse.fitzgero.sorouting.algorithm.routing._
import cse.fitzgero.sorouting.algorithm.trafficassignment.{NoTrafficAssignmentSolution, TerminationCriteria, TrafficAssignmentResult}
import cse.fitzgero.sorouting.algorithm.trafficassignment.localgraph.{LocalGraphFWSolverResult, LocalGraphFrankWolfe}
import cse.fitzgero.sorouting.roadnetwork.localgraph._


object LocalGraphRouting extends Routing[LocalGraphMATSim, LocalGraphODPair] {


  override def route(g: LocalGraphMATSim, odPairs: Seq[LocalGraphODPair], config: RoutingConfig): Future[RoutingResult] = {
    val startTime = Instant.now().toEpochMilli

    val kShortestAsync: Future[GenSeq[KSPSearchRoot[VertexId, EdgeId]]] = findKShortest(g, odPairs, config).map(_.flatMap(LocalGraphKSPSearchTree(_) match {
      case KSPEmptySearchTree => None
      case x => Some(x.asInstanceOf[KSPSearchRoot[VertexId, EdgeId]])
    }))
    val trafficAssignmentOracleFlowAsync: Future[TrafficAssignmentResult] = trafficAssignmentOracleFlow(g, odPairs, config)

    val promise = Promise[RoutingResult]()

//    println("starting")
    Future {
      kShortestAsync onComplete {
        case Success(kShortestPaths: GenSeq[KSPSearchTree]) =>

          trafficAssignmentOracleFlowAsync onComplete {
            case Success(fwResult: TrafficAssignmentResult) =>
              fwResult match {
                case LocalGraphFWSolverResult(network, iter, time, relGap) =>
                  println(s"fw iters: $iter time: $time relGap: $relGap network:")
//                  println(s"$network")
//                  println(s"kShortest")
//                  println(s"${kShortestPaths.map(_.toString)}")

                  val runTime = Instant.now().toEpochMilli - startTime
                  promise.success(LocalGraphRoutingResult(selectRoutes(kShortestPaths, network), runTime))

                case _ => promise.failure(new IllegalStateException())
              }

            // TODO: call method to select routes using flow estimation as a heuristic

            case Failure(e) =>
              promise.failure(new IllegalStateException(e))
          }
        case Failure(error) =>
          promise.failure(new IllegalStateException(error)) // alternatives exist - see RoutingResult.scala for returning NoRoutingSolution(UnknownRoutingFailure)
      }
    }
    promise.future
  }

  def selectRoutes(trees: GenSeq[KSPSearchNode[EdgeId]], graph: LocalGraphMATSim): GenSeq[LocalGraphODPath] = {

    def _selectRoute(tree: KSPSearchTree): List[(EdgeId, Double)] = {
      tree match {
        case x if x.isInstanceOf[KSPSearchRoot[_, _]] =>
          val node = x.asInstanceOf[KSPSearchRoot[VertexId, EdgeId]]
          if (node.children.isEmpty) List[(String, Double)]()
          else {
            val (edge, cost, proportion): (EdgeId, Double, Double) = node.children.map(tup => (tup._1, tup._2._1, graph.edgeAttrOf(tup._1).get.flow)).maxBy(_._3)
            (edge, cost) :: _selectRoute(node.traverse(edge))
          }
        case y if y.isInstanceOf[KSPSearchBranch[_]] =>
          val node = y.asInstanceOf[KSPSearchBranch[EdgeId]]
          if (node.children.isEmpty) List[(String, Double)]()
          else {
            val (edge, cost, proportion): (EdgeId, Double, Double) = node.children.map(tup => (tup._1, tup._2._1, graph.edgeAttrOf(tup._1).get.flow)).maxBy(_._3)
            (edge, cost) :: _selectRoute(node.traverse(edge))
          }
        case KSPSearchLeaf => Nil
        case _ => Nil // or error
      }
    }

    trees.map({
      case x if x.isInstanceOf[KSPSearchRoot[_, _]] =>
        val node = x.asInstanceOf[KSPSearchRoot[VertexId,EdgeId]]
        val result: List[(EdgeId, Double)] = _selectRoute(x)
        val (path, cost) = result.unzip
        LocalGraphODPath(node.personId, node.srcVertex, node.dstVertex, path, cost)
      case _ =>
        LocalGraphODPath("",0,0,List(), List())
    })
  }



//  selecting our routes starts with a KSP tree for each od pair. we want to select exactly one route for that od.
//
//  map the collection of KSP trees to a recursive function that will
//   find the best child based on a lookup of the edge in the graph
//   add that edge to the head of a list and recurse by passing that child

  val KSP: LocalGraphSimpleKSP[LocalGraphMATSim, VertexMATSim, EdgeMATSim] = LocalGraphSimpleKSP[LocalGraphMATSim, VertexMATSim, EdgeMATSim]()


  def findKShortest(g: LocalGraphMATSim, odPairs: Seq[LocalGraphODPair], config: RoutingConfig): Future[GenSeq[GenSeq[LocalGraphODPath]]] = {
    config match {
      case ParallelRoutingConfig(k, kspBounds, _, procs, blockSize) =>
        // TODO: use procs value (modify ExecutionContext?)
        Future {
          odPairs.grouped(blockSize).flatMap(_.par.map(od => KSP.kShortestPaths(g, od, k, kspBounds))).toSeq
        }
      case LocalRoutingConfig(k, kSPBounds, _) =>
        Future {
          odPairs.map(od => KSP.kShortestPaths(g, od, k, kSPBounds))
        }
    }
  }


  def trafficAssignmentOracleFlow(g: LocalGraphMATSim, odPairs: GenSeq[LocalGraphODPair], config: RoutingConfig): Future[TrafficAssignmentResult] =
    config match {
      case ParallelRoutingConfig(k, kspBounds, _, procs, blockSize) =>
        // TODO: use procs value (modify ExecutionContext?)
        Future {
          LocalGraphFrankWolfe.solve(g.par, odPairs.par, config.fwBounds)
        }
      case LocalRoutingConfig(k, kSPBounds, _) =>
        Future {
          LocalGraphFrankWolfe.solve(g, odPairs, config.fwBounds)
        }
    }

}
