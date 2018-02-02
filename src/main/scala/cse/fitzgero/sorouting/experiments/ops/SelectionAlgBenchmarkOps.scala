package cse.fitzgero.sorouting.experiments.ops

import scala.collection.GenSeq

import cse.fitzgero.graph.config.KSPBounds
import cse.fitzgero.sorouting.model.population.{LocalRequest, LocalResponse}
import cse.fitzgero.sorouting.model.roadnetwork.local.LocalGraph

object SelectionAlgBenchmarkOps {
  def generateBenchmarkFile(
    population: GenSeq[LocalRequest],
    sourceDirectory: String,
    k: Int, // number of alternate paths targeted per driver
    kspBounds: Option[KSPBounds], // way we terminate our ksp search
    overlapThreshold: Double, // what percent of edges alternates are permitted to have in common
    selectedRoutes: GenSeq[LocalResponse],
    costDifference: Double,
    destinationDirectory: String
  ): Unit = {
    // TODO: sharpen up. change parameter list?

    // put code in here

    // something like this:
    // dir/
    //   population.xml
    //   network.xml || something like a LocalGraph with Snapshot
    //   benchmark.xml { ksp data, meta data, selected routes, cost difference }

  }
}
