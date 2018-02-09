package cse.fitzgero.mcts

import scala.collection.GenMap

class MonteCarloTree [S,A] (
  val depth: Int = 0,
  var visits: Int = 0,
  var reward: Double = 0D,
  val state: S,
  var children: Option[GenMap[A, () => Option[MonteCarloTree[S,A]]]] = None,
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
    visits += 1
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
        children = Some(GenMap(action -> (() => Some(node))))
        this
      case Some(childrenToUpdate) =>
        children = Some(childrenToUpdate.updated(action, () => Some(node)))
        this
    }
  }

  def hasChildren: Boolean = children.nonEmpty
  def hasNoChildren: Boolean = children.isEmpty
//  def hasUnexploredChildren: Boolean = children match {
//    case None => false
//    case Some(c) => c.exists(_._2().isEmpty)
//  }
//  def hasNoUnexploredChildren: Boolean = !hasUnexploredChildren

  def print: String =
    s"${ " " * depth } ${if (depth == 0) "root" else if (children.isEmpty) "leaf" else "brch"}" +
      s" ${"%.3f".format(reward / visits)} ($reward/$visits)\n" + (children match {
      case None => ""
      case Some(childrenUnpacked) => childrenUnpacked map {
        child => child._2()
      }
    })

}

object MonteCarloTree {
  def apply[S,A](state: S): MonteCarloTree[S,A] = new MonteCarloTree[S,A](state = state)
  def apply[S,A](state: S,
                 action: Option[A],
                 children: Option[GenMap[A, () => Option[MonteCarloTree[S,A]]]],
                 parent: MonteCarloTree[S,A]): MonteCarloTree[S,A] = {
    val childDepth = parent.depth + 1
    val parentLink = () => Some(parent)
    new MonteCarloTree(depth = childDepth, state = state, children = children, action = action, parent = parentLink)
  }
}

