package cse.fitzgero.mcts.example

object TicTacToe {
  sealed trait Piece
  case object X extends Piece
  case object O extends Piece
  object Piece {
    def nextPlayer(current: Piece): Piece = if (current == X) O else X
  }

  case class Board(currentPlayer: Piece, state: Set[Move] = Set()) {

    def applyMove(move: Move): Board =
      this.copy(
        state = this.state + move,
        currentPlayer = Piece.nextPlayer(currentPlayer)
      )
  }

  object Board {

    sealed trait BoardState
    case class Turn(n: Int) extends BoardState
    case object XWins extends BoardState
    case object OWins extends BoardState
    case object Stalemate extends BoardState

    val allPositions: Set[Position] = Set(UL, UC, UR, CL, CC, CR, BL, BC, BR)

    def apply(): Board = new Board(X)

    def possibleMoves(board: Board): Seq[Move] =
      allPositions
        .diff(board.state.map { _.position })
        .map { pos => Move(pos, board.currentPlayer)}
        .toSeq

    def gameState(board: Board): BoardState =
      if (isWinner(board, X)) XWins
      else if (isWinner(board, O)) OWins
      else if (isFull(board)) Stalemate
      else Turn(board.state.size)

    def isWinner(b: Board, p: Piece): Boolean =
      Move.winningMoves(p).exists {
        _.forall {
          move => b.state(move)
        }
      }

    def isFull(board: Board): Boolean = board.state.size == 9

  }

  case class Move (position: Position, piece: Piece) {
    override def toString: String = s"$piece plays $position"
  }

  sealed trait Position
  case object UL extends Position
  case object UC extends Position
  case object UR extends Position
  case object CL extends Position
  case object CC extends Position
  case object CR extends Position
  case object BL extends Position
  case object BC extends Position
  case object BR extends Position
  object Move {
    type WinningMove = Set[Move]
    def winningMoves(p: Piece): Set[WinningMove] =
      Set(
        Set(Move(UL, p), Move(UC, p), Move(UR, p)),
        Set(Move(UL, p), Move(CL, p), Move(BL, p)),
        Set(Move(UL, p), Move(CC, p), Move(BR, p)),
        Set(Move(BL, p), Move(CC, p), Move(UR, p)),
        Set(Move(UR, p), Move(CR, p), Move(BR, p)),
        Set(Move(BL, p), Move(BC, p), Move(BR, p))
      )
  }
}
