package cse.fitzgero.mcts

import scala.annotation.tailrec
import scala.collection.GenSeq

import cse.fitzgero.mcts.core._
import cse.fitzgero.mcts.tree._

trait MonteCarloTreeSearch[S,A] {

  ////////// domain and user-provided operations. to be implemented by the user //////////

  /**
    * called on each TreePolicy. offers the chance to evaluate the tree before returning the search function coefficients.
    * @param tree the current decision point of the tree
    * @return coefficients for the search phase
    */
  def getSearchCoefficients(tree: Tree): Coefficients

  /**
    * the decision process may involve a different set of coefficients than the search phase. this provides an interface for those specifically.
    * @param tree the current decision point of the tree
    * @return coefficients for making a decision
    */
  def getDecisionCoefficients(tree: Tree): Coefficients

  /**
    * provides the means to create new trees. defined at the variant level, used by the expand algorithm.
    * @param state the state for the new node
    * @param action the action, if any, that was taken to reach the state/node
    * @return
    */
  def createNewNode(state: S, action: Option[A]): Tree

  /**\
    * given a state, generate all valid actions that can be performed
    * @param state the given state
    * @return a sequence of actions
    */
  def generatePossibleActions(state: S): Seq[A]

  /**
    * function that takes a state and an action, and produces the state that would result from taking this action
    * @param state the given state
    * @param action the given action
    * @return
    */
  def applyAction(state: S, action: A): S

  /**
    * given a terminal game state, produce a reward for this configuration
    * @param state a terminal game state
    * @return
    */
  def evaluateTerminal(state: S): Reward


  def evaluateBranch(tree: Tree, coefficients: Coefficients): Reward

  /**
    * recognizes non-terminal game states
    * @param state the given state
    * @return
    */
  def stateIsNonTerminal(state: S): Boolean

  /**
    * choose a single action from a list of possible actions. usually implemented by simply calling ${actionSelection.selectAction()}
    * @param actions a list of possible actions
    * @return
    */
  def selectAction(actions: Seq[A]): Option[A]

  /**
    * the pure start state for this game
    * @return
    */
  def startState: S

  /**
    * generates a root node for this game
    * @param state the start state for this game
    * @return
    */
  def startNode(state: S): Tree

//  /**
//    *
//    * for UCT, this would be the exploration coefficient 'Cp'. 0.7071D has been shown by Kocsis and Szepesvari (2006) to satisfy the 'Hoeffding inequality'
//    * @return
//    */
//  def coefficients: Coefficients


  //////// utility operations. provided by the MCTS library ////////////

  protected def terminationCriterion: TerminationCriterion
  protected def actionSelection: ActionSelection[S,A]
  protected def random: RandomGenerator

  /**
    * this should come from the sampling policy as a case class, exposed to the user
    */
  type Coefficients

  /**
    * this is the type of the tree, which can vary depending on reward type
    * this may not be truly necessary, but updates on rewards have placed it as an abstract type for the time being
    */
  type Tree <: MonteCarloTree[S,A,Reward,Tree]

  /**
    * reward type. something with a numeric ordering. this should come from the mcts variant file
    */
  type Reward

  /**
    * the ordering used to compare children during the treePolicy bestChild call, which uses maxBy to find a best child
    * @return
    */
  def rewardOrdering: Ordering[Reward]

  ///////// core operations. provided by a variant in the MCTS library ///////////////

  /**
    * picks a state in the search tree via a traversal that balances exploration and exploitation of the current tree state. may result in adding one additional previously unexplored node
    * @param node the search tree node in our tree traversal
    * @param coefficients the exploration coefficients, which are user-set
    * @return
    */
  protected def treePolicy(node: Tree, coefficients: Coefficients)(implicit ordering: Ordering[Reward]): Tree


  /**
    * a function that simulates the completion of the game from the current search node via a stochastic process, and returns a reward for the outcome
    * @param node the search node picked by the tree policy in the current iteration
    * @return
    */
  protected def defaultPolicy(node: Tree): Reward

  /**
    * updates the reward at this node, and back-propagates the reward to this node's parent, if applicable
    * @param node the current node in our back-propagation traversal
    * @param delta the reward to apply
    * @return
    */
  protected def backup(node: Tree, delta: Reward): Tree

  /**
    * chooses a child to expand via a provided selection method and attaches that new node to the tree
    * @param node the parent node we are expanding from
    * @return the new node of the tree
    */
  protected def expand(node: Tree): Option[Tree]

  /**
    * find the best child of a parent node based on the selection policy of this MCTS algorithm
    * @param node the parent node
    * @param coefficients the coefficients for our bandit selection function
    * @return the best child, based on the evaluate function provided by the user
    */
  protected def bestChild(node: Tree, coefficients: Coefficients)(implicit ordering: Ordering[Reward]): Option[Tree]

  //////// implemented members //////////


  /**
    * run this Monte Carlo Tree Search
    * @return the tree at the end of the search
    */
  final def run(root: Tree = startNode(startState)): Tree = {
    while (terminationCriterion.terminationCheck[S,A,Tree](root)) {
      val v_t = treePolicy(root, getSearchCoefficients(root))(rewardOrdering)
      val ∆ = defaultPolicy(v_t)
      backup(v_t, ∆)
    }
    root
  }


  /**
    * a utility that generates possible actions in order to tell us whether this node has any remaining to exploit
    * @param generatePossibleActions the function that determines the possible actions from this node's state
    * @param node this node
    * @return true, if there are unexplored actions to try, else false
    */
  final private def _hasUnexploredActions(generatePossibleActions: (S) => Seq[A])(node: Tree): Boolean = {
    val explored: GenSeq[A] = node.children match {
      case None => Seq[A]()
      case Some(c) => c.keys.toSeq
    }
    generatePossibleActions(node.state).diff(explored).nonEmpty
  }

  /**
    * a curried call to _hasUnexploredActions, with this tree search's generatePossibleActions loaded into it
    * @return true, if there are unexplored actions to try, else false
    */
  final protected def hasUnexploredActions: (Tree) => Boolean = _hasUnexploredActions(generatePossibleActions)

  /**
    * gives the best path through the search space at the decision time
    * @param root the decision point
    * @return a sequence of actions to perform with the expected best payoff
    */
  final def bestGame(root: Tree): Seq[A] =
    if (root.hasNoChildren) Seq()
    else {
      @tailrec
      def _bestGame(node: Tree, solution: Seq[A] = Seq.empty[A]): Seq[A] = {
        if (node.hasNoChildren) solution
        else {
          bestChild(node, getDecisionCoefficients(root))(rewardOrdering) match {
            case None => solution
            case Some(child) =>
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

  /**
    * gives the best top-level action at the decision time
    * @param decisionCoefficients the coefficients required to select best children without exploration
    * @param root the decision point
    * @return an action to perform with the expected best payoff
    */
  final def bestMove(decisionCoefficients: Coefficients, root: Tree): Option[A] =
    for {
      child <- bestChild(root, decisionCoefficients)(rewardOrdering)
      action <- child.action
    } yield action

}