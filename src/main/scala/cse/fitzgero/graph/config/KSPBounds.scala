package cse.fitzgero.graph.config

/**
  * algorithm setting declaring how to end search for alternate paths
  * declared as type: "KSPBounds". called as member: "KSPBounds.Iteration()"
  */
sealed trait KSPBounds {
  def name: String
  def value: String
}

object KSPBounds {

  /**
    * run i iterations of the KSP algorithm (some of which may produce invalid results)
    * @param i number of iterations
    */
  case class Iteration(i: Int) extends KSPBounds {
    override def name: String = "Terminate KSP by number of iterations"
    override def value: String = i.toString
  }

  /**
    * find n paths, which may take more than i iterations, n = i
    * @param n the number of valid alternative paths to find
    */
  case class PathsFound(n: Int) extends KSPBounds {
    override def name: String = "Terminate KSP by number of paths found"
    override def value: String = n.toString
  }

  /**
    * run algorithm until we go beyond the listed time limit
    * @param ms time duration, milliseconds
    */
  case class Time(ms: Long) extends KSPBounds {
    override def name: String = "Terminate KSP by time, in milliseconds"
    override def value: String = ms.toString
  }

  /**
    * run i iterations of the KSP algorithm, unless we exceed ms milliseconds
    * @param i number of iterations
    * @param ms time duration, milliseconds
    */
  case class IterationOrTime(i: Int, ms: Long) extends KSPBounds {
    override def name: String = "Terminate KSP by iteration or time, whichever is first"
    override def value: String = s"${i.toString},${ms.toString}"
  }

}