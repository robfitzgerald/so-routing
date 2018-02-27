package cse.fitzgero.sorouting.algorithm.local.sssp

import scala.annotation.tailrec
import cse.fitzgero.graph.algorithm.GraphRoutingAlgorithm
import cse.fitzgero.sorouting.model.roadnetwork.local._
import cse.fitzgero.sorouting.model.path.SORoutingPathSegment

object SSSPLocalDijkstrasAlgorithm extends GraphRoutingAlgorithm {
  type VertexId = String
  type EdgeId = String
  type Graph = LocalGraph
  override type AlgorithmRequest = LocalODPair
  override type AlgorithmConfig = Nothing
  override type PathSegment = SORoutingPathSegment
  case class AlgorithmResult(od: AlgorithmRequest, path: Path)
  /**
    * run a shortest path search
    * @param graph the graph to search
    * @param odPair an origin-destination pair
    * @param config (unused)
    * @return the shortest path
    */
  override def runAlgorithm(graph: Graph, odPair: AlgorithmRequest, config: Option[Nothing] = None): Option[AlgorithmResult] = {
    val requestName = s"REQ-${odPair.src}#${odPair.dst}"
    if (odPair.src == odPair.dst) {
      println(s"[SSSP] $requestName src equals dst, returning None for path")
      None
    } else {
      for {
        spanningTree <- minSpanningDijkstras(graph, odPair.src, Some(odPair.dst))
        path <- backPropagate(graph, spanningTree, odPair.dst)
      } yield {
//        println(s"[SSSP] $requestName returning shortest path")
        AlgorithmResult(odPair, path)
      }
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
//    println(s"[DIJ] search assets have been set up, with a starting frontier of ${frontier.size} edges")

    @tailrec def _dijkstras (
      solution: Map[VertexId, BackPropagateData] = Map.empty[VertexId, BackPropagateData],
      visited: Set[EdgeId] = Set.empty[EdgeId]
    ): Option[Map[VertexId, BackPropagateData]] = {
      if (visited.size > graph.edges.size) {
        println(s"[DIJ] somehow minimum spanning tree size exceeded size of original graph edge list, returning None for solution")
        None
      }
      else if (frontier.isEmpty) {
        if (destination.isDefined) {
          println(s"[DIJ] never found the goal, returning None for solution")
          None // never found the goal
        }
        else {
          println(s"[DIJ] no goal defined, completed a minimum spanning tree for ALL vertices, returning as result")
          Some(solution) // minimum spanning tree from origin, has no destination
        }
      }
      else {

        val shortestFrontier: SearchData = frontier.dequeue
        val solutionUpdate: Map[VertexId, BackPropagateData] =
          if (!visited(shortestFrontier.edge.id) && !solution.isDefinedAt(shortestFrontier.edge.dst)) {
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
    if (frontier.isEmpty) {
      println("[DIJ] dijkstras initial state with empty frontier, returning None for solution")
      None
    } else {
//      println("[DIJ] running _dijkstras to generate min spanning tree")
      _dijkstras(
        solution = Map(origin -> BackPropagateData(None, 0D))
      )
    }
  }

  /**
    * find the path back from the destination to the origin via the spanning tree. invariant is that the spanning tree contains both source and destination vertices.
    * @param g the original graph
    * @param spanningTree the map of vertices to the predecessor edges that should be taken in the tree
    * @param destination the destination vertex requested in the shortest path search
    * @return
    */
  def backPropagate(g: Graph, spanningTree: Map[VertexId, BackPropagateData], destination: VertexId): Option[Path] = {
    @tailrec def _backPropagate (
      currentVertex: VertexId,
      result: Path = List()
    ): Option[Path] = {
      if (spanningTree.isDefinedAt(currentVertex)) {
        val currentNode: BackPropagateData = spanningTree(currentVertex)
        currentNode.π match {
          case None =>
            Some(result.reverse)
          case Some(edgeId) =>
            val edge = g.edgeById(edgeId).get
            val cost = edge.attribute.linkCostFlow.get
            _backPropagate(edge.src, result :+ SORoutingPathSegment(edge.id, Some(Seq(cost))))
        }
      } else None
    }

    _backPropagate(destination)
  }
}
