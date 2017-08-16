package cse.fitzgero.sorouting.matsimrunner.population

import java.time.LocalTime

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate
import cse.fitzgero.sorouting.algorithm.pathsearch.mssp.graphx.simplemssp.SimpleMSSP_ODPath
import cse.fitzgero.sorouting.algorithm.pathsearch.od.localgraph.LocalGraphODPath
import cse.fitzgero.sorouting.roadnetwork.graphx.edge.EdgeIdType

import scala.xml.XML

class PopulationOneTripTests extends SORoutingUnitTestTemplate {
  val equilNetworkFile: String = "src/test/resources/PopulationTests/network.xml"
  "PopulationOneTrip" when {
    def config(pop: Int) =
      RandomPopulationOneTripConfig(
        pop,
        Seq(
          ActivityConfig(
            "home",
            LocalTime.parse("00:00:00"),
            LocalTime.parse("08:00:00"),
            30L),
          ActivityConfig(
            "work",
            LocalTime.parse("09:00:00"),
            LocalTime.parse("17:00:00"),
            30L),
          ActivityConfig(
            "home",
            LocalTime.parse("18:00:00"),
            LocalTime.parse("23:00:00"),
            30L)
        ),
        Seq(ModeConfig("car"))
      )
    "generateRandomOneTripPopulation" when {
      "called with a network, asking for 1000 people with 3 activities" should {
        "generate a population for 1000 people * 2 trip legs = 2000 entities" in {
          val network = XML.loadFile(equilNetworkFile)
          val result = PopulationOneTrip.generateRandomOneTripPopulation(network, config(1000)).toXml
          (result \ "person").size should be (1000 * 2)
//          result.take(20).foreach(println)
        }
      }
    }
    "subsetPartition" when {
      "given a <population/> and a subsetPercentage Integer" should {
        "select a random subset of the population which is of size subsetPercentage * population.size" in {
          val network = XML.loadFile(equilNetworkFile)
          val pop = PopulationOneTrip.generateRandomOneTripPopulation(network, config(1000))
          val (subset, remaining) = pop.subsetPartition(0.20)
          subset.persons.size should equal (200 * 2)
          remaining.persons.size should equal (800 * 2)
        }
      }
    }
    "reintegrateSubset" when {
      "given the original population and a subset of that population with different data" should {
        "replace the old data with the new and return the complete set" in {
          val network = XML.loadFile(equilNetworkFile)
          val pop = PopulationOneTrip.generateRandomOneTripPopulation(network, config(1000))
          val (subset, remaining) = pop.subsetPartition(0.20)
          val updated = PopulationOneTrip(subset.persons.map(_.copy(mode = "updated")))
          val result = pop.reintegrateSubset(updated)
          result.persons.count(_.mode == "updated") should equal (200 * 2)
          result.persons.size should equal (1000 * 2)
        }
      }
    }
    "exportTimeGroupAsODPairs" when {
      "given a population and all possible time ranges" should {
        "return all 100 trips for this population" in {
          val popSize = 100
          val network = XML.loadFile(equilNetworkFile)
          val pop = PopulationOneTrip.generateRandomOneTripPopulation(network, config(popSize / 2))
          val allTimesInRange = (
            LocalTime.parse("00:00:00").toSecondOfDay to
            LocalTime.parse("23:59:59").toSecondOfDay by 600
            ).map(timeNumber => LocalTime.ofSecondOfDay(timeNumber))

          val result = allTimesInRange.sliding(2).flatMap(bounds => {
            pop.exportTimeGroupAsODPairs(bounds(0), bounds(1))
          }).toList

          result.size should equal (popSize)
          pop.persons.foreach(person => {
            result.find(r => r.personId.toString == person.id.toString) match {
              case Some(p) => // good
              case None =>
                println(s"couldn't find ${person.id}")
                fail()
            }
          })
        }
      }
    }
    "updatePerson" when {
      "given data associated with a person" should {
        "add the path information to the relevant person's relevant path leg" in {
          val popSize = 1
          val network = XML.loadFile(equilNetworkFile)
          val pop = PopulationOneTrip.generateRandomOneTripPopulation(network, config(10))
          val personId = pop.persons.head.id.toString
          val personSrc = pop.persons.head.act1.vertex
          val personDst = pop.persons.head.act2.vertex
          val (path, cost) = (List("1","4","9"), List(5D, 5D, 5D))
          val data = LocalGraphODPath(personId, personSrc, personDst, path, cost)
          val result = pop.updatePerson(data)

          result.persons.find(_.id.toString == personId) match {
            case Some(person) => person.leg.path.mkString(" ") should equal ("1 4 9")
            case _ => fail()
          }
        }
      }
    }
  }
}
