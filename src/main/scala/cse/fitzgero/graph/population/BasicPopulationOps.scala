package cse.fitzgero.graph.population

import cse.fitzgero.graph.basicgraph.{BasicGraph, BasicPathSegment}

import scala.collection.GenSeq
;

trait BasicPopulationOps { pop =>
  // graph types
  type EdgeId
  type VertexId
  type Graph <: BasicGraph {
    type EdgeId = pop.EdgeId
    type VertexId = pop.VertexId
  }

  // request/response types
  type Path <: List[BasicPathSegment]
  type Request <: BasicRequest
  type Response <: BasicResponse
  type PopulationConfig

  /**
    * method to generate a collection of requests based on the graph topology
    * @param graph underlying graph structure
    * @param config information to constrain the generated data
    * @return a set of requests
    */
  def generateRequests(graph: Graph, config: PopulationConfig): GenSeq[Request]

  /**
    * turns a request into its XML representation
    * @param graph underlying graph structure
    * @param request request data
    * @return request in xml format
    */
  def generateXML(graph: Graph, request: Request): xml.Elem

  /**
    * turns a response into its XML representation
    * @param graph underlying graph structure
    * @param response response data
    * @return response in xml format
    */
  def generateXML(graph: Graph, response: Response): xml.Elem
}
