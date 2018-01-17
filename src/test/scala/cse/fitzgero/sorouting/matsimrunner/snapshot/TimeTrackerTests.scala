package cse.fitzgero.sorouting.matsimrunner.snapshot

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate
import org.matsim.api.core.v01.Id

class TimeTrackerTests extends SORoutingUnitTestTemplate {
  "TimeTracker" when {
    "constructed" should {
      "take parameters for time batch tracking" in {
        val timeTracker = TimeTracker(5, "06:00:00", "12:00:00")
        timeTracker shouldBe a [TimeTracker]
      }
    }
    "belongsToThisTimeGroup" should {
      "return true when events fall within the current time delta" in {
        val v = Id.createVehicleId(1)
        val l = Id.createLinkId(1)
        val e: LinkEnterData = LinkEnterData(3600, l, v)
        val timeTracker = TimeTracker(5, "01:00:00", "12:00:00")

        timeTracker.belongsToThisTimeGroup(e) should equal (true)
      }
      "return false when events fall outside the current time delta" in {
        val v = Id.createVehicleId(1)
        val l = Id.createLinkId(1)
        val e: LinkEnterData = LinkEnterData(3600, l, v)
        val timeTracker = TimeTracker(5, "00:30:00", "12:00:00")

        timeTracker.belongsToThisTimeGroup(e) should equal (false)  // current time group is "1800"
      }
    }
    "currentTimeGroup" should {
      "report the current time group in seconds" in {
        val timeTracker = TimeTracker(5, "06:00:00", "12:00:00")
        timeTracker.currentTimeGroup should equal (21600)

      }
    }
    "currentTimeString" should {
      "report the current time group as a string" in {
        val timeTracker = TimeTracker(5, "06:00:00", "12:00:00")
        timeTracker.currentTimeString should equal ("06:00:00")
      }
    }
    "advance" should {
      "move time forward by one time delta" in {
        val timeTracker = TimeTracker(5, "06:00:00", "12:00:00")
        timeTracker.advance.currentTimeGroup should equal(21605)
      }
    }
    "isDone" should {
      "identify that there are no more time windows to report" in {
        val timeTrackerSS = TimeTracker(5, "06:00:00", "06:00:05")
        timeTrackerSS.advance.isDone should equal (true)
        val timeTrackerMM = TimeTracker(600, "06:00:00", "06:10:00")
        timeTrackerMM.advance.isDone should equal (true)
        val timeTrackerHH = TimeTracker(3600, "06:00:00", "07:00:00")
        timeTrackerHH.advance.isDone should equal (true)
      }
      "return false when the current time delta is within the start and end time" in {
        val timeTrackerSS = TimeTracker(5, "06:00:00", "6:00:06")
        timeTrackerSS.isDone should equal (false)
        timeTrackerSS.advance.isDone should equal (false)
        val timeTrackerMM = TimeTracker(600, "06:00:00", "6:10:01")
        timeTrackerMM.isDone should equal (false)
        timeTrackerMM.advance.isDone should equal (false)
        val timeTrackerHH = TimeTracker(3600, "06:00:00", "7:00:01")
        timeTrackerHH.isDone should equal (false)
        timeTrackerHH.advance.isDone should equal (false)
      }
    }
  }
}
