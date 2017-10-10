package cse.fitzgero.sorouting.model.roadnetwork.local

import cse.fitzgero.graph.basicgraph.BasicODPair

case class LocalODPair (id: String, src: String, dst: String) extends BasicODPair[String]
