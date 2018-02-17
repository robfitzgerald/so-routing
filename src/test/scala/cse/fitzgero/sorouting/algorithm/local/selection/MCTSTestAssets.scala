package cse.fitzgero.sorouting.algorithm.local.selection

import java.time.LocalTime

import cse.fitzgero.sorouting.algorithm.local.selection.SelectionLocalMCTSAlgorithm.Tag
import cse.fitzgero.sorouting.model.population.LocalRequest
import cse.fitzgero.sorouting.model.roadnetwork.costfunction._
import cse.fitzgero.sorouting.model.roadnetwork.local._
import org.scalacheck.Gen

object MCTSTestAssets {

  trait selectionMethodGenerator {
    val globalAltsGen: Gen[Seq[(String, Map[Tag, Seq[String]])]] = Gen.containerOf[Seq, (String, Map[Tag, Seq[String]])] {
      for {
        pID <- Gen.alphaChar // Gen.oneOf((97 to 122).map(_.toChar.toString))
        alts <- Gen.choose(1, 8)
      } yield {
        val personID: String = pID.toString
        val altPaths: Map[Tag, Seq[String]] = (1 to alts).map {
          alt =>
            Tag(personID, alt) -> (1 to 2).map {
              edgeId => s"$personID-alt$alt-edge$edgeId"
            }
        }.toMap
        (personID, altPaths)
      }
    }
  }
  trait selectionMethodMock {
    val globalAlts: Map[String, Map[Tag, Seq[String]]] =
      Map(
        "e" -> Map(
          Tag("e", 1) -> Vector("e-alt1-edge1", "e-alt1-edge2"),
          Tag("e", 5) -> Vector("e-alt5-edge1", "e-alt5-edge2"),
          Tag("e", 3) -> Vector("e-alt3-edge1", "e-alt3-edge2"),
          Tag("e", 4) -> Vector("e-alt4-edge1", "e-alt4-edge2"),
          Tag("e", 6) -> Vector("e-alt6-edge1", "e-alt6-edge2"),
          Tag("e", 2) -> Vector("e-alt2-edge1", "e-alt2-edge2")
        ),
        "n" -> Map(
          Tag("n", 5) -> Vector("n-alt5-edge1", "n-alt5-edge2"),
          Tag("n", 1) -> Vector("n-alt1-edge1", "n-alt1-edge2"),
          Tag("n", 6) -> Vector("n-alt6-edge1", "n-alt6-edge2"),
          Tag("n", 4) -> Vector("n-alt4-edge1", "n-alt4-edge2"),
          Tag("n", 3) -> Vector("n-alt3-edge1", "n-alt3-edge2"),
          Tag("n", 2) -> Vector("n-alt2-edge1", "n-alt2-edge2")
        ),
        "j" -> Map(
          Tag("j", 1) -> Vector("j-alt1-edge1", "j-alt1-edge2"),
          Tag("j", 2) -> Vector("j-alt2-edge1", "j-alt2-edge2"),
          Tag("j", 3) -> Vector("j-alt3-edge1", "j-alt3-edge2")
        ),
        "t" -> Map(
          Tag("t", 1) -> Vector("t-alt1-edge1", "t-alt1-edge2"),
          Tag("t", 2) -> Vector("t-alt2-edge1", "t-alt2-edge2"),
          Tag("t", 3) -> Vector("t-alt3-edge1", "t-alt3-edge2"),
          Tag("t", 5) -> Vector("t-alt5-edge1", "t-alt5-edge2"),
          Tag("t", 4) -> Vector("t-alt4-edge1", "t-alt4-edge2"),
          Tag("t", 6) -> Vector("t-alt6-edge1", "t-alt6-edge2")
        ),
        "i" -> Map(
          Tag("i", 1) -> Vector("i-alt1-edge1", "i-alt1-edge2"),
          Tag("i", 2) -> Vector("i-alt2-edge1", "i-alt2-edge2")
        ),
        "g" -> Map(
          Tag("g", 1) -> Vector("g-alt1-edge1", "g-alt1-edge2"),
          Tag("g", 2) -> Vector("g-alt2-edge1", "g-alt2-edge2"),
          Tag("g", 3) -> Vector("g-alt3-edge1", "g-alt3-edge2"),
          Tag("g", 4) -> Vector("g-alt4-edge1", "g-alt4-edge2")
        ), "c" -> Map(
          Tag("c", 1) -> Vector("c-alt1-edge1", "c-alt1-edge2"),
          Tag("c", 2) -> Vector("c-alt2-edge1", "c-alt2-edge2")
        ), "h" -> Map(Tag("h", 2) -> Vector("h-alt2-edge1", "h-alt2-edge2"), Tag("h", 8) -> Vector("h-alt8-edge1", "h-alt8-edge2"), Tag("h", 3) -> Vector("h-alt3-edge1", "h-alt3-edge2"), Tag("h", 1) -> Vector("h-alt1-edge1", "h-alt1-edge2"), Tag("h", 7) -> Vector("h-alt7-edge1", "h-alt7-edge2"), Tag("h", 5) -> Vector("h-alt5-edge1", "h-alt5-edge2"), Tag("h", 6) -> Vector("h-alt6-edge1", "h-alt6-edge2"), Tag("h", 4) -> Vector("h-alt4-edge1", "h-alt4-edge2")), "r" -> Map(Tag("r", 1) -> Vector("r-alt1-edge1", "r-alt1-edge2"), Tag("r", 2) -> Vector("r-alt2-edge1", "r-alt2-edge2")), "d" -> Map(Tag("d", 2) -> Vector("d-alt2-edge1", "d-alt2-edge2"), Tag("d", 1) -> Vector("d-alt1-edge1", "d-alt1-edge2"), Tag("d", 5) -> Vector("d-alt5-edge1", "d-alt5-edge2"), Tag("d", 4) -> Vector("d-alt4-edge1", "d-alt4-edge2"), Tag("d", 3) -> Vector("d-alt3-edge1", "d-alt3-edge2")))
  }

