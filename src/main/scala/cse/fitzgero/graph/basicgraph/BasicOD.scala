package cse.fitzgero.graph.basicgraph

import scala.collection.GenSeq

sealed trait BasicOD

trait BasicODPair [V] extends BasicOD {
  def src: V
  def dst: V
}

trait BasicODBatch[O] extends BasicOD {
  def ods: GenSeq[O]
}