package cse.fitzgero.sorouting.matsimrunner.population

import java.time.LocalTime

import scala.util.Random

abstract class ActivityTime (time: LocalTime) {
  def nextSample(): Long = throw new NotImplementedError("Any ActivityTimeAndDeviation subclass should implement it's own random generator function with the signature 'def nextSample(): Long'")
  def nextTime(): LocalTime = time.plusMinutes(nextSample())
}

final case class NoDeviation(time: LocalTime) extends ActivityTime(time) {
  override def nextSample(): Long = 0L
}

/**
  * produces random values in a gaussian distribution within the range [-deviation, deviation]
  * @param deviation standard deviation, in minutes
  * @param seed random seed value
  */
final case class BidirectionalDeviation(time: LocalTime, deviation: Long, seed: Long = System.currentTimeMillis) extends ActivityTime(time) {
  private val random: Random = new Random(seed)
  override def nextSample(): Long = (random.nextGaussian() * deviation).toLong
}

/**
  * produces random values in a gaussian distribution within the range [-low, high]
  * @param low lower bound of the range, in minutes
  * @param high upper bound of the range, in minutes
  * @param seed random seed value
  */
final case class RangeDeviation(time: LocalTime, low: Long, high: Long, seed: Long = System.currentTimeMillis) extends ActivityTime(time) {
  private val mid: Long = (low + high) / 2L
  private val random: Random = new Random(seed)
  override def nextSample(): Long = (random.nextGaussian() * mid).toLong
}

/**
  * produces random values in a gaussian distribution within the range [-deviation, deviation]
  * @param maxDistance 2 standard deviations from the mean, which will also serve as an upper/lower bound of the result value, in minutes
  * @param seed random seed value
  */
final case class BidirectionalBoundedDeviation(time: LocalTime, maxDistance: Long = 0L, seed: Long = System.currentTimeMillis) extends ActivityTime(time) {
  val deviation: Long = maxDistance / 2L
  val random: Random = new Random(seed)
  override def nextSample(): Long = {
    if (maxDistance == 0) 0L
    else {
      val next = (random.nextGaussian() * deviation).toLong
      if (next < (-maxDistance)) -maxDistance
      else if (next > maxDistance) maxDistance
      else next
    }
  }
}