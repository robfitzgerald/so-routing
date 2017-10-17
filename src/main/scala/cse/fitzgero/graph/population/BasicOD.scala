package cse.fitzgero.graph.population

//import scala.collection.GenSeq

trait BasicOD {
  type VertexId
  def src: VertexId
  def dst: VertexId
}

//trait BasicODPair extends BasicOD {
//  type VertexId
//  def src: VertexId
//  def dst: VertexId
//}

//trait BasicODBatch extends BasicOD { batch =>
//  type VertexId
//  type ODPair <: BasicODPair {
//    type VertexId = batch.VertexId
//  }
//  def ods: GenSeq[ODPair]
//}