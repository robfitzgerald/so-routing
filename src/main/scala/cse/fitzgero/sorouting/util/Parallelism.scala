package cse.fitzgero.sorouting.util

sealed trait Parallelism
case class NumProcs(n: Int) extends Parallelism {
  require(n > 1)
}
case object OneProc extends Parallelism {
  override def toString: String = "1"
}
case object AllProcs extends Parallelism {
  override def toString: String = "*"
}