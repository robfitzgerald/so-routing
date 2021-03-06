package cse.fitzgero.sorouting.model.population

import java.time.LocalTime

import scala.collection.GenSeq

import cse.fitzgero.sorouting.model.roadnetwork.local.LocalODPair

class LocalPopulationSelectedSourceSinkGenerator (src: String, dst: String) extends LocalPopulationOps {

  /**
    * method to generate a collection of requests based on the graph topology
    * @param graph  underlying graph structure
    * @param config information to constrain the generated data
    * @return a set of requests
    */
  override def generateRequests(graph: Graph, config: PopulationConfig): GenSeq[Request] = {

    val offsetGenerator = timeDepartureOffsetGenerator()

    1 to config.n map (n => {

      val personId: String = s"$n-$src#$dst"

      val timeDepartureOffset = offsetGenerator(config.departureTimeRange)

      val time: LocalTime = config.meanDepartureTime.plusSeconds(timeDepartureOffset)

      LocalRequest(personId, LocalODPair(personId, src, dst), time)
    })
  }
}

object LocalPopulationSelectedSourceSinkGenerator {
  def apply(src: String, dst: String): LocalPopulationSelectedSourceSinkGenerator =
    new LocalPopulationSelectedSourceSinkGenerator(src, dst)
}