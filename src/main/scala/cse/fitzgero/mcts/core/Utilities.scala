package cse.fitzgero.mcts.core

import scala.annotation.tailrec
import scala.collection.GenSeq

import cse.fitzgero.mcts.tree.MonteCarloTree

object Utilities {

  def hasUnexploredActions[S,A](generatePossibleActions: (S) => Seq[A])(node: MonteCarloTree[S,A]): Boolean = {
    val explored: GenSeq[A] = node.children match {
      case None => Seq[A]()
      case Some(c) => c.keys.toSeq
    }
    generatePossibleActions(node.state).diff(explored).nonEmpty
  }


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
