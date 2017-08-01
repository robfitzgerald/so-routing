package cse.fitzgero.sorouting.algorithm.trafficassignment

import java.time.Instant

abstract class TrafficAssignment [G, O] {
  /**
    * solves the traffic assignment problem for the given graph and set of origin/destination pairs. od pairs are assumed to be individual agents, not flows of agents.
    * @param graph the network to solve on
    * @param odPairs the set of origin/destination pairs
    * @param terminationCriteria the way to determine convergence
    * @return a solution which contains the final graph estimation, or no solution
    */
  def solve (graph: G, odPairs: Seq[O], terminationCriteria: TerminationCriteria): TrafficAssignmentResult

  /**
    * holds the percentage value 'phi' and it's inverse during an iteration of a traffic assignment step
    * @param value the position on the real number line which is used to define a proportion from the previous estimation to the current estimation
    */
  case class Phi (value: Double) {
    require(value <= 1 && value >= 0, s"Phi is only defined for values in the range [0,1], but found $value")
    val inverse: Double = 1.0D - value
  }

  /**
    * convenience methods for types of Phi values
    */
  case object Phi {
    /**
      * calculates a linear phi value that starts from 1 and takes simple fraction steps (at first) toward 0
      * @param i current assignment algorithm iteration (assumed to begin at 1)
      * @return Phi
      */
    def linearFromIteration(i: Int): Phi = Phi(2.0D / (i + 1.0D))
  }

  /**
    * used to find the flow values for the current phase of Frank-Wolfe
    *
    * @param phi      a phi value with it's inverse (1 - phi) pre-calculated, for performance
    * @param linkFlow this link's flow of the previous FW iteration
    * @param aonFlow  this link's flow in the All-Or-Nothing assignment
    * @return the flow value for the next step
    */
  def frankWolfeFlowCalculation(phi: Phi, linkFlow: Double, aonFlow: Double): Double = (phi.inverse * linkFlow) + (phi.value * aonFlow)

//  /**
//    * parses the user-passed termination criteria and evaluates it with values taken from the current assignment iteration
//    * @param terminationCriteria a case class object that defines the type of criteria and results in it's calculation
//    * @param relGap the calculated relative gap (error) value
//    * @param startTime the time that the assignment algorithm began
//    * @param iter the current iteration of the assignment algorithm
//    * @return
//    */
//  def evaluateStoppingCriteria(terminationCriteria: TerminationCriteria, relGap: => Double, startTime: Long, iter: Int): Boolean = terminationCriteria match {
//    case RelativeGapTerminationCriteria(relGapThresh) => relGap > relGapThresh
//    case IterationTerminationCriteria(iterThresh) => iter >= iterThresh
//    case RunningTimeTerminationCriteria(timeThresh) => Instant.now().toEpochMilli - startTime > timeThresh
//    case AllTerminationCriteria(relGapThresh, iterThresh, timeThresh) =>
//      relGap > relGapThresh &&
//        iter >= iterThresh &&
//        Instant.now().toEpochMilli - startTime > timeThresh
//  }
}
