import cse.fitzgero.sorouting.roadnetwork.costfunction._

val f1: BPRCostFunction = BPRCostFunction(Map("freespeed" -> "50", "capacity" -> "100"))
val f2: BPRCostFunction = BPRCostFunction(Map("freespeed" -> "40", "capacity" -> "100"))
val f3: BPRCostFunction = BPRCostFunction(Map("freespeed" -> "1", "capacity" -> "1"))

f1.costFlow(0D)
f1.costFlow(100D)
f1.costFlow(200D)

f2.costFlow(0D)
f2.costFlow(100D)
f2.costFlow(206D)

f3.costFlow(1D)