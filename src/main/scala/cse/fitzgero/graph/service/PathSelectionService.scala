package cse.fitzgero.graph.service

import scala.collection.GenSeq
import scala.concurrent.Future

trait PathSelectionService extends GraphAlgorithmService {
  type Path
  def runService(graph: Graph, paths: GenSeq[Path]): Future[Option[ServiceResult]]
}
