package cse.fitzgero.sorouting.matsimrunner.population

import java.time.LocalTime

import scala.xml.XML
import scala.xml.dtd.{DocType, SystemID}
import cse.fitzgero.sorouting.algorithm.pathsearch.mssp.graphx.simplemssp.{ODPairs, SimpleMSSP_ODPath}
import cse.fitzgero.sorouting.algorithm.pathsearch.od.localgraph.{LocalGraphODPairByEdge, LocalGraphODPairByVertex, LocalGraphODPath}
import cse.fitzgero.sorouting.util.convenience._


// TODO finish Population overhaul
// we rushed this the first time through
// breaking this up better should lead to simpler code anyway
// finish that, toward the plan to remove the older version
// think about role of inheritance or collection-oriented functional style


case class PopulationOneTrip (persons: Set[PersonOneTrip], seed: Long = System.currentTimeMillis) extends Population with ConvertsToXml {
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
    val thisSampling: Set[PersonOneTrip] = sampling(persons).take(numSampled).toSet
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
    val toUpdate = persons.find(_.id.toString == data.personId)
    toUpdate match {
      case None => this
      case Some(person) =>
        val updatedPerson: PersonOneTrip = person.updatePath(data.path)
        PopulationOneTrip((persons - person) + updatedPerson)
    }
  }

  def updatePerson(data: SimpleMSSP_ODPath): PopulationOneTrip = {
    val toUpdate = persons.find(_.id.toString == data.personId)
    toUpdate match {
      case None => this
      case Some(person) =>
        val updatedPerson: PersonOneTrip = person.updatePath(data.path)
        PopulationOneTrip((persons - person) + updatedPerson)
    }
  }

  // export ops
  def exportTimeGroupAsODPairsByVertex(lowerBound: LocalTime, upperBound: LocalTime): Seq[LocalGraphODPairByVertex] =
    persons.filter(_.activityInTimeGroup(lowerBound, upperBound)).map(_.toLocalGraphODPairByVertex).toSeq

  def exportTimeGroupAsODPairsByEdge(lowerBound: LocalTime, upperBound: LocalTime): Seq[LocalGraphODPairByEdge] =
    persons.filter(_.activityInTimeGroup(lowerBound, upperBound)).map(_.toLocalGraphODPairByEdge).toSeq

  def exportTimeGroup(lowerBound: LocalTime, upperBound: LocalTime): PopulationOneTrip =
    PopulationOneTrip(persons.filter(_.activityInTimeGroup(lowerBound, upperBound)))

  def exportAsODPairsByVertex: Seq[LocalGraphODPairByVertex] =
    persons.map(_.toLocalGraphODPairByVertex).toSeq

  def exportAsODPairsByEdge: Seq[LocalGraphODPairByEdge] =
    persons.map(_.toLocalGraphODPairByEdge).toSeq

  def toODPairs: Set[LocalGraphODPairByVertex] = persons.map(_.toLocalGraphODPairByVertex)

  def size: Int = persons.size

}

case class ActivityConfig2(name: String, endTime: LocalTime = LocalTime.MIDNIGHT, dev: Long = 0L minutesDeviation)
case class RandomPopulationOneTripConfig(populationSize: Int, activities: Seq[ActivityConfig2], modes: Seq[ModeConfig])

object PopulationOneTrip {

  sealed trait SamplingMethod {
    def apply(population: Set[PersonOneTrip]): Stream[PersonOneTrip]
  }
  object RandomSampling extends SamplingMethod {
    val random = new java.util.Random(System.currentTimeMillis)
    def setSeed(s: Long): Unit = random.setSeed(s)
    def apply(population: Set[PersonOneTrip]): Stream[PersonOneTrip] = {
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
    val activityTimeGenerator = PopulationRandomTimeGenerator2(conf.activities)

    PopulationOneTrip(
      (Zero until conf.populationSize).flatMap(n => {
        val times = activityTimeGenerator.next()
        val numLegs = conf.activities.size
//        val idRange: Iterator[Int] = (n * numLegs until (n * numLegs) + numLegs).iterator
        val idRange: Iterator[Int] = (0 until numLegs).iterator
        val actLocations = conf.activities.map(_ => ActivityLocation.takeRandomLocation(network))
        val allActivities = conf.activities.zip(actLocations).sliding(2)

        allActivities.zip(idRange).map(actTuple => {
          val personID = CombinedPersonID(n.toString, actTuple._2.toString)
          val a1Tup = actTuple._1(0)
          val a2Tup = actTuple._1(1)

          val act1 = MiddayActivity(a1Tup._1.name, a1Tup._2._2.x, a1Tup._2._2.y, a1Tup._2._1, a1Tup._2._3, EndTime(times(a1Tup._1.name)))

          val isLastActivity = actTuple._2 == numLegs - 1
          val act2 =
            if (isLastActivity)
              EveningActivity(a2Tup._1.name, a2Tup._2._2.x, a2Tup._2._2.y, a2Tup._2._1, a2Tup._2._3)
            else
              MiddayActivity(a2Tup._1.name, a2Tup._2._2.x, a2Tup._2._2.y, a2Tup._2._1, a2Tup._2._3, EndTime(times(a2Tup._1.name)))
          val mode = conf.modes.filter(evaluateModeProbability).map(_.name).mkString(",")

          PersonOneTrip(
            personID,
            mode,
            act1,
            act2
          )
        })
      }).toSet
    )
  }
  private def evaluateModeProbability(modeConfig: ModeConfig): Boolean =
    random.nextDouble <= modeConfig.probability
}