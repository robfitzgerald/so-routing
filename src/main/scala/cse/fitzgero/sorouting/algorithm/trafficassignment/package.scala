package cse.fitzgero.sorouting.algorithm

package object trafficassignment {
  sealed trait TerminationCriteria
  final case class RelativeGapTerminationCriteria (value: Double) extends TerminationCriteria
  final case class IterationTerminationCriteria (value: Int) extends TerminationCriteria
  final case class RunningTimeTerminationCriteria (value: Long) extends TerminationCriteria
}
