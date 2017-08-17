object Hierarchies {

  sealed trait Path
  case object NoPath extends Path
  case class HasPath(path: List[String]) extends Path

  abstract class Leg
  case

}