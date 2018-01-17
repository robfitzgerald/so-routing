package cse.fitzgero.sorouting.matsimrunner.snapshot

object SnapshotCollector {
  // a snapshot is a collection of elements that model link state

  // modeling a link's state requires a set of current vehicles on that link and a cost function to apply when we want to evaluate the snapshot

  // reporting the overall congestion information requires listing out deltas in the changing link state

  // a link needs a toXml method

  // we could copy + refactor AnalyticLinkData, or parameterize the cost function type (possibly)

  // the only things that we need to refactor in this are
  // - the cost function integration in AnalyticLinkData
  // - the call to updateCongestion in AnalyticLinkData which depends on computing the cost function
  // - the NetworkStateAnalyticCollector object apply method

  // SOOOOOOOO

  // copy with minimal refactor, or complete refactor?
}
