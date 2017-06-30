package cse.fitzgero.sorouting.matsimrunner.population

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate
import scala.xml.XML

class PopulationTests extends SORoutingUnitTestTemplate {
  val equilNetworkFile: String = "src/test/resources/PopulationTests/network.xml"
  "Population" when {
    "generateSimpleRandomPopulation" when {
      "called with a network, asking for 100 people" should {
        "generate a set of coordinates and nearest link for each location, as well as time data" in {
          val network = XML.loadFile(equilNetworkFile)
          val result = PopulationFactory.generateSimpleRandomPopulation(network, 100)
          (result \ "person").size should be (100)
        }
      }
      "called with a network, asking for 10000 people" should {
        "generate a set of coordinates and nearest link for each location, as well as time data" in {
          val network = XML.loadFile(equilNetworkFile)
          val result = PopulationFactory.generateSimpleRandomPopulation(network, 10000)
          (result \ "person").size should be (10000)
        }
      }
      "called with a network, asking for 100000 people" should {
        "generate a set of coordinates and nearest link for each location, as well as time data" in {
          val network = XML.loadFile(equilNetworkFile)
          val result = PopulationFactory.generateSimpleRandomPopulation(network, 100000)
          (result \ "person").size should be (100000)
        }
      }
      "called with a network, asking for 500000 people" should {
        "generate a set of coordinates and nearest link for each location, as well as time data" in {
          val network = XML.loadFile(equilNetworkFile)
          val result = PopulationFactory.generateSimpleRandomPopulation(network, 500000)
          (result \ "person").size should be (500000)
        }
      }
    }
    "simple io utilities" when {

    }
    "generateRandomPopulation" when {
      "called with a network and parameters for the resulting population" should {
        "randomly vary the times via a gaussian distribution" in {

        }
      }
    }
    "selectSubset" when {
      "given a <population/> and a subsetPercentage Integer" should {
        "select a random subset of the population which is of size subsetPercentage * population.size" in {

        }
      }
    }
    "replaceSubset" when {
      "given the original population and a subset of that population with different data" should {
        "replace the old data with the new and return the complete set" in {

        }
      }
    }
    "injectPersonActivityData" when {
      "given a population, a person id, an activity name, and a path" should {
        "inject the list of ids for that activity into the person and return the whole population" in {

        }
      }
    }
    "injectUEStartTime" when {
      "given a population and a UE plans.xml simulation result" should {
        "inject the start time for the selected plans used in the UE sim, for each person" in {

        }
      }
    }
    "groupByTimeGroup" when {
      "given a population (subset)" should {
        "group the population by activity(home, work) and then by the injected starttime value (w/ duplicates)" in {

        }
      }
    }
  }
}
