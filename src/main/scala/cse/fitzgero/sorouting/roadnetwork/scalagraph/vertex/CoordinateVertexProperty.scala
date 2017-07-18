package cse.fitzgero.sorouting.roadnetwork.scalagraph.vertex

case class Euclidian (x: Double, y: Double) extends VertexPosition {
  def apply(attrs: Map[String, String]): Euclidian = {
    if (attrs.isDefinedAt("x") && attrs.isDefinedAt("y")) {
      Euclidian(
        attrs("x").toDouble,
        attrs("y").toDouble
      )
    } else throw new IllegalArgumentException(s"constructing Euclidian from attribute map with missing x and y attributes in ${attrs.mkString(" ")}")
  }
}

/**
  * a vertex with a 2D euclidian coordinate
  * @param position x and y position of vertex
  */
case class CoordinateVertexProperty (
  override val position: Euclidian)
  extends VertexProperty[Euclidian] (position) {}
