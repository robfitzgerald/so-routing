package cse.fitzgero.sorouting.network

/**
  * has a method to calculate the value used for calculating the objective
  * for
  */
trait CalculatesGradientObjective {
  /**
    * can be called to produce a known objective function value
    * @return a value to be minimized
    */
  def calculateObjective(): Double
}
