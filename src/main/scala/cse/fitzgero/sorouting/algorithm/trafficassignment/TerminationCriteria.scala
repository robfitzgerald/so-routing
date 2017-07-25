package cse.fitzgero.sorouting.algorithm.trafficassignment

import java.time.Instant

class TerminationData (val startTime: Long, val iter: Int, gapFn: => Double) {
  lazy val relGap: Double = gapFn
}

object TerminationData {
  def apply(startTime: Long, iter: Int, gapFn: => Double): TerminationData = new TerminationData(startTime, iter, gapFn)
}

sealed trait TerminationCriteria {
  def eval(t: TerminationData): Boolean
}
final case class RelativeGapTerminationCriteria (relGapThresh: Double) extends TerminationCriteria {
  override def eval(t: TerminationData): Boolean = t.relGap > relGapThresh
}
final case class IterationTerminationCriteria (iterThresh: Int) extends TerminationCriteria {
  override def eval(t: TerminationData): Boolean = t.iter >= iterThresh
}
final case class RunningTimeTerminationCriteria (timeThresh: Long) extends TerminationCriteria {
  override def eval(t: TerminationData): Boolean = Instant.now().toEpochMilli - t.startTime > timeThresh
}
final case class CombinedTerminationCriteria(a: TerminationCriteria, b: TerminationCriteria) extends TerminationCriteria {
  override def eval(t: TerminationData): Boolean = a.eval(t) && b.eval(t)
}