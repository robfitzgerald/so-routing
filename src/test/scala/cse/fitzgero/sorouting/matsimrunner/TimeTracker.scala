package cse.fitzgero.sorouting.matsimrunner

class TimeTracker (val windowDuration: Int, startTime: String, endTime: String) {

}

object TimeTracker {
  def apply(wD: Int, sT: String, eT: String): TimeTracker = new TimeTracker(wD, sT, eT)
}