package cse.fitzgero.sorouting.experiments.ops

import java.nio.file.{Files, Paths}

import cse.fitzgero.graph.config.KSPBounds

object SelectionAlgBenchmarkOps {

  /**
    * takes all the data needed for a benchmark file and creates an xml object for it
    * @param name
    * @param population
    * @param sourceDirectory
    * @param k
    * @param kspBounds
    * @param overlapThreshold
    * @param selectedRoutes
    * @param costDifference
    * @param destinationDirectory
    * @return
    */
  def generateBenchmarkFile(
    name: String,
    population: xml.Elem,
    sourceDirectory: String, // data/source with original network.xml, config.xml
    k: Int, // number of alternate paths targeted per driver
    kspBounds: Option[KSPBounds], // way we terminate our ksp search
    overlapThreshold: Double, // what percent of edges alternates are permitted to have in common
    selectedRoutes: xml.Elem,
    costDifference: Double,
    destinationDirectory: String
  ): xml.Elem = {
    // TODO: sharpen up. change parameter list?

    val networkName: String = "/".r.split(sourceDirectory).last

    val (kspType: String, kspVal: String) = kspBounds match {
      case None => ("none", "")
      case Some(kspB) => (kspB.name, kspB.value)
    }

    val meaningfulName: String = s"$networkName-${population.size}-k$k-$kspType-$kspVal-$overlapThreshold"

    <benchmark-selection-algorithm>
      <name>{meaningfulName}</name>
      <config>
        <source-directory>{sourceDirectory}</source-directory>
        <ksp>
          <k>{k}</k>
          <ksp-type>{kspType}</ksp-type>
          <ksp-value>{kspVal}</ksp-value>
          <overlap-threshold>{overlapThreshold}</overlap-threshold>
        </ksp>
      </config>
      <population>{population}</population>
      <selected-routes>{selectedRoutes}</selected-routes>
      <cost-difference>{costDifference}</cost-difference>
    </benchmark-selection-algorithm>
  }

  def writeBenchmarkFiles(network: xml.Elem, benchmark: xml.Elem): Unit = {

    val path: String = s"${ExperimentFSOps.benchmarkSelectionAlgorithmDirectory}/${(benchmark \ "name").text}"
    Files.createDirectories(Paths.get(path))

    ExperimentFSOps.saveXmlDocType(
      ExperimentFSOps.networkFileURI(path),
      network,
      ExperimentFSOps.NetworkDocType
    )

    ExperimentFSOps.saveXmlDocType(
      ExperimentFSOps.benchmarkSelectionAlgorithmURI(path),
      benchmark,
      ExperimentFSOps.BenchmarkSelectionAlgorithmDocType
    )
  }

}
