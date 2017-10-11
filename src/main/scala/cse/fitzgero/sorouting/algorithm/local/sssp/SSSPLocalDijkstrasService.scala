package cse.fitzgero.sorouting.algorithm.local.sssp

import java.time.Instant

import scala.annotation.tailrec
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import cse.fitzgero.graph.algorithm.ShortestPathAlgorithm
import cse.fitzgero.graph.service.ShortestPathService
import cse.fitzgero.sorouting.model.roadnetwork.local._


object SSSPLocalDijkstrasService extends ShortestPathService with ShortestPathAlgorithm {
  type VertexId = String
  type EdgeId = String
  type RequestId = String
  type Graph = LocalGraph
  override type ODPair = LocalODPair
  override type LoggingClass = Map[String, Long]
  case class PathSegment (e: EdgeId, cost: Option[Seq[Double]])
  case class AlgorithmResult(od: ODPair, path: Path) extends ShortestPathResult
  case class SSSPLocalDijkstrasServiceResult (result: AlgorithmResult, logs: LoggingClass) extends ShortestPathServiceResult

  /**
    * runs the concurrent shortest path service as a future and with details stored in a log
    * @param graph the road network graph
    * @param oDPair the origin and destination pair for this search
    * @return a shortest path and logging data, or nothing
    */
  override def runService(graph: Graph, oDPair: ODPair): Future[Option[SSSPLocalDijkstrasServiceResult]] = Future {
    val startTime = Instant.now.toEpochMilli
    runAlgorithm(graph, oDPair) match {
      case Some(result) =>
        val runTime = Instant.now.minusNanos(startTime).toEpochMilli
        val log = Map(
          "algorithm.sssp.local.runtime" -> runTime,
          "algorithm.sssp.local.success" -> 1L
        )
        Some(SSSPLocalDijkstrasServiceResult(result, log))
      case None => None
    }
  }

  /**
    * run a shortest path search
    * @param g the graph to search
    * @param od an origin-destination pair
    * @return the shortest path
    */
  override def runAlgorithm(g: Graph, od: ODPair): Option[AlgorithmResult] = {
    minSpanningDijkstras(g, od.src, Some(od.dst)) match {
      case Some(spanningTree) =>
        backPropagate(g, spanningTree, od.dst) match {
          case Some(path) =>
            Some(AlgorithmResult(od, path))
          case None =>
            None
        }
      case None =>
        None
    }
  }

  /**
    * tuple used to hold data in the heap representing the frontier of edges not yet visited
    * @param edge the edge in the search
    * @param cost the cost of traversing from the origin to this edge
    */
  private case class SearchData(edge: LocalEdge, cost: Double = 0D)

  /**
    * tuple used to represent an edge in our minimum spanning tree, which is held in a map of type $VertexId -> $BackPropagationData
    * @param π the edge between this vertex and the origin, which can be None if this vertex is the origin
    * @param d the cumulative distance cost
    */
  case class BackPropagateData(π: Option[EdgeId], d: Double)

  /**
    * produces a minimum spanning tree that terminates when destination is found. if destination is not provided, it terminates when all edges have been visited.
    * @param graph a graph with cost functions on it's edge attributes
    * @param origin the origin vertex
    * @param destination the destination vertex
    * @return a spanning tree, or nothing if a destination was submitted but never found
    */
  def minSpanningDijkstras (graph: Graph, origin: VertexId, destination: Option[VertexId] = None): Option[Map[VertexId, BackPropagateData]] = {

    // define the frontier, beginning with the neighbors of the origin vertex
    val frontierOrdering: Ordering[SearchData] = Ordering.by (_.cost)
    val frontier: collection.mutable.PriorityQueue[SearchData] = collection.mutable.PriorityQueue()(frontierOrdering.reverse)
    graph
      .outEdges(origin)
      .flatMap(graph.edgeById)
      .foreach(e => {
        val cost = e.attribute.linkCostFlow match {
          case Some(linkCost) => linkCost
          case None => Double.MaxValue  // ***** assumes that a missing cost function value means avoid this link. could be a config-set value.
        }
        frontier.enqueue(SearchData(e, cost))
      })

    @tailrec def _dijkstras (
      solution: Map[VertexId, BackPropagateData] = Map.empty[VertexId, BackPropagateData],
      visited: Set[EdgeId] = Set.empty[EdgeId]
    ): Option[Map[VertexId, BackPropagateData]] = {
      if (frontier.isEmpty)
        if (destination.isDefined) None // never found the goal
        else Some(solution) // minimum spanning tree from origin, has no destination
      else {
        val shortestFrontier = frontier.dequeue

        val solutionUpdate =
          if (!visited(shortestFrontier.edge.id) && !solution.isDefinedAt(shortestFrontier.edge.dst)) {
            //            val updatedDistance = solution(shortestFrontier.edge.src).d + shortestFrontier.cost
            solution.updated(shortestFrontier.edge.dst, BackPropagateData(Some(shortestFrontier.edge.id), shortestFrontier.cost))
          } else
            solution
        if (destination.isDefined && shortestFrontier.edge.dst == destination.get) {
          Some(solutionUpdate) // return solution tree with origin, destination present
        } else {

          graph
            .outEdges(shortestFrontier.edge.dst)
            .filter(!visited(_))
            .flatMap(e =>
              graph.edgeById(e) match {
                case Some(localEdge) => localEdge.attribute.linkCostFlow match {
                  case Some(linkCost) =>
                    val sourceCost: Double = solutionUpdate(localEdge.src).d
                    Some(SearchData(localEdge, linkCost + sourceCost))
                  case None => None
                }
                case None => None
              }
            ).foreach(e => frontier.enqueue(e) )

          _dijkstras(solutionUpdate, visited + shortestFrontier.edge.id)
        }
      }
    }

    _dijkstras(
      solution = Map(origin -> BackPropagateData(None, 0D))
    )
  }

  /**
    * find the path back from the destination to the origin via the spanning tree
    * @param g the original graph
    * @param spanningTree the map of vertices to the predecessor edges that should be taken in the tree
    * @param destination the destination vertex requested in the shortest path search
    * @return
    */
  def backPropagate(g: Graph, spanningTree: Map[VertexId, BackPropagateData], destination: VertexId): Option[Path] = {

    @tailrec def _backPropagate (
      currentVertex: VertexId,
      result: Path = Seq()
    ): Option[Path] = {
      if (spanningTree.isDefinedAt(currentVertex)) {
        val currentNode: BackPropagateData = spanningTree(currentVertex)
        currentNode.π match {
          case None => Some(result.reverse)
          case Some(edgeId) =>
            val edge = g.edgeById(edgeId).get
            val cost = edge.attribute.linkCostFlow.get
            _backPropagate(edge.src, result :+ PathSegment (edge.id, Some(Seq(cost))))
        }
      } else None
    }

    _backPropagate(destination)
  }
}
