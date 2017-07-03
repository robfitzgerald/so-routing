package cse.fitzgero.sorouting.matsimrunner.population

import java.time.LocalTime

import cse.fitzgero.sorouting.roadnetwork.edge.EdgeIdType

import scala.xml.{Elem, XML}

sealed trait SamplingMethod {
  def apply(population: Set[PersonNode]): Stream[PersonNode]
}
object RandomSampling extends SamplingMethod {
  val random = new java.util.Random(System.currentTimeMillis)
  def setSeed(s: Long): Unit = random.setSeed(s)
  def apply(population: Set[PersonNode]): Stream[PersonNode] = {
    if (population.isEmpty) throw new IndexOutOfBoundsException("attempting to sample from empty set")
    else {
      val randIndex = random.nextInt(population.size)
      val nextPerson = population.iterator.drop(randIndex).next
      nextPerson #:: this(population - nextPerson)
    }
  }
}

case class Population (persons: Set[PersonNode], seed: Long = System.currentTimeMillis) extends ConvertsToXml {
  implicit val sampling = RandomSampling
  sampling.setSeed(seed)
  def toXml: xml.Elem = <population>{persons.map(_.toXml)}</population>
  def saveFile(fileName: String): Unit = XML.save(fileName, this.toXml)
  def subsetPartition(percentage: Double): (Population, Population) = {
    val numSampled = (percentage * persons.size).toInt
    val thisSampling: Set[PersonNode] = sampling(persons).take(numSampled).toSet
    (Population(thisSampling), Population(persons -- thisSampling))
  }
  def reintegrateSubset(subset: Population): Population = {
    Population(
      subset.persons.foldLeft(this.persons)((accum, updatedPerson) => {
        val toRemove = this.persons.find(_.id == updatedPerson.id)
        (accum -- toRemove) + updatedPerson
      })
    )
  }
  def updatePerson(id: PersonIDType, path: List[EdgeIdType]): Population = {
    persons.find(_.id == id) match {
      case None => this
      case Some(person) =>
        val updatedPerson: PersonNode = person.updatePath(path.head, path.last, path)
        Population((persons - person) + updatedPerson)
    }
  }
}

case class HomeConfig(name: String)
case class ActivityConfig(name: String, start: LocalTime, dur: LocalTime, dev: Long = 0L)
case class ModeConfig(name: String, probability: Double = 1.0D)
case class RandomPopulationConfig(pSize: Int, home: HomeConfig, activities: Seq[ActivityConfig], modes: Seq[ModeConfig])

object PopulationFactory {
  val Zero: Int = 0
  val random = new java.util.Random(System.currentTimeMillis)
  def setSeed(s: Long): Unit = random.setSeed(s)
  def generateSimpleRandomPopulation (network: xml.Elem, pSize: Int): Population = {
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
          List(MiddayActivity("work", work._2.x, work._2.y, work._1, Dur(times("work")))),
          EveningActivity("home", home._2.x, home._2.y, home._1)
        )
      }).toSet
    )
  }
  def generateRandomPopulation (network: xml.Elem, conf: RandomPopulationConfig): Population = {
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
          }).toList,
          EveningActivity(conf.home.name, homeLocation._2.x, homeLocation._2.y, homeLocation._1)
        )
      }).toSet
    )
  }
  private def evalModeProbability(modeConfig: ModeConfig): Boolean =
    random.nextDouble <= modeConfig.probability
}
