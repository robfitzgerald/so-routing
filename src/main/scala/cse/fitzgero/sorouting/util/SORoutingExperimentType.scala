package cse.fitzgero.sorouting.util

/**
  * Defines a category of Experiment Types
  */
sealed trait SORoutingExperimentType

/**
  * The Full User Equilibrium Experiement uses a population where all users are expected to have a selfish routing mechanism
  */
case object FullUEExp extends SORoutingExperimentType {
  override def toString: String = "full-ue"
}

/**
  * The Combined User Equilibrium / System Optimal Experiment uses a population where some are selfish, and some are routed via a SO algorithm
  */
case object CombinedUESOExp extends SORoutingExperimentType {
  override def toString: String = "combined-ue-so"
}