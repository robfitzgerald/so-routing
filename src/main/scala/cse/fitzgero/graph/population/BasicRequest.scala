package cse.fitzgero.graph.population

trait BasicRequest {
  type TimeUnit
  type Id
  type OD <: BasicOD
  def id: Id
  def od: OD
  def requestTime: TimeUnit
}
