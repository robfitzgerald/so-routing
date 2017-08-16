package cse.fitzgero.sorouting.matsimrunner.population

sealed trait PersonID {
//  def == (that: String): Boolean = this.toString == that
}
case class SimplePersonID(id: String) extends PersonID {
  override def toString: String = id
//  override def == (that: String)
}
case class CombinedPersonID(id: String, instance: String) extends PersonID {
  override def toString: String = s"$id-$instance"
}