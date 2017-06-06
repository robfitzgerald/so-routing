//package cse.fitzgero.sorouting.matsimrunner
//
//import cse.fitzgero.sorouting.SORoutingUnitTests
//import org.matsim.api.core.v01.Id
//
//class TimeTrackerTests extends SORoutingUnitTests {
//  "TimeTracker" when {
//    "constructed" should {
//      "take parameters for time batch tracking" in {
//        val timeTracker = TimeTracker(5, "06:00", "12:00")
//        timeTracker shouldBe a [TimeTracker]
//      }
//    }
//    "belongsToThisTimeGroup" should {
//      "return true when events fall within the current time delta" in {
//        val v = Id.createVehicleId(1)
//        val l = Id.createLinkId(1)
//        val e: SnapshotEventData = LinkEnterData(3600, l, v)
//        val timeTracker = TimeTracker(5, "01:00", "12:00")
//
//        timeTracker.belongsToThisTimeGroup(e) should equal (true)
//      }
//      "return false when events fall outside the current time delta" in {
//        val v = Id.createVehicleId(1)
//        val l = Id.createLinkId(1)
//        val e: SnapshotEventData = LinkEnterData(3600, l, v)
//        val timeTracker = TimeTracker(5, "00:30", "12:00")
//
//        timeTracker.belongsToThisTimeGroup(e) should equal (false)  // current time group is "1800"
//      }
//    }
//    "currentTimeGroup" should {
//      "report the current time group in seconds" in {
//        val timeTracker = TimeTracker(5, "06:00", "12:00")
//        timeTracker.currentTimeGroup should equal (21600)
//      }
//    }
//    "currentTimeString" should {
//      "report the current time group as a string" in {
//        val timeTracker = TimeTracker(5, "06:00", "12:00")
//        timeTracker.currentTimeString should equal ("06:00")
//      }
//    }
//    "advance" should {
//      "move time forward by one time delta" in {
//        val timeTracker = TimeTracker(5, "06:00", "12:00")
//        timeTracker.advance.currentTimeGroup should equal(21605)
//      }
//    }
//    "isDone" should {
//      "identify that there are no more time windows to report" in {
//        val timeTracker = TimeTracker(5, "06:00", "6:01")
//        timeTracker.advance.isDone should equal (true)
//      }
//      "return false when the current time delta is within the start and end time" in {
//        val timeTracker = TimeTracker(5, "06:00", "6:10")
//        timeTracker.isDone should equal (false)
//        timeTracker.advance.isDone should equal (false)
//      }
//    }
//  }
//}
