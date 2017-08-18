package cse.fitzgero.sorouting.util

/**
  * Defines a category of Population Types
  */
sealed trait SORoutingPopulationType

/**
  * The Full User Equilibrium Population matches the Full UE Experiment Type
  * @see FullUEExp
  */
case object FullUEPopulation extends SORoutingPopulationType {
  override def toString: String = s"population-full-ue"
}

/**
  * The Combined User Equilibrium / System Optimal Population matches the Combined UE/SO Experiment Type
  * @see CombinedUESOExp
  */
case object CombinedUESOPopulation extends SORoutingPopulationType {
  override def toString: String = s"population-combined-ue-so"
}
