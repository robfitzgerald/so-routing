package cse.fitzgero.sorouting.model.roadnetwork.local

import cse.fitzgero.graph.population.BasicOD

case class LocalODPair (id: String, src: String, dst: String) extends BasicOD {
  override type VertexId = String
}