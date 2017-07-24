package cse.fitzgero.sorouting.algorithm.trafficassignment

abstract class TrafficAssignment [G, O]{
  def solve (graph: G, odPairs: Seq[O], terminationCriteria: TerminationCriteria): TrafficAssignmentResult[_]
}
