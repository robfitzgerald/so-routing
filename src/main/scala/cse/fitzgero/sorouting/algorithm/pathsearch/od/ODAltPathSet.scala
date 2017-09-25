package cse.fitzgero.sorouting.algorithm.pathsearch.od

import scala.collection.GenSeq

trait ODAltPathSet [O <:  ODPath[_,_]] {
  def paths: GenSeq[O]
}
