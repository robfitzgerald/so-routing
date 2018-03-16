package cse.fitzgero.mcts.core

import scala.annotation.tailrec
import scala.collection.GenSeq

import cse.fitzgero.mcts.MonteCarloTreeSearch
import cse.fitzgero.mcts.tree._

object Utilities {

//  def hasUnexploredActions(generatePossibleActions: (S) => Seq[A])(node: Tree): Boolean = {
//    val explored: GenSeq[A] = node.children match {
//      case None => Seq[A]()
//      case Some(c) => c.keys.toSeq
//    }
//    generatePossibleActions(node.state).diff(explored).nonEmpty
//  }
//
//
//  def bestGame(decisionCoefficients: Coefficients, root: Tree): Seq[A] =
//    if (root.hasNoChildren) Seq()
//    else {
//      @tailrec
//      def _bestGame(node: Tree, solution: Seq[A] = Seq.empty[A]): Seq[A] = {
//        if (node.hasNoChildren) solution
//        else {
//          bestChild(node, decisionCoefficients) match {
//            case None => solution
//            case Some(child) =>
//              child.action match {
//                case None => solution
//                case Some(action) =>
//                  _bestGame(child, solution :+ action)
//              }
//          }
//        }
//      }
//      _bestGame(root)
//    }

}
