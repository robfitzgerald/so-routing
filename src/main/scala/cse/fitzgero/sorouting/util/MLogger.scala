package cse.fitzgero.sorouting.util

abstract class MLogger [T] {
  def logs: Map[String, T]
  def get(k: String): Option[T] =
    if (logs.isDefinedAt(k)) Some(logs(k))
    else None
  def getOrElse(k: String, default: T): T = get(k) match {
    case Some(t) => t
    case None => default
  }
  def getOrZero(k: String): T = getOrElse(k, zero)
  protected def addValues(a: T, b: T): T
  protected def zero: T
  def update(k: String, v: T): MLogger[T]
  def ++ (that: MLogger[T]): MLogger[T]
  final def combine (that: MLogger[T]): Map[String, T] = {
    val (thisOverlap, thisExtras): (Map[String, T], Map[String, T]) = this.logs.partition(l => that.logs.isDefinedAt(l._1))
    val (thatOverlap, thatExtras): (Map[String, T], Map[String, T]) = that.logs.partition(r => this.logs.isDefinedAt(r._1))
    val addIntersectValues: Map[String, T] = thisOverlap.map(tup => tup._1 -> addValues(tup._2, thatOverlap(tup._1)))
    addIntersectValues ++ thisExtras ++ thatExtras
  }
}

case class AnalyticLogger (override val logs: Map[String, Double] = Map()) extends MLogger[Double] {
  override protected def addValues(a: Double, b: Double): Double = a + b
  override protected def zero: Double = 0
  override def update(k: String, v: Double): AnalyticLogger =
    AnalyticLogger(logs.updated(k,v))
  override def ++(that: MLogger[Double]): AnalyticLogger = {
    this.copy(logs = this.combine(that))
  }
}

case class RunTimeLogger (override val logs: Map[String, List[Long]] = Map()) extends MLogger[List[Long]] {
  override protected def addValues(a: List[Long], b: List[Long]): List[Long] = a ::: b
  override protected def zero: List[Long] = List()
  override def update(k: String, v: List[Long]): RunTimeLogger =
    RunTimeLogger(logs.updated(k,v))
  override def ++(that: MLogger[List[Long]]): RunTimeLogger = {
    this.copy(logs = this.combine(that))
  }
}

case class LogsGroup(analytic: AnalyticLogger = AnalyticLogger(), runTime: RunTimeLogger = RunTimeLogger()) {
  def ++(that: LogsGroup): LogsGroup = {
    this
      .copy(
        analytic = this.analytic ++ that.analytic,
        runTime = this.runTime ++ that.runTime
      )
  }
}