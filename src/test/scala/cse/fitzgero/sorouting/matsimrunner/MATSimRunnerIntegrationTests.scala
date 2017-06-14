package cse.fitzgero.sorouting.matsimrunner

import java.io.{File, PrintWriter}

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate
import cse.fitzgero.sorouting.matsimrunner.snapshot._
import org.matsim.api.core.v01.Id
import org.matsim.api.core.v01.network._
import org.matsim.vehicles.Vehicle

/**
  * Created by robertfitzgerald on 6/7/17.
  */
class MATSimRunnerIntegrationTests extends SORoutingUnitTestTemplate {
  "MATSimRunner" when {
    "run with a simple mock set of data events" should {
      "run correctly" in {
        case class MockConfig(window: Int, startTime: Int, endTime: Int)
        val appConfig = MockConfig(5, 0, 20)
        val links: Seq[Id[Link]] = for (l <- 0 until 10) yield Id.createLinkId(l)
        val vehicles: Seq[Id[Vehicle]] = for (v <- 0 until 10) yield Id.createVehicleId(v)
        val events: List[SnapshotEventData] = List(
          LinkEnterData(0, links(0), vehicles(0)),
          LinkEnterData(2, links(2), vehicles(1)),
          LinkLeaveData(6, links(0), vehicles(0)),
          LinkEnterData(6, links(1), vehicles(0)),
          LinkLeaveData(7, links(2), vehicles(1)),
          LinkEnterData(7, links(1), vehicles(1)),
          LinkLeaveData(11, links(1), vehicles(0)),
          LinkEnterData(11, links(2), vehicles(0)),
          LinkLeaveData(14, links(1), vehicles(1)),
          LinkEnterData(14, links(0), vehicles(1)),
          LinkLeaveData(17, links(2), vehicles(0)),
          LinkLeaveData(20, links(0), vehicles(1)),
          NewIteration(2)
        )
        var currentNetworkState: NetworkStateCollector = NetworkStateCollector()
        var currentIteration: Int = 1
        var timeTracker: TimeTracker = TimeTracker(appConfig.window, appConfig.startTime, appConfig.endTime)

        // MATSimRunner Logic
        events.foreach({
          case LinkEventData(e) =>
            if (timeTracker.belongsToThisTimeGroup(e))
              currentNetworkState = currentNetworkState.update(e)
            else {
              println(s"current network state at time step ${e.time}")
              println(s"${currentNetworkState.toString}")

              timeTracker = timeTracker.advance
              currentNetworkState = currentNetworkState.update(e)
            }

          case NewIteration(i) => {
            println(s"start new iteration $i")

            // TODO: make sure everything's written

            // start over
            timeTracker = TimeTracker(appConfig.window, appConfig.startTime, appConfig.endTime)
            currentNetworkState = NetworkStateCollector()
            currentIteration = i
            // TODO: open new directory
          }
        })
        currentIteration should equal (2)
        currentNetworkState.toString should equal ("")

      }
    }
  }
}
