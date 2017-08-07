package cse.fitzgero.sorouting.matsimrunner.population

import java.time.LocalTime

import cse.fitzgero.sorouting.algorithm.pathsearch.mssp.graphx.simplemssp._

import scala.xml.dtd.{DocType, SystemID}
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
  // random values
  implicit val sampling = RandomSampling
  sampling.setSeed(seed)

  // xml save operation
  val populationDocType = DocType("population", SystemID("http://www.matsim.org/files/dtd/population_v6.dtd"), Nil)
  val WriteXmlDeclaration = true
  def toXml: xml.Elem = <population>{persons.map(_.toXml)}</population>
  def saveFile(fileName: String): Unit = XML.save(fileName, this.toXml, "UTF-8", WriteXmlDeclaration, populationDocType)

  // population operations
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
  def updatePerson(data: SimpleMSSP_ODPath): Population = {
    persons.find(_.id == data.personId) match {
      case None => this
      case Some(person) =>
        val updatedPerson: PersonNode = person.updatePath(data.srcVertex, data.dstVertex, data.path)
        Population((persons - person) + updatedPerson)
    }
  }

  // export ops
  def fromTimeGroup(lowerBound: LocalTime, upperBound: LocalTime): ODPairs =
    persons.flatMap(_.unpackTrips(lowerBound, upperBound)).toSeq
  def toODPairs: ODPairs = persons.toSeq.flatMap(p => p.legs.map(leg => SimpleMSSP_ODPair(p.id, leg.srcVertex, leg.dstVertex)))
}

case class HomeConfig(name: String)
case class ActivityConfig(name: String, start: LocalTime, dur: LocalTime, dev: Long = 0L)
case class ModeConfig(name: String, probability: Double = 1.0D)
case class RandomPopulationConfig(populationSize: Int, home: HomeConfig, activities: Seq[ActivityConfig], modes: Seq[ModeConfig])

object PopulationFactory {
  val Zero: Int = 0
  val random = new java.util.Random(System.currentTimeMillis)
  def setSeed(s: Long): Unit = random.setSeed(s)
  def generateSimpleRandomPopulation (network: xml.Elem, pSize: Int): Population = {
//    val activityLocations = ??? // ActivityLocation.takeAllLocations(network)
    val activityTimeGenerator =
      PopulationRandomTimeGenerator(
        Seq(
          ("home", NoDeviation(LocalTime.parse("09:00:00"))),
          ("work", NoDeviation(LocalTime.parse("15:00:00")))
        )
      )

    Population(
      (Zero until pSize).map(n => {
        val times = activityTimeGenerator.next()
//        val home = ActivityLocation.pickRandomLocation(activityLocations)
//        val work = ActivityLocation.pickRandomLocation(activityLocations)
        val home = ActivityLocation.takeRandomLocation(network)
        val work = ActivityLocation.takeRandomLocation(network)
        PersonNode(
          n.toString,
          "car",
          MorningActivity("home", home._2.x, home._2.y, home._1, home._3, EndTime(times("home"))),
          List(MiddayActivity("work", work._2.x, work._2.y, work._1, work._3, EndTime(times("work")))),
          EveningActivity("home", home._2.x, home._2.y, home._1, home._3)
        )
      }).toSet
    )
  }
  def generateRandomPopulation (network: xml.Elem, conf: RandomPopulationConfig): Population = {
    // @TODO: revise time generation. is currently passing start time and duration; we just use end time.
    // might be worth smarting up with case classes for types of time and different results from the time generator
    val activityTimeGenerator =
      PopulationRandomTimeGenerator(
        (conf.home.name, NoDeviation(conf.activities.head.start)) +:
        conf.activities.map(act => {
          (act.name, BidirectionalBoundedDeviation(act.start.plusSeconds(act.dur.toSecondOfDay), act.dev))
        })
      )

    Population(
      (Zero until conf.populationSize).map(n => {
        val times = activityTimeGenerator.next()
//        println("generated times")
//        println(times.mkString(" "))
//        val homeLocation = ActivityLocation.pickRandomLocation(activityLocations)
//        val actLocations = conf.activities.map(_ => ActivityLocation.pickRandomLocation(activityLocations))
        val homeLocation = ActivityLocation.takeRandomLocation(network)
        val actLocations = conf.activities.map(_ => ActivityLocation.takeRandomLocation(network))
        PersonNode(
          n.toString,
          conf.modes.filter(evaluateModeProbability).map(_.name).mkString(","),
          MorningActivity(conf.home.name, homeLocation._2.x, homeLocation._2.y, homeLocation._1, homeLocation._3, EndTime(times(conf.home.name))),
          conf.activities.zip(actLocations).map(act => {
            MiddayActivity(act._1.name, act._2._2.x, act._2._2.y, act._2._1, act._2._3, EndTime(times(act._1.name)))
          }).toList,
          EveningActivity(conf.home.name, homeLocation._2.x, homeLocation._2.y, homeLocation._1, homeLocation._3)
        )
      }).toSet
    )
  }
  private def evaluateModeProbability(modeConfig: ModeConfig): Boolean =
    random.nextDouble <= modeConfig.probability
}
