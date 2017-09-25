package cse.fitzgero.sorouting.algorithm.routing.localgraph

import scala.collection.GenSeq

import cse.fitzgero.sorouting.algorithm.pathsearch.ksp._
import cse.fitzgero.sorouting.algorithm.pathsearch.od.localgraph.LocalGraphODPath
import cse.fitzgero.sorouting.roadnetwork.localgraph.{EdgeId, LocalGraphMATSim, VertexId}
import cse.fitzgero.sorouting.util.ClassLogging

object LocalGraphRouteSelection extends ClassLogging {
  /**
    * use the flow estimate as a oracle to select the best alternate paths
    * @param trees a tree of alternate paths
    * @param macroscopicFlowEstimate a graph with outflow proportions at each vertex
    * @return
    */
  def selectRoutes(
    trees: GenSeq[KSPSearchNode[EdgeId]],
    macroscopicFlowEstimate: LocalGraphMATSim
  ): GenSeq[LocalGraphODPath] = {
    def _selectRoute(tree: KSPSearchTree): List[(EdgeId, Double)] = {
      tree match {
        case x if x.isInstanceOf[KSPSearchRoot[_, _]] =>
          val node = x.asInstanceOf[KSPSearchRoot[VertexId, EdgeId]]
          if (node.children.isEmpty) List[(String, Double)]()
          else {
            val (edge, cost, proportion): (EdgeId, Double, Double) = node.children.map(tup => (tup._1, tup._2._1, macroscopicFlowEstimate.edgeAttrOf(tup._1).get.assignedFlow)).maxBy(_._3)
            (edge, cost) :: _selectRoute(node.traverse(edge))
          }
        case y if y.isInstanceOf[KSPSearchBranch[_]] =>
          val node = y.asInstanceOf[KSPSearchBranch[EdgeId]]
          if (node.children.isEmpty) List[(String, Double)]()
          else {
            val (edge, cost, proportion): (EdgeId, Double, Double) = node.children.map(tup => (tup._1, tup._2._1, macroscopicFlowEstimate.edgeAttrOf(tup._1).get.assignedFlow)).maxBy(_._3)
            (edge, cost) :: _selectRoute(node.traverse(edge))
          }
        case KSPSearchLeaf => Nil
        case _ => Nil // or error
      }
    }

    val result = trees.map({
      case x if x.isInstanceOf[KSPSearchRoot[_, _]] =>
        val node = x.asInstanceOf[KSPSearchRoot[VertexId,EdgeId]]
        val result: List[(EdgeId, Double)] = _selectRoute(x)
        val (path, cost) = result.unzip
        LocalGraphODPath(node.personId, node.srcVertex, node.dstVertex, path, cost)
      case _ =>
        LocalGraphODPath("",0,0,List(), List())
    })



    result
  }
}