  trait graphComposedAlts extends requestComposed with graphComposedAltsTopology {
    val graph: LocalGraph =
      LocalGraph(
        adjList = adjList,
        vertexList = vertexList,
        edgeList = shortestPath ++ alts(None)
      )
  }

  trait requestComposed {
    val request: Seq[LocalRequest] =
      (1 to 9)
        .map {
          id =>
            LocalRequest(id.toString, LocalODPair(s"req-${id.toString}", "a", "d"), LocalTime.MIN)
        }
  }

  trait graphComposedAltsTopology {
    val adjList: Map[String, Map[String, String]] =
      Map(
        "a" -> Map("ab" -> "b", "ad" -> "d"),
        "b" -> Map("bc" -> "c", "bd" -> "d"),
        "c" -> Map("cd1" -> "d", "cd2" -> "d")
      )

    val vertexList: Map[String, LocalVertex] =
      Map[String, LocalVertex](
        "a" -> LocalVertex("a", 0, 0),
        "b" -> LocalVertex("b", 0, 50),
        "c" -> LocalVertex("c", 0, 100),
        "d" -> LocalVertex("d", 0, 150)
      )

    val shortestPath: Map[String, LocalEdge] =
      Map(
        "ab" -> LocalEdge("ab", "a", "b", new LocalEdgeFlowAttribute(None, Some(4), Some(8.33), Some(50)) with BPRCostFunction),
        "bc" -> LocalEdge("bc", "b", "c", new LocalEdgeFlowAttribute(None, Some(4), Some(8.33), Some(50)) with BPRCostFunction),
        "cd1" -> LocalEdge("cd1", "c", "d", new LocalEdgeFlowAttribute(None, Some(4), Some(8.33), Some(50)) with BPRCostFunction)
      )

    def alts(flow: Option[Double]): Map[String, LocalEdge] =
      Map(
        "ad" -> LocalEdge("ad", "a", "d", new LocalEdgeFlowAttribute(flow, Some(4), Some(8.33), Some(50)) with BPRCostFunction),
        "bd" -> LocalEdge("bd", "b", "d", new LocalEdgeFlowAttribute(flow, Some(4), Some(8.33), Some(50)) with BPRCostFunction),
        "cd2" -> LocalEdge("cd2", "c", "d", new LocalEdgeFlowAttribute(flow, Some(4), Some(8.33), Some(50)) with BPRCostFunction)
      )
  }

  // 9 requests over a map with 3 valid paths, each which should not increase too fast for < 4 people
  trait graphSquare9PeopleThreeWays extends graphSquareTopology with requestSquare {
    val graph =
      LocalGraph(
        adjList = adjList,
        vertexList = vertexList,
        edgeList = outerPathsEdges ++ tenElevenPaths(Some(Double.MaxValue)) ++ oneFivePath(None)
      )
  }
  
  trait requestSquare {
    val request: Seq[LocalRequest] =
    (1 to 9)
    .map {
          id =>
            LocalRequest(id.toString, LocalODPair(s"req-${id.toString}", "0", "9"), LocalTime.MIN)
        }
  }
  
