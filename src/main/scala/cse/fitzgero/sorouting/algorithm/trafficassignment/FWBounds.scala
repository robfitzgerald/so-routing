package cse.fitzgero.sorouting.algorithm.trafficassignment

import java.time.Instant

// assumptions: iterations begin at 1, startTime is LocalTime as milliseconds

// TODO: name of class should be less generic.  TrafficAssignmentTerminationCriteria?

/**
  * Holds data from an iteration of a traffic assignment solver to be used to signal completion of the algorithm
  * @param startTime time as milliseconds when algorithm began
  * @param iter current iteration of the algorithm, beginning with 1
  * @param gapFn the result of the relativeGap function. function call should be place inline as the argument directly
  */
class TerminationData (val startTime: Long, val iter: Int, gapFn: => Double) {
  lazy val relGap: Double = gapFn
}
object TerminationData {
  def apply(startTime: Long, iter: Int, gapFn: => Double): TerminationData = new TerminationData(startTime, iter, gapFn)
}

/**
  * an algebra of (composable) criteria used to evaluate algorithm completion
  */
sealed trait FWBounds {
  def eval(t: TerminationData): Boolean
}

/**
  * evaluate the tangent (i.e. optimization) as a termination criteria. default threshold should not exceed 0.1% and is recommended to be 0.0001% (Boyce, 2004)
  * @param relGapThresh a value in the range [0.0, 1.0] which will test as an upper threshold in a boolean function
  */
final case class RelativeGapFWBounds (relGapThresh: Double = 0.0001D) extends FWBounds {
  require(relGapThresh <= 0.1D)
  override def eval(t: TerminationData): Boolean = t.relGap < relGapThresh
}

/**
  * evaluate the current iteration as a termination criteria. iterations begin at 1 (per algorithm pseudocode in Modeling Transport).
  * @param iterThresh a value in the range [1, Int.MaxValue) which will test as an upper threshold inclusive
  */
final case class IterationFWBounds (iterThresh: Int) extends FWBounds {
  override def eval(t: TerminationData): Boolean = t.iter >= iterThresh
}

/**
  * evaluate the algorithm runtime as a termination criteria
  * @param timeThreshMs upper bound in ms. that tests if the algorithm should run another iteration
  */
final case class RunningTimeFWBounds (timeThreshMs: Long) extends FWBounds {
  override def eval(t: TerminationData): Boolean =
    Instant.now().toEpochMilli - t.startTime > timeThreshMs
}

/**
  * possible binary operators that can be used to combine termination criteria
  */
sealed trait SumOperation {
  def apply(a: Boolean, b: Boolean): Boolean
}

/**
  * performs a logical AND between the results of two termination criteria
  */
case object And extends SumOperation {
  def apply(a: Boolean, b: Boolean): Boolean = a && b
}

/**
  * performs a logical OR between the results of two termination criteria
  */
case object Or extends SumOperation {
  def apply(a: Boolean, b: Boolean): Boolean = a || b
}

/**
  * evaluates two termination criteria or a binary tree of criteria recursively
  * @param a left operand criteria
  * @param op binary operation used to combine the results of the left and right criteria evaluation
  * @param b right operand criteria
  */
final case class CombinedFWBounds(a: FWBounds, op: SumOperation, b: FWBounds) extends FWBounds {
  override def eval(t: TerminationData): Boolean = op(a.eval(t), b.eval(t))
}