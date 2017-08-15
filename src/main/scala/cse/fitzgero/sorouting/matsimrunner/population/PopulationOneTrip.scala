package cse.fitzgero.sorouting.matsimrunner.population

import java.time.LocalTime

import cse.fitzgero.sorouting.algorithm.pathsearch.mssp.graphx.simplemssp.{ODPairs, SimpleMSSP_ODPath}
import cse.fitzgero.sorouting.algorithm.pathsearch.od.localgraph.{LocalGraphODPair, LocalGraphODPath}

import scala.xml.XML
import scala.xml.dtd.{DocType, SystemID}



case class PopulationOneTrip (persons: Set[PersonOneTripNode], seed: Long = System.currentTimeMillis) extends ConvertsToXml {
  // random values
  implicit val sampling = PopulationOneTrip.RandomSampling
  sampling.setSeed(seed)


  // xml save operation
  val populationDocType = DocType("population", SystemID("http://www.matsim.org/files/dtd/population_v6.dtd"), Nil)
  val WriteXmlDeclaration = true
  def toXml: xml.Elem = <population>{persons.map(_.toXml)}</population>
  def saveFile(fileName: String): Unit = XML.save(fileName, this.toXml, "UTF-8", WriteXmlDeclaration, populationDocType)

  // population operations
  def subsetPartition(percentage: Double): (PopulationOneTrip, PopulationOneTrip) = {
    val numSampled = (percentage * persons.size).toInt
    val thisSampling: Set[PersonOneTripNode] = sampling(persons).take(numSampled).toSet
    (PopulationOneTrip(thisSampling), PopulationOneTrip(persons -- thisSampling))
  }

  def reintegrateSubset(subset: PopulationOneTrip): PopulationOneTrip = {
    PopulationOneTrip(
      subset.persons.foldLeft(this.persons)((accum, updatedPerson) => {
        val toRemove = this.persons.find(_.id == updatedPerson.id)
        (accum -- toRemove) + updatedPerson
      }),
      seed
    )
  }

  def updatePerson(data: LocalGraphODPath): PopulationOneTrip = {
    persons.find(_.id == data.personId) match {
      case None => this
      case Some(person) =>
        val updatedPerson: PersonOneTripNode = person.updatePath(data.path)
        PopulationOneTrip((persons - person) + updatedPerson)
    }
  }

  def updatePerson(data: SimpleMSSP_ODPath): PopulationOneTrip = {
    persons.find(_.id == data.personId) match {
      case None => this
      case Some(person) =>
        val updatedPerson: PersonOneTripNode = person.updatePath(data.path)
        PopulationOneTrip((persons - person) + updatedPerson)
    }
  }

  // export ops
  def exportTimeGroupAsODPairs(lowerBound: LocalTime, upperBound: LocalTime): Seq[LocalGraphODPair] =
    persons.filter(_.activityInTimeGroup(lowerBound, upperBound)).map(_.toLocalGraphODPair).toSeq

  def toODPairs: Set[LocalGraphODPair] = persons.map(_.toLocalGraphODPair)


}

object PopulationOneTrip {

  sealed trait SamplingMethod {
    def apply(population: Set[PersonOneTripNode]): Stream[PersonOneTripNode]
  }
  object RandomSampling extends SamplingMethod {
    val random = new java.util.Random(System.currentTimeMillis)
    def setSeed(s: Long): Unit = random.setSeed(s)
    def apply(population: Set[PersonOneTripNode]): Stream[PersonOneTripNode] = {
      if (population.isEmpty) throw new IndexOutOfBoundsException("attempting to sample from empty set")
      else {
        val randIndex = random.nextInt(population.size)
        val nextPerson = population.iterator.drop(randIndex).next
        nextPerson #:: this(population - nextPerson)
      }
    }
  }

  val Zero: Int = 0
  val random = new java.util.Random(System.currentTimeMillis)
  def setSeed(s: Long): Unit = random.setSeed(s)

  def generateRandomOneTripPopulation (network: xml.Elem, conf: RandomPopulationOneTripConfig): PopulationOneTrip = {
    // @TODO: revise time generation. is currently passing start time and duration; we just use end time.
    // might be worth smarting up with case classes for types of time and different results from the time generator
    val activityTimeGenerator =
    PopulationRandomTimeGenerator(
      conf.activities.map(act => {
        (act.name, BidirectionalBoundedDeviation(act.start.plusSeconds(act.dur.toSecondOfDay), act.dev))
      })
    )

    PopulationOneTrip(
      (Zero until conf.populationSize).flatMap(n => {
        val times = activityTimeGenerator.next()
        val numLegs = conf.activities.size
        val idRange: Iterator[Int] = (n * numLegs until (n * numLegs) + numLegs).iterator
        val actLocations = conf.activities.map(_ => ActivityLocation.takeRandomLocation(network))
        val allActivities = conf.activities.zip(actLocations).sliding(2)

        allActivities.zip(idRange).map(actTuple => {
          val a1Tup = actTuple._1(0)
          val a2Tup = actTuple._1(1)
          val act1 = MiddayActivity(a1Tup._1.name, a1Tup._2._2.x, a1Tup._2._2.y, a1Tup._2._1, a1Tup._2._3, EndTime(times(a1Tup._1.name)))
          val act2 = MiddayActivity(a2Tup._1.name, a2Tup._2._2.x, a2Tup._2._2.y, a2Tup._2._1, a2Tup._2._3, EndTime(times(a2Tup._1.name)))
          val mode = conf.modes.filter(evaluateModeProbability).map(_.name).mkString(",")

          PersonOneTripNode(
            actTuple._2.toString,
            mode,
            act1,
            act2,
            PersonOneTripNode.generateLeg(mode, act1, act2)
          )
        })
      }).toSet
    )
  }
  private def evaluateModeProbability(modeConfig: ModeConfig): Boolean =
    random.nextDouble <= modeConfig.probability
}