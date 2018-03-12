package cse.fitzgero.mcts.tree

import scala.collection.GenMap

class MonteCarloTree [S,A] (
  val depth: Int = 0,
  var visits: Long = 0,
  var reward: Double = 0D,
  val state: S,
  var children: Option[GenMap[A, () => MonteCarloTree[S,A]]] = None,
  val action: Option[A] = None,
  val parent: () => Option[MonteCarloTree[S,A]] = () => None
) {

  /**
    * predicate function to tell if this node is a leaf
    * @return
    */
  def isLeaf: Boolean = children.isEmpty

  /**
    * updates the reward value at this node in the tree
    * @param rewardUpdate the reward to add, typically 0 or 1
    * @return the updated (mutated) tree
    */
  def updateReward(rewardUpdate: Double): MonteCarloTree[S,A] = {
    reward = reward + rewardUpdate
    visits += 1L
    this
  }

  /**
    * adds a tree node to the children of this node
    * @param action the "action" it takes to move from the current node to this child
    * @param node the child node to add
    * @return the updated (mutated) tree
    */
  def addChild(action: A, node: MonteCarloTree[S,A]): MonteCarloTree[S,A] = {
    children match {
      case None =>
        children = Some(GenMap(action -> (() => node)))
        this
      case Some(childrenToUpdate) =>
        children = Some(childrenToUpdate.updated(action, () => node))
        this
    }
  }

  def hasChildren: Boolean = children.nonEmpty
  def hasNoChildren: Boolean = children.isEmpty

  override def toString: String = {
    val leadIn = if (depth == 0) "" else s"$depth${ " " * (depth-1) }"
    val nodeType = if (depth == 0) "root" else if (children.isEmpty) "leaf" else "brch"
    val actionUsed = action match {
      case None => ""
      case Some(a) => s"$a"
    }
    val averageValue = s"${"%.0f".format((reward / visits) * 100)}% ($reward/$visits)"
    s"$leadIn$nodeType - $actionUsed - $averageValue\n"
  }

  def defaultTransform(nodes: List[(A, () => MonteCarloTree[S,A])]): List[MonteCarloTree[S,A]] = nodes.map { _._2() }
  def printTree(printDepth: Int, transform: List[(A, () => MonteCarloTree[S,A])] => List[MonteCarloTree[S,A]] = defaultTransform): String = {
    val childrenStrings: String =
      if (depth >= printDepth)
        ""
      else {
        children match {
          case None => ""
          case Some(childrenUnpacked) =>

            val childrenData = transform(childrenUnpacked.toList) map {
              child => s"${child.printTree(printDepth, transform)}"
            }
            childrenData.mkString("")
        }
      }
    toString + childrenStrings
  }

  def printBestTree(printDepth: Int, evaluate: S => Double): String = {
    def transform(nodes: List[(A, () => MonteCarloTree[S,A])]) = {
      List(
        nodes.maxBy{
          child =>
            val childUnpacked = child._2()
            childUnpacked.reward / childUnpacked.visits
        }._2()
      )
    }
    printTree(printDepth, transform)
  }

}

object MonteCarloTree {
  def apply[S,A](state: S): MonteCarloTree[S,A] = new MonteCarloTree[S,A](state = state)
  def apply[S,A](state: S,
                 action: Option[A],
                 children: Option[GenMap[A, () => MonteCarloTree[S,A]]],
                 parent: MonteCarloTree[S,A]): MonteCarloTree[S,A] = {
    val childDepth = parent.depth + 1
    val parentLink = () => Some(parent)
    new MonteCarloTree(depth = childDepth, state = state, children = children, action = action, parent = parentLink)
  }
}

