package cse.fitzgero.mcts.core

import scala.annotation.tailrec

import cse.fitzgero.mcts.MonteCarloTree

object Utilities {

  def hasUnexploredActions[S,A](generatePossibleActions: (S) => Seq[A])(node: MonteCarloTree[S,A]): Boolean =
    generatePossibleActions(node.state)
      .diff(node.children match {
        case None => Seq[A]()
        case Some(c) => c.keys.toSeq
      })
      .isEmpty


  def bestGame[S,A](bestChild: (MonteCarloTree[S,A], Double) => Option[MonteCarloTree[S,A]])(root: MonteCarloTree[S,A]): Seq[A] =
    if (root.hasNoChildren) Seq()
    else {
      @tailrec
      def _bestGame(node: MonteCarloTree[S,A], solution: Seq[A] = Seq.empty[A]): Seq[A] = {
        if (node.hasNoChildren) solution
        else {
          bestChild(node, 0D) match {
            case None => solution
            case Some(child: MonteCarloTree[S,A]) =>
              child.action match {
                case None => solution
                case Some(action) =>
                  _bestGame(child, solution :+ action)
              }
          }
        }
      }
      _bestGame(root)
    }

}
