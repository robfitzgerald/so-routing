package cse.fitzgero.sorouting.matsimrunner.population

sealed trait PersonID {
}
case class SimplePersonID(id: String) extends PersonID {
  override def toString: String = id
}
case class CombinedPersonID(id: String, instance: String) extends PersonID {
  override def toString: String = s"$id-$instance"
}