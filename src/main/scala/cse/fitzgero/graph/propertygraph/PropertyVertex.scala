package cse.fitzgero.graph.propertygraph

import cse.fitzgero.graph.basicgraph.BasicVertex

trait PropertyVertex extends BasicVertex {
  type Attr
  def attribute: Attr
}
