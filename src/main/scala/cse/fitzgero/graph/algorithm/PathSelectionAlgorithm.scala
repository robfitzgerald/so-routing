package cse.fitzgero.graph.algorithm

import cse.fitzgero.graph.service.GraphAlgorithmService

import scala.collection.GenSeq
import scala.concurrent.Future

trait PathSelectionAlgorithm extends GraphAlgorithm {
  abstract class PathSelectionResult {
    def originalPaths: GenSeq[Path]
    def selectedPaths: GenSeq[Path]
  }
  def runAlgorithm(g: Graph, paths: GenSeq[Path]): Option[PathSelectionResult]
}

// TODO: uncouple alg from svc
trait PathSelectionService extends GraphAlgorithmService with PathSelectionAlgorithm {
  abstract class PathSelectionServiceResult {
    def logs: LoggingClass
    def result: PathSelectionResult
  }
  def runService(graph: Graph, paths: GenSeq[Path]): Future[Option[PathSelectionServiceResult]]
}