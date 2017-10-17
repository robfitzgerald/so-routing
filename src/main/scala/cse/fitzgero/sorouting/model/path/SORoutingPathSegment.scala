package cse.fitzgero.sorouting.model.path

import cse.fitzgero.graph.basicgraph.BasicPathSegment

case class SORoutingPathSegment (edgeId: String, cost: Option[Seq[Double]]) extends BasicPathSegment { override type EdgeId = String }
