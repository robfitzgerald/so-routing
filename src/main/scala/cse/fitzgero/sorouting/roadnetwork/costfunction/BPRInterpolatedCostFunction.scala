package cse.fitzgero.sorouting.roadnetwork.costfunction

import scala.math.pow
import breeze.interpolation._
import breeze.linalg.DenseVector

case class BPRInterpCoefficients (capacity: Double, freeFlowSpeed: Double, stepSize: Int = 1000) extends CostFunctionCoefficients

object BPRInterpolatedCostFunction extends CostFunction[BPRInterpCoefficients] {
  override def generate(coef: BPRInterpCoefficients): (Double) => Double = {
    val indices: DenseVector[Double] = DenseVector((0 to coef.stepSize).map(_.toDouble * 0.1).toArray)
    val points: DenseVector[Double] = indices.map(scaledIndex => {
      coef.freeFlowSpeed * (1 + 0.15D * pow(scaledIndex / coef.capacity, 4))
    })

    val interpolatedFunction: LinearInterpolator[Double] = LinearInterpolator(indices, points)

    (flow: Double) => interpolatedFunction(flow)
  }
}
