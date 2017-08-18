package cse.fitzgero.sorouting.matsimrunner.population

import java.time.LocalTime

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate
import cse.fitzgero.sorouting.algorithm.pathsearch.mssp.graphx.simplemssp.{ODPairs, SimpleMSSP_ODPath}
import cse.fitzgero.sorouting.roadnetwork.graphx.edge.EdgeIdType
import org.apache.spark.graphx.VertexId

import scala.xml.XML

class PopulationTests extends SORoutingUnitTestTemplate {
  val equilNetworkFile: String = "src/test/resources/PopulationTests/network.xml"
  "Population" when {
    "generateSimpleRandomPopulation" when {
      "called with a network, asking for 100 people" should {
        "generate a set of coordinates and nearest link for each location, as well as time data" in {
          val network = XML.loadFile(equilNetworkFile)
          val result = PopulationFactory.generateSimpleRandomPopulation(network, 100).toXml
          (result \ "person").size should be (100)
        }
      }
      "called with a network, asking for 10000 people" should {
        "generate a set of coordinates and nearest link for each location, as well as time data" in {
          val network = XML.loadFile(equilNetworkFile)
          val result = PopulationFactory.generateSimpleRandomPopulation(network, 10000).toXml
          (result \ "person").size should be (10000)
        }
      }
      "called with a network, asking for 100000 people" should {
        "generate a set of coordinates and nearest link for each location, as well as time data" in {
          val network = XML.loadFile(equilNetworkFile)
          val result = PopulationFactory.generateSimpleRandomPopulation(network, 100000).toXml
          (result \ "person").size should be (100000)
        }
      }
    }
    "generateRandomPopulation" when {
      "called with some basic config with one activity" should {
        "generates a population" in {
          val network = XML.loadFile(equilNetworkFile)
          val config =
            RandomPopulationConfig(
              1000,
              HomeConfig("home"),
              Seq(
                ActivityConfig(
                  "work",
                  LocalTime.parse("09:00:00"),
                  LocalTime.parse("08:00:00"),
                  30L)
              ),
              Seq(ModeConfig("car"))
            )
          val result = PopulationFactory.generateRandomPopulation(network, config).toXml
          (result \ "person").size should be (1000)
//          result.foreach(println)
        }
      }
    }
    "RandomSampling" when {
      "passed a population" should {
        "randomly sample from that population when streamed" in {
          val network = XML.loadFile(equilNetworkFile)
          val pop = PopulationFactory.generateSimpleRandomPopulation(network, 100)
          val result = Population.RandomSampling(pop.persons)
          result.take(10).foreach(println)
        }
      }
    }
    "subsetPartition" when {
      "given a <population/> and a subsetPercentage Integer" should {
        "select a random subset of the population which is of size subsetPercentage * population.size" in {
          val network = XML.loadFile(equilNetworkFile)
          val pop = PopulationFactory.generateSimpleRandomPopulation(network, 100)
          val (subset, remaining) = pop.subsetPartition(0.20)
          subset.persons.size should equal (20)
          remaining.persons.size should equal (80)
        }
      }
    }
    "reintegrateSubset" when {
      "given the original population and a subset of that population with different data" should {
        "replace the old data with the new and return the complete set" in {
          val network = XML.loadFile(equilNetworkFile)
          val pop = PopulationFactory.generateSimpleRandomPopulation(network, 100)
          val (subset, remaining) = pop.subsetPartition(0.20)
          val updated = Population(subset.persons.map(_.copy(mode = "updated")))
          val result = pop.reintegrateSubset(updated)
          result.persons.count(_.mode == "updated") should equal (20)
          result.persons.size should equal (100)
        }
      }
    }
    "injectPersonActivityData" when {
      "given a population, a person id, and a path" should {
        "inject the list of ids for that activity into the person and return the whole population" in {
          val updatedPersonId = "15"
          val network = XML.loadFile(equilNetworkFile)
          val pop = PopulationFactory.generateSimpleRandomPopulation(network, 100)

          val personToUpdate = pop.persons.find(_.id == updatedPersonId).get
          val (src, dst) = (personToUpdate.legs.head.srcVertex, personToUpdate.legs.head.dstVertex)
          val newPath: List[EdgeIdType] = List("3", "4", "1234567890" , "5")
          val newODPath: SimpleMSSP_ODPath = SimpleMSSP_ODPath(updatedPersonId, src, dst, newPath)

          val newPop = pop.updatePerson(newODPath)
          val result = newPop.persons.find(_.id == updatedPersonId).get

          result.legs.head.path.mkString(" ") should equal ("3 4 1234567890 5")
        }
      }
    }
    "injectUEStartTime" when {
      "given a population and a UE plans.xml simulation result" should {
        "inject the start time for the selected plans used in the UE sim, for each person" in {

        }
      }
    }
    "castAsODPairs" when {
      "given a population (subset)" should {
        "group the population by the starttime value (w/ duplicates)" in {
          val popSize = 100
          val network = XML.loadFile(equilNetworkFile)
          val pop: Population = PopulationFactory.generateSimpleRandomPopulation(network, popSize)
          Population(pop.persons.map(p => p.copy(legsParam = p.legsParam.map(_.copy(path = List("1", "2", "3"))))))
          pop.persons.size should be (popSize)

          val result = pop.toODPairs

          // two legs, before and after activity, for each person
          result.size should be (popSize * 2)
          // should be exactly two of each id
          result.map(_.personId.toInt).sum should equal ((0 until 100 map(_ * 2) ).sum)
          // @todo find more tests of this result
        }
      }
    }
    "fromTimeGroup" when {
      "given a population and all possible time ranges" should {
        "return all 200 trips for this population" in {
          val popSize = 100
          val network = XML.loadFile(equilNetworkFile)
          val pop = PopulationFactory.generateSimpleRandomPopulation(network, popSize)
          val allTimesInRange = (
            LocalTime.parse("00:00:00").toSecondOfDay to
            LocalTime.parse("23:59:59").toSecondOfDay by 30
            ).map(timeNumber => LocalTime.ofSecondOfDay(timeNumber))
          val result = allTimesInRange.sliding(2).map(bounds => {
            pop.exportTimeGroupAsODPairs(bounds(0), bounds(1))
          })
          result.flatten.size should be (popSize * 2)
        }
      }
    }
    "updatePerson" when {
      "given data associated with a person" should {
        "add the path information to the relevant person's relevant path leg" in {
          val popSize = 1
          val network = XML.loadFile(equilNetworkFile)
          val pop = PopulationFactory.generateSimpleRandomPopulation(network, popSize)
          val personId = pop.persons.head.id
          val personSrc = pop.persons.head.homeAM.vertex
          val personDst = pop.persons.head.work.head.vertex
          val path = List("1","4","9")
          val data = SimpleMSSP_ODPath(personId, personSrc, personDst, path)
          val result = pop.updatePerson(data)
          result.persons.head.legs.head.path.mkString(" ") should equal ("1 4 9")
        }
      }
    }
  }
}
