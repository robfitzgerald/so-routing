package cse.fitzgero.mcts

import scala.collection.GenMap

class MonteCarloTree [S,A] (
  var visits: Int = 0,
  var reward: Double = 0D,
  val state: Option[S] = None,
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
  def updateReward(rewardUpdate: Int): MonteCarloTree[S,A] = {
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
      case None => this
      case Some(childrenToUpdate) =>
        children = Some(childrenToUpdate.updated(action, () => Some(node)))
        this
    }
  }

  def hasChildren: Boolean = children.nonEmpty
  def hasNoChildren: Boolean = children.isEmpty
  def hasUnexploredChildren: Boolean = children match {
    case None => false
    case Some(c) => c.exists(_._2().isEmpty)
  }
  def hasNoUnexploredChildren: Boolean = !hasUnexploredChildren
}

object MonteCarloTree {
  def apply[S,A](): MonteCarloTree[S,A] = new MonteCarloTree[S,A]()
  def apply[S,A](
    state: Option[S],
    action: Option[A],
    children: Option[GenMap[A, () => Option[MonteCarloTree[S,A]]]],
    parent: () => Option[MonteCarloTree[S,A]]) =
    new MonteCarloTree(state = state, children = children, action = action, parent = parent)
}

