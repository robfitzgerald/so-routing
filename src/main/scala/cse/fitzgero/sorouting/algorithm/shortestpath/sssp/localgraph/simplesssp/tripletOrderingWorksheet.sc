import cse.fitzgero.sorouting.roadnetwork.costfunction.{BPRCostFunction, CostFunctionAttributes, TestCostFunction}
import cse.fitzgero.sorouting.roadnetwork.edge.MacroscopicEdgeProperty
import cse.fitzgero.sorouting.roadnetwork.localgraph.{EdgeId, EdgeMATSim, LocalGraphMATSim, LocalGraphMATSimFactory}

val networkFilePath: String =   "/Users/robertfitzgerald/dev/ucd/phd/projects/2017su/SO-Routing/src/test/resources/LocalGraphFrankWolfe2Tests/network.xml"
val snapshotFilePath: String =   "/Users/robertfitzgerald/dev/ucd/phd/projects/2017su/SO-Routing/src/test/resources/LocalGraphFrankWolfe2Tests/snapshot.xml"
val graph: LocalGraphMATSim = LocalGraphMATSimFactory(BPRCostFunction, 10).fromFileAndSnapshot(networkFilePath, snapshotFilePath).get

//val costFuntionAttributes = 
//  CostFunctionAttributes(
//    capacity = 2000,
//    freespeed = 30,
//    algorithmFlowRate = 60
//  )
//val costFunction = BPRCostFunction(costFuntionAttributes)
//
//val edgeList: Map[EdgeId, EdgeMATSim] = (1 to 3).map(
//  (n: Int) => {
//    n.toLong -> MacroscopicEdgeProperty[EdgeId](n, 0D, BPRCostFunction(CostFunctionAttributes(
//      capacity = 2000,
//      freespeed = 30,
//      flow = 10D - n,
//      algorithmFlowRate = 60)))
//  }
//)
//.toMap

//val graph = new LocalGraphMATSim(Map(), Map(), edgeList)

implicit val tripletOrdering: Ordering[graph.Triplet] = Ordering.by {
  (t: graph.Triplet) => {
    val edge: EdgeMATSim =  graph.edgeAttrOf(t.e).get
    println(s"tripletOrdering with edge ${edge.id} snapshot flow ${edge.cost.snapshotFlow} and flow ${edge.flow} will be ordered by ${edge.linkCostFlow}")
    edge.linkCostFlow
  }
}
val startFrontier: collection.mutable.PriorityQueue[graph.Triplet] = collection.mutable.PriorityQueue()(tripletOrdering)
val t1 = graph.Triplet(0, 1L, 0)
val t2 = graph.Triplet(0, 2L, 0)
val t3 = graph.Triplet(0, 3L, 0)
startFrontier.enqueue(t1)
startFrontier.enqueue(t2)
startFrontier.enqueue(t3)
println("fully enqueued")
startFrontier.dequeueAll mkString " "