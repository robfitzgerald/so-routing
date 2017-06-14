package cse.fitzgero.sorouting.roadnetwork.costfunction

import scala.math.pow
import breeze.interpolation._
import breeze.linalg.DenseVector

/**
  * latency function from the Bureau of Public Roads, taken from
  * U.S. Bureau of Public Roads. Traffic Assignment Manual. U.S. Department of Commerce, Washington, D.C (1964)
  * linear interpolated for query optimization
  * @note cannot be used as is in Spark, as breeze.interpolation.LinearInterpolator is not serializable
  */
class BPRInterpolatedCostFunction (capacity: Double, freeFlowSpeed: Double, numSteps: Int, stepSize: Int) extends CostFunction {
  override def generate: (Double) => Double = {
    val indices: DenseVector[Double] = DenseVector((0 to (numSteps * stepSize)).map(_.toDouble * (1.0 / stepSize)).toArray)
    val points: DenseVector[Double] = indices.map(scaledIndex => {
      freeFlowSpeed * (1 + 0.15D * pow(scaledIndex / capacity, 4))
    })

    val interpolatedFunction: LinearInterpolator[Double] = LinearInterpolator(indices, points)
    (flow: Double) => interpolatedFunction(flow)
  }
}

object BPRInterpolatedCostFunction extends CostFunctionFactory {
  val defaultCapacity: String = 100.toString
  val defaultFreeFlow: String = 50.toString
  val defaultnumSteps: String = 100.toString
  val defaultStepSize: String = 10.toString
  def apply(attributes: Map[String, String]): BPRInterpolatedCostFunction = {
    val cap: Double = attributes.getOrElse("capacity", defaultCapacity).toDouble
    val free: Double = attributes.getOrElse("freespeed", defaultFreeFlow).toDouble
    val steps: Int = attributes.getOrElse("numsteps", defaultnumSteps).toInt
    val size: Int = attributes.getOrElse("stepsize", defaultStepSize).toInt
    new BPRInterpolatedCostFunction(cap, free, steps, size)
  }
}