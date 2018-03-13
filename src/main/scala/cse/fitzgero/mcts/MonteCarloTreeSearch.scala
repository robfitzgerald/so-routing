package cse.fitzgero.mcts

import cse.fitzgero.mcts.reward.MCTSAlgorithm
import cse.fitzgero.mcts.core._
import cse.fitzgero.mcts.reward.scalar.UCTScalarStandardReward
import cse.fitzgero.mcts.tree._

trait MonteCarloTreeSearch[S,A,R,C] {

  type Tree <: MonteCarloTree[S,A,R,_]

  ////////// domain and user-provided operations. to be implemented by the user //////////

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
  def evaluate(state: S): R

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

  /**
    * exploration coefficient. 0.7071D has been shown by Kocsis and Szepesvari (2006) to satisfy the 'Hoeffding inequality'
    * @return
    */
  def Cp: Double = 0.7071D

  //////// utility operations. provided by the MCTS library ////////////

  protected def samplingMethod: MCTSAlgorithm[Tree, C]
  protected def terminationCriterion: TerminationCriterion
  protected def actionSelection: ActionSelection[S,A]
  protected def random: RandomGenerator

  ///////// core operations. provided by a variant in the MCTS library ///////////////

  /**
    * picks a state in the search tree via a traversal that balances exploration and exploitation of the current tree state. may result in adding one additional previously unexplored node
    * @param node the search tree node in our tree traversal
    * @param Cp the exploration coefficient, which is user-set
    * @return
    */
  protected def treePolicy(node: Tree, Cp: Double): Tree


  /**
    * a function that simulates the completion of the game from the current search node via a stochastic process, and returns a reward for the outcome
    * @param node the search node picked by the tree policy in the current iteration
    * @return
    */
  protected def defaultPolicy(node: Tree): R

  /**
    * updates the reward at this node, and back-propagates the reward to this node's parent, if applicable
    * @param node the current node in our back-propagation traversal
    * @param delta the reward to apply
    * @return
    */
  protected def backup(node: Tree, delta: R): Tree

  /**
    * chooses a child to expand via a provided selection method and attaches that new node to the tree
    * @param node the parent node we are expanding from
    * @return the new node of the tree
    */
  protected def expand(node: Tree): Option[Tree]

  /**
    * find the best child of a parent node based on the selection policy of this MCTS algorithm
    * @param node the parent node
    * @param Cp the exploration coefficient, which is user-set, or is 0 if we are seeking the best choice based on our generated knowledge
    * @return the best child, based on the evaluate function provided by the user
    */
  protected def bestChild(node: Tree, Cp: Double): Option[Tree]

  //////// implemented members //////////

  final protected def hasUnexploredActions: (Tree) => Boolean = Utilities.hasUnexploredActions[S,A,Tree](generatePossibleActions)


  /**
    * run this Monte Carlo Tree Search
    * @return the tree at the end of the search
    */
  final def run(root: Tree = startNode(startState)): Tree = {
    while (terminationCriterion.terminationCheck[S,A,Tree](root)) {
      val v_t = treePolicy(root, Cp)
      val ∆ = defaultPolicy(v_t)
      backup(v_t, ∆)
    }
    root
  }

  /**
    * find the path of best moves through the generated tree
    * @return the sequence of best moves through the game
    */
  final def bestGame: (Tree) => Seq[A] = Utilities.bestGame[S,A,Tree](bestChild)
}