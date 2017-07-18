package cse.fitzgero.sorouting.roadnetwork.scalagraph.edge

import cse.fitzgero.sorouting.roadnetwork.scalagraph.vertex.CoordinateVertexProperty

import scalax.collection.GraphEdge.{DiEdge, EdgeCopy, ExtendedKey, NodeProduct}
import scalax.collection.GraphPredef.OuterEdge

case class Edge[+N](srcIntersection: N, dstIntersection: N, attr: MacroscopicEdgeProperty)
  extends DiEdge[N](NodeProduct(srcIntersection, dstIntersection))
    with    ExtendedKey[N]
    with    EdgeCopy[Edge]
    with    OuterEdge[N,Edge]
{
  private def this(nodes: Product, flightNo: String) {
    this(nodes.productElement(0).asInstanceOf[N],
      nodes.productElement(1).asInstanceOf[N], flightNo)
  }
  def keyAttributes = Seq(attr)
  override def copy[NN](newNodes: Product) = new Edge[NN](newNodes, attr)
  override protected def attributesToString = s" ($attr)"
}
object Flight {
  implicit final class ImplicitEdge[A <: CoordinateVertexProperty](val e: DiEdge[A]) extends AnyVal {
    def ## (attr: MacroscopicEdgeProperty) = new Edge[A](e.source, e.target, attr)
  }
}