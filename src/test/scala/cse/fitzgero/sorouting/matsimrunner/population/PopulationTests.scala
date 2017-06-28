package cse.fitzgero.sorouting.matsimrunner.population

import cse.fitzgero.sorouting.FileWriteSideEffectTestTemplate

class PopulationTests extends FileWriteSideEffectTestTemplate("PopulationTests") {
  val filePath: String = testRootPath
  "Population" when {
    "scaffolding methods" when {
      ""
//      "Person" ignore {
//        ".apply()" should {
//          "produce a person xml element with all required defaults present" in {
//            val result: xml.Elem = PersonNode("1").toXml
//            result.attribute("id") should be (Some("1"))
//            val plans = result \ "plan"
//            plans.size should be (1)
//            val planChildElements = plans.head \ "_"
//            planChildElements(0).label should equal ("activity")
//            planChildElements(0).attribute("type") should equal ("home")
//            planChildElements(1).label should equal ("leg")
////            planChildElements(1).attribute("type") should equal ("home")
//            planChildElements(2).label should equal ("activity")
//            planChildElements(2).attribute("type") should equal ("work")
//            planChildElements(3).label should equal ("leg")
////            planChildElements(3).attribute("type") should equal ("home")
//            planChildElements(4).label should equal ("activity")
//            planChildElements(4).attribute("type") should equal ("home")
//
//          }
//        }
//      }
      "population constructor" when {
        "population of 100" should {
          "produce a population scaffolding with 100 people attributes with unique ids" in {

          }
        }
      }
    }
    "createPlacesList" when {
      "from a small network" should {
        "create a list of all edge ids mapped to the coordinates of their origin intersections" in {

        }
      }
    }
    "randomDistribution" when {
      "from a small network and small population" should {
        "return a population with home and work activities distributed across the network" in {

        }
      }
    }
    "selectSubset" when {
      "given a subsetPercentage Integer" should {
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
