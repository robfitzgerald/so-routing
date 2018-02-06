package cse.fitzgero.mcts.core

import scala.util.Random

trait RandomGenerator {
  def nextInt(upperBounds: Int): Int
}

class BuiltInRandomGenerator(seed: Option[Long] = None) extends RandomGenerator {
  val random: Random = seed match {
    case Some(s) => new scala.util.Random(s)
    case None => new scala.util.Random()
  }

  override def nextInt(upperBounds: Int): Int = random.nextInt(upperBounds)
}
