package cse.fitzgero.mcts.example

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate
import TicTacToe._
import org.scalatest.prop.PropertyChecks
import org.scalacheck.Gen

class TicTacToeTests extends SORoutingUnitTestTemplate with PropertyChecks {
  "TicTacToe" when {
    "Move" when {
      "winningMoves" when {
        "called with a piece" should {
          "create winning moves for that piece" in {
            Move.winningMoves(X) foreach {
              winningMove =>
                winningMove foreach {
                  _.piece should equal (X)
                }
            }
          }
        }
      }
    }
    "Board" when {
      "constructed" should {
        "be an empty board with the current player as X" in {
          val result = Board()
          result.currentPlayer should equal (X)
          result.state should equal (Set())
        }
      }
      ".possibleMoves" when {
        "called on an empty board" should {
          "give us all moves for the current player" in {

            // changed to a set since we have no ordering guarantees on the generated Sequence

            Board.possibleMoves(Board()).toSet should equal (
              Set(Move(UL, X), Move(UC, X), Move(UR, X), Move(CL, X), Move(CC, X), Move(CR, X), Move(BL, X), Move(BC, X), Move(BR, X))
            )
          }
        }
        "called with some moves on the board" should {
          "produce the remaining moves" in {
            val result = Board.possibleMoves(new Board(state = Set(Move(UL, X), Move(UC, X), Move(UR, X)), currentPlayer = X))
            result.toSet should equal (Set(Move(CL, X), Move(CC, X), Move(CR, X), Move(BL, X), Move(BC, X), Move(BR, X)))
          }
        }
        "called with some moves on the board and the other player is current" should {
          "produce the remaining moves" in {
            val result = Board.possibleMoves(new Board(state = Set(Move(UL, X), Move(UC, X), Move(UR, X)), currentPlayer = O))
            result.toSet should equal (Set(Move(CL, O), Move(CC, O), Move(CR, O), Move(BL, O), Move(BC, O), Move(BR, O)))
          }
        }
      }
      ".isWinner" when {
        "called when X is a winner" should {
          "return true" in {
            val result = Board.isWinner(new Board(state = Set(Move(UL, X), Move(UC, X), Move(UR, X)), currentPlayer = X), X)
            result should equal (true)
          }
        }
        "called when X is not a winner" should {
          "return false" in {
            val result = Board.isWinner(new Board(state = Set(Move(UL, X), Move(UC, O), Move(UR, X)), currentPlayer = X), X)
            result should equal (false)
          }
        }
      }
      ".gameState" when {
        "called on a board where X is a winner" should {
          "return XWins" in {
            val board = new Board(state = Set(Move(UL, X), Move(UC, X), Move(UR, X), Move(CC, O), Move(CL, O)), currentPlayer = X)
            Board.gameState(board) should equal (Board.XWins)
          }
        }
        "called on a board where O is a winner" should {
          "return OWins" in {
            val board = new Board(state = Set(Move(CL, X), Move(UC, X), Move(UR, O), Move(BR, O), Move(CR, O)), currentPlayer = O)
            Board.gameState(board) should equal (Board.OWins)
          }
        }
        "called on a board where the game is a stalemate" should {
          "return Stalemate" in {
            val board = new Board(state = Set(
              Move(UL, O), Move(UC, X), Move(UR, O), Move(CL, X), Move(CC, X), Move(CR, O), Move(BL, O), Move(BC, O), Move(BR, X)
            ), currentPlayer = X)
            Board.gameState(board) should equal (Board.Stalemate)
          }
        }
        "called on a game in progress with no winner" should {
          "return Turn" in {
            val board = new Board(state = Set(Move(UL, X), Move(CC, O), Move(CR, X)), currentPlayer = O)
            Board.gameState(board) should equal (Board.Turn(3))
          }
        }
      }
      ".applyMove" when {
        val moveGenerator = for {
          piece <- Gen.oneOf(Seq(X, O))
          position <- Gen.oneOf(Board.allPositions.toSeq)
        } yield Move(position, piece)
        "called with an empty board and an arbitrary move" should {
          "add the move to the board" in {
            forAll (moveGenerator) { (move: Move) =>
              new Board(move.piece).applyMove(move) should equal (
                new Board(currentPlayer = Piece.nextPlayer(move.piece), state=Set(move))
              )
            }
          }
        }
      }
    }
  }
}
