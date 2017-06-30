package cse.fitzgero.sorouting.matsimrunner.population

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate
import cse.fitzgero.sorouting.roadnetwork.edge.EdgeIdType
import cse.fitzgero.sorouting.roadnetwork.vertex.Euclidian

class ActivityLocationTests extends SORoutingUnitTestTemplate {
  "ActivityLocation" when {
    // loads this cute little triangular graph into Spark, in scope with a test
    val xmlGraph: xml.Elem =
      <network>
        <links>
          <link id="100" from="1" to="2" freespeed="27.78" capacity="1000.0"/>
          <link id="101" from="2" to="3" freespeed="27.78" capacity="2000.0"/>
          <link id="102" from="3" to="1" freespeed="27.78" capacity="4000.0"/>
        </links>
        <nodes>
          <node id="1" x="-5" y="-5"/>
          <node id="2" x="5" y="5"/>
          <node id="3" x="0" y="10"/>
        </nodes>
      </network>

    "takeAllLocations" when {
      "called with a road network" should {
        "grab all edges and the positions from their source vertices and return an array of that information" in {
          ActivityLocation.setSeed(1L)
          val result: ActivityLocations = ActivityLocation.takeAllLocations(xmlGraph)
          result.foreach(location => {
            if (location._1 == "100") location._2 should equal (Euclidian(-5, -5))
            else if (location._1 == "101") location._2 should equal (Euclidian(5, 5))
            else if (location._1 == "102") location._2 should equal (Euclidian(0, 10))
            else fail()  // only those 3 edges should exist
          })
        }
      }
    }
    "pickRandomLocation" when {
      "called with an array of activity locations" should {
        "return one of them, randomly" in {
          ActivityLocation.setSeed(1L)
          val activityLocations: ActivityLocations = ActivityLocation.takeAllLocations(xmlGraph)
          val r1: (EdgeIdType, Euclidian) = ActivityLocation.pickRandomLocation(activityLocations)
          val r2: (EdgeIdType, Euclidian) = ActivityLocation.pickRandomLocation(activityLocations)
          val r3: (EdgeIdType, Euclidian) = ActivityLocation.pickRandomLocation(activityLocations)
          r1._1 should equal ("100")
          r2._1 should equal ("101")
          r3._1 should equal ("101")
        }
      }
    }
  }
}
