package cse.fitzgero.sorouting.model.roadnetwork.costfunction

object TestAssets {
  trait ValidArgs {
    def fixedFlow = Some(10D)     // vehs
    def capacity = Some(100D)     // vehs
    def freeFlowSpeed = Some(50D) // ft per second
    def distance = Some(1000D)    // ft
  }
}
