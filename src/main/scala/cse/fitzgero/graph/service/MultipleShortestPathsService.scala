package cse.fitzgero.graph.service

import scala.collection.GenSeq
import scala.concurrent.Future

trait MultipleShortestPathsService extends GraphAlgorithmService {
  def runService(graph: Graph, odPairs: GenSeq[ODPair]): Future[Option[ServiceResult]]
}
