package cse.fitzgero.sorouting.algorithm.trafficassignment

import java.time.Instant

import scala.collection.GenSeq

abstract class TrafficAssignment [G, O] {
  /**
    * solves the traffic assignment problem for the given graph and set of origin/destination pairs. od pairs are assumed to be individual agents, not flows of agents.
    * @param graph the network to solve on
    * @param odPairs the set of origin/destination pairs
    * @param terminationCriteria the way to determine convergence
    * @return a solution which contains the final graph estimation, or no solution
    */
  def solve (graph: G, odPairs: GenSeq[O], terminationCriteria: TerminationCriteria): TrafficAssignmentResult

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
      * calculates a linear phi value that starts from 0.66 and takes simple fraction steps (at first) toward 0
      * @param i current assignment algorithm iteration (assumed to begin at 1)
      * @return Phi
      */
    def linearFromIteration(i: Int): Phi = Phi(2.0D / (i + 2.0D))
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
}