  trait graphSquareTopology {
    val adjList: Map[String, Map[String, String]] = 
      Map(
        "0" -> Map("001" -> "1", "002" -> "2", "004" -> "4"),
        "1" -> Map("102" -> "2", "104" -> "4", "105" -> "5", "110" -> "10", "111" -> "11"),
        "2" -> Map("203" -> "3"),
        "3" -> Map("306" -> "6"),
        "4" -> Map("407" -> "7"),
        "5" -> Map("509" -> "9"),
        "6" -> Map("609" -> "9"),
        "7" -> Map("708" -> "8"),
        "8" -> Map("809" -> "9"),
        "9" -> Map(),
        "10" -> Map("10-9" -> "9"),
        "11" -> Map("11-9" -> "9")
      )
    val vertexList: Map[String, LocalVertex] = 
      Map[String, LocalVertex](
        "0" -> LocalVertex("0", -25, -25),
        "1" -> LocalVertex("1", 0, 0),
        "2" -> LocalVertex("2", 0, 50),
        "3" -> LocalVertex("3", 0, 100),
        "4" -> LocalVertex("4", 50, 0),
        "5" -> LocalVertex("5", 50, 50),
        "6" -> LocalVertex("6", 50, 100),
        "7" -> LocalVertex("7", 100, 0),
        "8" -> LocalVertex("8", 100, 50),
        "9" -> LocalVertex("9", 100, 100),
        "10" -> LocalVertex("10", 33, 66),
        "11" -> LocalVertex("11", 66, 33)
      )
    val outerPathsEdges: Map[String, LocalEdge] =
      Map[String, LocalEdge](
        "001" -> LocalEdge("001", "0", "1", new LocalEdgeFlowAttribute(None, Some(4), Some(8.33), Some(50)) with BPRCostFunction),
        "002" -> LocalEdge("002", "0", "2", new LocalEdgeFlowAttribute(None, Some(4), Some(8.33), Some(50)) with BPRCostFunction),
        "004" -> LocalEdge("004", "0", "4", new LocalEdgeFlowAttribute(None, Some(4), Some(8.33), Some(50)) with BPRCostFunction),
        "102" -> LocalEdge("102", "1", "2", new LocalEdgeFlowAttribute(None, Some(4), Some(8.33), Some(50)) with BPRCostFunction),
        "104" -> LocalEdge("104", "1", "4", new LocalEdgeFlowAttribute(None, Some(4), Some(8.33), Some(50)) with BPRCostFunction),
        "203" -> LocalEdge("203", "2", "3", new LocalEdgeFlowAttribute(None, Some(4), Some(8.33), Some(50)) with BPRCostFunction),
        "306" -> LocalEdge("306", "3", "6", new LocalEdgeFlowAttribute(None, Some(4), Some(8.33), Some(50)) with BPRCostFunction),
        "407" -> LocalEdge("407", "4", "7", new LocalEdgeFlowAttribute(None, Some(4), Some(8.33), Some(50)) with BPRCostFunction),
        "609" -> LocalEdge("609", "6", "9", new LocalEdgeFlowAttribute(None, Some(4), Some(8.33), Some(50)) with BPRCostFunction),
        "708" -> LocalEdge("708", "7", "8", new LocalEdgeFlowAttribute(None, Some(4), Some(8.33), Some(50)) with BPRCostFunction),
        "809" -> LocalEdge("809", "8", "9", new LocalEdgeFlowAttribute(None, Some(4), Some(8.33), Some(50)) with BPRCostFunction)
      )
    def tenElevenPaths(flow: Option[Double]): Map[String, LocalEdge] =
      Map(
        "110" -> LocalEdge("110", "1", "10", new LocalEdgeFlowAttribute(flow, Some(4), Some(8.33), Some(50)) with BPRCostFunction),
        "10-9" -> LocalEdge("10-9", "10", "9", new LocalEdgeFlowAttribute(flow, Some(4), Some(8.33), Some(50)) with BPRCostFunction),
        "111" -> LocalEdge("111", "1", "11", new LocalEdgeFlowAttribute(flow, Some(4), Some(8.33), Some(50)) with BPRCostFunction),
        "11-9" -> LocalEdge("11-9", "11", "9", new LocalEdgeFlowAttribute(flow, Some(4), Some(8.33), Some(50)) with BPRCostFunction)
    )
    def oneFivePath(flow: Option[Double]): Map[String, LocalEdge] =
      Map(
        "105" -> LocalEdge("105", "1", "5", new LocalEdgeFlowAttribute(flow, Some(4), Some(8.33), Some(50)) with BPRCostFunction),
        "509" -> LocalEdge("509", "5", "9", new LocalEdgeFlowAttribute(flow, Some(4), Some(8.33), Some(50)) with BPRCostFunction)
      )
  }
  
}
