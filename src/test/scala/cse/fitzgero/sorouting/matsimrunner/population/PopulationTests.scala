package cse.fitzgero.sorouting.matsimrunner.population

import java.time.LocalTime

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate
import cse.fitzgero.sorouting.algorithm.mssp.graphx.simplemssp.{ODPairs, SimpleMSSP_ODPath}
import cse.fitzgero.sorouting.roadnetwork.edge.EdgeIdType
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
          val result = RandomSampling(pop.persons)
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
          val (src, dst) = (personToUpdate.legs.head.source, personToUpdate.legs.head.destination)
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
      "given a population and a filename which contains a snapshot range" should {
        "return a collection of origin-destination pairs" in {
          val popSize = 100
          val network = XML.loadFile(equilNetworkFile)
          val pop = PopulationFactory.generateSimpleRandomPopulation(network, popSize)
          val result: ODPairs = pop.fromTimeGroup(LocalTime.parse("06:00:00"), LocalTime.parse("10:00:00"))
          result.foreach(println)
        }
      }
    }
  }
}
