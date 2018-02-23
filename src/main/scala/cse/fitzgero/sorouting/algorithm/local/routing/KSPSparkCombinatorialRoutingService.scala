package cse.fitzgero.sorouting.algorithm.local.routing

import scala.collection.GenSeq
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}

import org.apache.spark.SparkContext

import cse.fitzgero.graph.algorithm.GraphBatchRoutingAlgorithmService
import cse.fitzgero.sorouting.algorithm.local.ksp._
import cse.fitzgero.sorouting.algorithm.local.mksp._
import cse.fitzgero.sorouting.algorithm.local.selection._
import cse.fitzgero.sorouting.model.population.{LocalRequest, LocalResponse}
import cse.fitzgero.sorouting.model.roadnetwork.local._

object KSPSparkCombinatorialRoutingService extends GraphBatchRoutingAlgorithmService {
  // types taken from SSSP
  override type VertexId = KSPLocalDijkstrasAlgorithm.VertexId
  override type EdgeId = KSPLocalDijkstrasAlgorithm.EdgeId
  override type Graph = KSPLocalDijkstrasAlgorithm.Graph
  type AlgorithmResult = SelectionSparkCombinatorialAlgorithm.AlgorithmResult

  // types for Routing Service
  override type ServiceRequest = GenSeq[LocalRequest]
  override type LoggingClass = Map[String, Long]
  case class ServiceResult(result: GenSeq[LocalResponse], logs: LoggingClass)
  type KSPConfig = KSPLocalDijkstrasConfig {
    def blockSize: Int
  }
  override type ServiceConfig = (KSPConfig, SparkContext)

  /**
    * run the combinatorial k-shortest-paths optimal routing algorithm as a concurrent service
    * @param graph underlying graph structure
    * @param request a single request or a batch request
    * @param config an object that states the number of alternate paths, the stopping criteria, and any dissimilarity requirements for the ksp algorithm
    * @return a future resolving to a list of true optimal paths from a combinatorial search
    */
  override def runService(graph: LocalGraph, request: ServiceRequest, config: Option[ServiceConfig]): Future[Option[ServiceResult]] = {
    val promise = Promise[Option[ServiceResult]]()
//    println()
//    println(s"expecting selection of ${request.size} requests")
    config match {
      case None => promise.success(None)
      case Some(conf) =>
        val (kspConfig, sparkContext) = (Some(conf._1), Some(conf._2))

        MKSPLocalDijkstrasService.runService(graph, request, kspConfig) map {
          case Some(ksp) =>
            SelectionSparkCombinatorialService.runService(graph, ksp.result, sparkContext) map {
              case Some(selection) =>
                val logs = Map[String, Long](
                  "algorithm.routing.local.success" -> 1L,
                  "algorithm.routing.local.batch.request.size" -> request.size,
                  "algorithm.routing.local.batch.completed" -> selection.result.size
                )


//                println(s"successful run of both services for ${request.size} requests and ${selection.result.size} results")
//                println(request)
//                println(selection.result)
//                println()

                promise.success(Some(ServiceResult(selection.result, logs ++ ksp.logs ++ selection.logs)))
              case None =>
                println("None result from selection service")
                println()
                promise.success(None)
            }
          case None =>
            println("None result from MKSP service")
            println()
            promise.success(None)
        }
    }
    promise.future
  }
}
