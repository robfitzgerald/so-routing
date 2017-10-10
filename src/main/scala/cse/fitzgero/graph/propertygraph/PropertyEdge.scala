package cse.fitzgero.graph.propertygraph

import cse.fitzgero.graph.basicgraph.BasicEdge

trait PropertyEdge extends BasicEdge {
  type Attr
  def attribute: Attr
}
