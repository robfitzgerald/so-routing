package cse.fitzgero.graph.algorithm

import cse.fitzgero.graph.service.GraphAlgorithmService

import scala.collection.{GenMap, GenSeq}
import scala.concurrent.Future

trait RouteAlgorithm extends GraphAlgorithm {
  abstract class RouteResult {
    def paths: GenMap[ODPair, Path]
  }
  def runAlgorithm(g: Graph, odPairs: GenSeq[ODPair]): Option[RouteResult]
}

// TODO: uncouple alg from svc
trait RouteAlgorithmService extends GraphAlgorithmService with RouteAlgorithm {
  abstract class RouteServiceResult {
    def logs: LoggingClass
    def result: RouteResult
  }
  def runService(graph: Graph, odPairs: GenSeq[ODPair]): Future[Option[RouteServiceResult]]
}