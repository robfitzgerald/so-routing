package cse.fitzgero.sorouting.algorithm.pathsearch.ksp

case class KSPBoundsData(t: Long, n: Int)

sealed trait KSPBounds {
  /**
    * predicate which is true when the algorithm should stop
    * @param data time and iteration of current algorithm
    * @return true -> stop
    */
  def test(data: KSPBoundsData): Boolean
}

/**
  * should run K-Shortest Paths until time 't' has passed.
  * @param t time bounds to test for
  */
case class TimeBounds(t: Long) extends KSPBounds {
  override def test(currentData: KSPBoundsData): Boolean =
    currentData.t > t
}

/**
  * should run K-Shortest Paths until n paths have been found (should be greater than k).
  * @param n number of paths to find
  */
case class PathsFoundBounds(n: Int) extends KSPBounds {
  override def test(currentData: KSPBoundsData): Boolean =
    currentData.n > n
}

case class CombinedBounds(a: KSPBounds, b: KSPBounds) extends KSPBounds {
  override def test(data: KSPBoundsData): Boolean = a.test(data) && b.test(data)
}

case object NoKSPBounds extends KSPBounds {
  override def test(ignored: KSPBoundsData): Boolean = false
}