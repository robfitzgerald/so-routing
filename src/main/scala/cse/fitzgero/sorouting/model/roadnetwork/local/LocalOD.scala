package cse.fitzgero.sorouting.model.roadnetwork.local

import cse.fitzgero.graph.basicgraph.{BasicODBatch, BasicODPair}

import scala.collection.GenSeq

case class LocalODPair (id: String, src: String, dst: String) extends BasicODPair[String]
case class LocalODBatch (ods: GenSeq[LocalODPair]) extends BasicODBatch[LocalODPair]