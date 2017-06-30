package cse.fitzgero.sorouting.matsimrunner.population

import java.time.LocalTime

case class Population (persons: Seq[PersonNode]) extends ConvertsToXml {
  def toXml: xml.Elem = <population>{persons.map(_.toXml)}</population>
}

case class HomeConfig(name: String)
case class ActivityConfig(name: String, start: LocalTime, dur: LocalTime, dev: Long = 0L)
case class ModeConfig(name: String, probability: Double = 1.0D)
case class RandomPopulationConfig(pSize: Int, home: HomeConfig, activities: Seq[ActivityConfig], modes: Seq[ModeConfig])

object PopulationFactory {
  val Zero: Int = 0
  val random = new java.util.Random(System.currentTimeMillis)
  def setSeed(s: Long): Unit = random.setSeed(s)
  def generateSimpleRandomPopulation (network: xml.Elem, pSize: Int): xml.Elem = {
    val activityLocations = ActivityLocation.takeAllLocations(network)
    val activityTimeGenerator =
      PopulationRandomTimeGenerator(
        Seq(
          ("home", NoDeviation(LocalTime.parse("09:00:00"))),
          ("work", NoDeviation(LocalTime.parse("08:00:00")))
        )
      )

    Population(
      (Zero until pSize).map(n => {
        val times = activityTimeGenerator.next()
        val home = ActivityLocation.pickRandomLocation(activityLocations)
        val work = ActivityLocation.pickRandomLocation(activityLocations)
        PersonNode(
          n.toString,
          "car",
          MorningActivity("home", home._2.x, home._2.y, home._1, EndTime(times("home"))),
          Seq(MiddayActivity("work", work._2.x, work._2.y, work._1, Dur(times("work")))),
          EveningActivity("home", home._2.x, home._2.y, home._1)
        )
      })
    ).toXml
  }
  def generateRandomPopulation (network: xml.Elem, conf: RandomPopulationConfig): xml.Elem = {
    val activityLocations = ActivityLocation.takeAllLocations(network)
    val activityTimeGenerator =
      PopulationRandomTimeGenerator(
        (conf.home.name, NoDeviation(conf.activities.head.start)) +:
        conf.activities.map(act => {
          (act.name, BidirectionalBoundedDeviation(act.dur, act.dev))
        })
      )

    Population(
      (Zero until conf.pSize).map(n => {
        val times = activityTimeGenerator.next()
        val homeLocation = ActivityLocation.pickRandomLocation(activityLocations)
        val actLocations = conf.activities.map(_ => ActivityLocation.pickRandomLocation(activityLocations))
        PersonNode(
          n.toString,
          conf.modes.filter(evalModeProbability).map(_.name).mkString(","),
          MorningActivity(conf.home.name, homeLocation._2.x, homeLocation._2.y, homeLocation._1, EndTime(times(conf.home.name))),
          conf.activities.zip(actLocations).map(act => {
            MiddayActivity(act._1.name, act._2._2.x, act._2._2.y, act._2._1, Dur(times(act._1.name)))
          }),
          EveningActivity(conf.home.name, homeLocation._2.x, homeLocation._2.y, homeLocation._1)
        )
      })
    ).toXml
  }
  private def evalModeProbability(modeConfig: ModeConfig): Boolean =
    random.nextDouble <= modeConfig.probability
}
