package cse.fitzgero.sorouting.model.roadnetwork.local

object TestAssets {
  trait LocalEdgeValidArgs1 {
    def id = "some ID"
    def src = "101"
    def dst = "505"
    def fixedFlow = Some(20D)
    def capacity = Some(100D)
    def freeFlowSpeed = Some(50D)
    def distance = Some(1000D)
    def t: LocalEdgeAttributeType = LocalEdgeAttributeBPR
  }
}
