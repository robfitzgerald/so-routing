package cse.fitzgero.mcts

import cse.fitzgero.mcts.core.{ActionSelection, RandomGenerator, SamplingFunction, TerminationCriterion}

trait MonteCarloTreeSearch[S,A] {

  // core operations. provided by a variant in the MCTS library
  def defaultPolicy(node: MonteCarloTree[S,A]): Double
  def treePolicy(node: MonteCarloTree[S,A]): MonteCarloTree[S,A]
  def backup(node: MonteCarloTree[S,A], delta: Double): MonteCarloTree[S,A]
  def expand(node: MonteCarloTree[S,A]): Option[MonteCarloTree[S,A]]
  def bestChild(node: MonteCarloTree[S,A]): Option[MonteCarloTree[S,A]]

  // utility operations. provided by the MCTS library
  def samplingMethod: SamplingFunction
  def terminationCriterion: TerminationCriterion
  def actionSelection: ActionSelection[S,A]
  def random: RandomGenerator

  // domain and user-provided operations. to be implemented by the user
  def generatePossibleActions(state: S): Seq[A]
  def applyAction(state: S, action: A): S
  def evaluate(state: S): Double
  def stateIsNonTerminal(state: S): Boolean
  def selectAction(monteCarloTree: MonteCarloTree[S,A], actions: Seq[A]): Option[A]

  /**
    * run this Monte Carlo Tree Search
    * @return the tree at the end of the search
    */
  final def run(): MonteCarloTree[S,A] = {

    val root: MonteCarloTree[S,A] = MonteCarloTree[S,A]()

    while (terminationCriterion.terminationCheck(root)) {
      val v_t = treePolicy(root)
      val ∆ = defaultPolicy(v_t)
      backup(v_t, ∆)
    }

    root
  }
}

