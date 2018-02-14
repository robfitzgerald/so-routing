package cse.fitzgero.mcts

import cse.fitzgero.mcts.core._

trait MonteCarloTreeSearch[S,A] {

  // core operations. provided by a variant in the MCTS library
  protected def defaultPolicy(node: MonteCarloTree[S,A]): Double
  protected def treePolicy(node: MonteCarloTree[S,A], Cp: Double): MonteCarloTree[S,A]
  protected def backup(node: MonteCarloTree[S,A], delta: Double): MonteCarloTree[S,A]
  protected def expand(node: MonteCarloTree[S,A]): Option[MonteCarloTree[S,A]]
  protected def bestChild(node: MonteCarloTree[S,A], Cp: Double): Option[MonteCarloTree[S,A]]

  // utility operations. provided by the MCTS library
  protected def samplingMethod: SamplingFunction
  protected def terminationCriterion: TerminationCriterion
  protected def actionSelection: ActionSelection[S,A]
  protected def random: RandomGenerator
  final protected def hasUnexploredActions: (MonteCarloTree[S,A]) => Boolean = Utilities.hasUnexploredActions[S,A](generatePossibleActions)

  // domain and user-provided operations. to be implemented by the user
  def generatePossibleActions(state: S): Seq[A]
  def applyAction(state: S, action: A): S
  def evaluate(state: S): Double
  def stateIsNonTerminal(state: S): Boolean
  def selectAction(actions: Seq[A]): Option[A]
  def startState: S
  def Cp: Double

  /**
    * run this Monte Carlo Tree Search
    * @return the tree at the end of the search
    */
  final def run(root: MonteCarloTree[S,A] = MonteCarloTree[S,A](state = startState)): MonteCarloTree[S,A] = {

    while (terminationCriterion.terminationCheck(root)) {
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
  final def bestGame: (MonteCarloTree[S,A]) => Seq[A] = Utilities.bestGame[S,A](bestChild)
}

