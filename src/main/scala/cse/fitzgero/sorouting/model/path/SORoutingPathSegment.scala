package cse.fitzgero.sorouting.model.path

import cse.fitzgero.graph.basicgraph.BasicPathSegment

/**
  * Representing a road segment and it's state as a vector of user-defined costs
  * @param edgeId the associated edge by ID as it corresponds to the underlying simulation or real data
  * @param cost an optional vector of calculated costs
  */
case class SORoutingPathSegment (edgeId: String, cost: Option[Seq[Double]]) extends BasicPathSegment with Serializable {
  override type EdgeId = String
}
