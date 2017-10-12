package cse.fitzgero.graph.service

import scala.collection.GenSeq
import scala.concurrent.Future

trait RouteService extends GraphAlgorithmService {
  def runService(graph: Graph, odPairs: GenSeq[ODPair]): Future[Option[ServiceResult]]
}