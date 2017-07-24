package cse.fitzgero.sorouting.algorithm.trafficassignment

sealed trait TerminationCriteria
final case class RelativeGapTerminationCriteria (value: Double) extends TerminationCriteria
final case class IterationTerminationCriteria (value: Int) extends TerminationCriteria
final case class RunningTimeTerminationCriteria (value: Long) extends TerminationCriteria
final case class AllTerminationCriteria(relGap: Double, iteration: Int, runTime: Long) extends TerminationCriteria