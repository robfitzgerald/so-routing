package cse.fitzgero.sorouting.matsimrunner.population

import java.time.LocalTime

case class Population (persons: Seq[PersonNode]) extends PopulationDataThatConvertsToXml {
  def toXml: xml.Elem = <population>{persons.map(_.toXml)}</population>
}

object PopulationFactory {
  def generateSimpleRandomPopulation (network: xml.Elem, pSize: Int): xml.Elem = {
    val activityLocations = ActivityLocation.takeAllLocations(network)
    val activityTimeGenerator =
      PopulationRandomTimeGenerator(
        Seq(
          ("home", BidirectionalBoundedDeviation(LocalTime.parse("09:00:00"), 0L)),
          ("work", BidirectionalBoundedDeviation(LocalTime.parse("08:00:00"), 0L))
        )
      )

    Population(
      (0 until pSize).map(n => {
        val times = activityTimeGenerator.next()
        val home = ActivityLocation.pickRandomLocation(activityLocations)
        val work = ActivityLocation.pickRandomLocation(activityLocations)
        PersonNode(
          n.toString,
          "car",
          MorningActivity("home", home._2.x, home._2.y, home._1, EndTime(times("home"))),
          MiddayActivity("work", work._2.x, work._2.y, work._1, Dur(times("work"))),
          EveningActivity("home", home._2.x, home._2.y, home._1)
        )
      })
    ).toXml
  }
}
