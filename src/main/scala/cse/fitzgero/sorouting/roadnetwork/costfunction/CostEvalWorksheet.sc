import cse.fitzgero.sorouting.roadnetwork.costfunction._

val f1 = BPRCostFunction(Map("freespeed" -> "50", "capacity" -> "100")).generate
val f2 = BPRCostFunction(Map("freespeed" -> "40", "capacity" -> "100")).generate

f1(0)
f1(100)
f1(200)

f2(0)
f2(100)
f2(206)