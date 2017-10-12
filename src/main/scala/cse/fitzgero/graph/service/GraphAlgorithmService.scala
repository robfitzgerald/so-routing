package cse.fitzgero.graph.service

import java.time.Instant

import cse.fitzgero.graph.basicgraph.{BasicGraph, BasicODPair}

trait GraphAlgorithmService { service =>
  type VertexId
  type EdgeId
  type Graph <: BasicGraph {
    type VertexId = service.VertexId
    type EdgeId = service.EdgeId
  }
  type ODPair <: BasicODPair[VertexId]
  type LoggingClass
  type ServiceResult <: { def logs: LoggingClass }
  val startTime: Long = Instant.now.toEpochMilli
  def runTime: Long = Instant.now.toEpochMilli - startTime
}