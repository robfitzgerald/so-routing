package cse.fitzgero.sorouting.algorithm.local.selection

import java.time.{Instant, LocalTime}

import scala.annotation.tailrec
import scala.collection.{GenIterable, GenMap, GenSeq, GenSet}
import scala.util.Random

import cse.fitzgero.graph.algorithm.GraphAlgorithm
import cse.fitzgero.sorouting.algorithm.local.ksp.KSPLocalDijkstrasAlgorithm
import cse.fitzgero.sorouting.model.path.SORoutingPathSegment
import cse.fitzgero.sorouting.model.roadnetwork.local.{LocalGraph, LocalODPair}

// The MCTS UTC Algorithm
// Aka The Monte Carlo Tree Search Upper Confidence Bounds for Trees algorithm
// taken from Cameron B. Browne et. al., "A Survey of Monte Carlo Tree Search Methods", IEEE Trans. on Comp. Itl. and AI in Games, v4 n1, March 2012.
//
// function UCTSearch(s_0)
//   create root node v_0 with state s_0   (the empty set)
//   while within computational budget
//     v_l <- treePolicy(v_0)
//     ∆ <- defaultPolicy(s(v_l))
//     backup(v_l, ∆)
//   return a(bestChild(v_0, 0))
//
// function treePolicy(v)
//   while v is nonTerminal
//     if v not fully expanded
//       return expand(v)
//     else
//       v <- bestChild(v, Cp)
//
// function expand(v)
//   choose a ∈ untried actions from A(s(v))
//   add a new child v' to v
//     with s(v') = f(s(v), a)
//     and a(v') = a
//   return v'
//
// function bestChild(v,c)
//   return UTC()  // see paper. it's the function that weighs exploration and exploitation.
//
// function defaultPolicy(s)
//   while s is non-terminal
//     choose a ∈ A(s) uniformly at random
//     s <- f(s, a)
//   return reward for state s
//
// function backup(v,∆)
//   while v is not null
//     N(v) <- N(v) + 1
//     Q(v) <- Q(v) + ∆(v,p)
//     v <- parent of v

// possible default policies
// - cost addition less than strict threshold ∀ links under simulation
// - harmful congestion effects not increased (on all or some % of edges impacted)
// - calculate upper bound on network cost and compare by percentage
// - uncongested edges remain uncongested; congested edges only increase by a bounded delta / % increase


object SelectionLocalMCTSAlgorithm extends GraphAlgorithm {
  override type VertexId = KSPLocalDijkstrasAlgorithm.VertexId
  override type EdgeId = KSPLocalDijkstrasAlgorithm.EdgeId
  override type Graph = KSPLocalDijkstrasAlgorithm.Graph
  type Path = List[SORoutingPathSegment]
  override type AlgorithmRequest = GenMap[LocalODPair, GenSeq[Path]]
  override type AlgorithmConfig = {
    def coefficientCp: Double // 0 means flat mon
    def congestionRatioThreshold: Double
    def computationalLimit: Long // ms.
  }
  override type AlgorithmResult = GenMap[LocalODPair, Path]

  val DefaultCp: Double = 1.2D
  val DefaultCongestionRatioThreshold: Double = 1.5D
  val DefaultComputationalLimit = 60000 // 60 seconds
  val random: Random = new Random

  /**
    * UCT-based MCTS solving a Multi-Armed Bandit Problem over the multiset of alternate paths
    * @param graph underlying graph structure
    * @param request user-defined request object
    * @param config user-defined config object
    * @return a user-defined result object
    */
  override def runAlgorithm(graph: LocalGraph, request: AlgorithmRequest, config: Option[AlgorithmConfig]): Option[AlgorithmResult] = {
    if (request.isEmpty) None
    else {
      val Cp: Double = config match {
        case Some(conf) => conf.coefficientCp
        case None => DefaultCp
      }

      val CongestionRatioThreshold: Double = config match {
        case Some(conf) => conf.congestionRatioThreshold
        case None => DefaultCongestionRatioThreshold
      }

      val ComputationalLimit: Long = config match {
        case Some(conf) => conf.computationalLimit
        case None => DefaultComputationalLimit
      }

      val endTime: Long = Instant.now.toEpochMilli + ComputationalLimit
      def withinComputationalLimit: Boolean = Instant.now.toEpochMilli < endTime

      // the global list of alternate paths, where each alternate has it's Tag for back-tracking
      val globalAlts: GenMap[PersonID, GenMap[Tag, Seq[String]]] =
        request.map {
          od =>
            val alts = od._2
              .zipWithIndex
              .map(tup => (Tag(od._1.id, tup._2), tup._1.map(_.edgeId)))
              .toMap
            (od._1.id, alts)
        }

      val globalTags: Seq[MCTSAltPath] =
        globalAlts
          .flatMap {
            person =>
              person._2.map {
                p =>
                  MCTSAltPath(p._1, p._2)
              }
          }.toList

      // backtrack from a tag to the associated od pair
      val untag: GenMap[Tag, (LocalODPair, Path)] =
        request.flatMap {
          od =>
            od._2
              .zipWithIndex
              .map {
                tup => (Tag(od._1.id, tup._2), (od._1, tup._1))
              }
        }

      // backtrack from a tag to the associated (Tag, EdgeList)
      def getAltPathFrom(tag: Tag): MCTSAltPath = {
        val edges = globalAlts(tag.personId)(tag)
        MCTSAltPath(tag, edges)
      }

      /**
        * gives a reward value only if none of the costs increase by CongestionRatioThreshold
        * @param costs the edges paired with their starting costs and the costs from this group
        * @return 1 or 0
        */
      def basicEvaluation(costs: List[(String, Double, Double)]): Int = {
        val testResult = costs.forall {
          cost =>
            (cost._3 / cost._2) <= CongestionRatioThreshold
        }
        if (testResult) 1 else 0
      }

      /**
        * gives a reward value if the average of the costs do not exceed CongestionRatioThreshold
        * @param costs the edges paired with their starting costs and the costs from this group
        * @return 1 or 0
        */
      def meanCostDiff(costs: List[(String, Double, Double)]): Int = {
        if (costs.isEmpty) {
          1
        } else {
          val avgCostDiff = costs.map {
            tuple =>
              tuple._3 / tuple._2
          }.sum / costs.size
          val testResult = avgCostDiff <= CongestionRatioThreshold
          if (testResult) 1 else 0
        }
      }

      // a helper that selects a random child in the Expand step
      def selectionMethod(children: GenMap[Tag, () => Option[MCTSTreeNode]]): MCTSAltPath = {
        val remainingAlts = children.filter(_._2().isEmpty).keys.map(getAltPathFrom).toVector
        remainingAlts(random.nextInt(remainingAlts.size))
      }

      /**
        * main method for running MCTS
        * @return a solution
        */
      def uctSearch(): Option[AlgorithmResult] = {
        val rootChildren: GenMap[Tag, Option[MCTSTreeNode]] =
          for {
            person <- globalAlts
            alt <- person._2
          } yield (alt._1, None)
        val rootChildrenClosure: GenMap[Tag, () => Option[MCTSTreeNode]] =
          rootChildren
            .mapValues(node => () => node)


        val root: MCTSTreeNode =
          MCTSTreeNode(
            visits = 0,
            reward = 0,
            state = Seq(),
            children = Some(rootChildrenClosure),
            action = None,
            parent = () => None
          )

        def remainingTags(usedTags: Seq[MCTSAltPath]): Seq[MCTSAltPath] = {
          val usedIds: Set[PersonID] = usedTags.map(_.tag.personId).toSet
          globalTags.filter {
            alt => !usedIds(alt.tag.personId)
          }
        }

        // TODO: a smarter computation bounds than (1 to 1000)
        //      val finalTree: MCTSTreeNode =
        while (withinComputationalLimit) {
          val v_t = treePolicy(root, Cp, remainingTags, selectionMethod)
          val ∆ = defaultPolicy(graph, v_t, globalAlts, meanCostDiff)
          backup(v_t, ∆)
        }
        //        (1 to 50000).foldLeft(originalRoot)((root, n) => {
        //          val v_t = treePolicy(root, Cp, remainingTags, selectionMethod)
        //          val ∆ = defaultPolicy(graph, v_t, globalAlts, basicEvaluation)
        //          backup(v_t, ∆)
        //        })

        //      println(root.toString)

        val result = bestPath(root).map {
          tag =>
            untag(tag)
        }

        Some(result.toMap)
      }
      // do work
      uctSearch()
    }
  }

  /**
    * simulates the reward of the current configuration found at v.state
    * @param graph the underlying graph structure
    * @param v the current node we are evaluating from
    * @param globalAlts the complete list of available alternate paths
    * @param evaluate a special user-defined function that evaluates a given outcome
    * @return {1|0} the reward function
    */
  def defaultPolicy(graph: LocalGraph, v: MCTSTreeNode, globalAlts: GenMap[PersonID, GenMap[Tag, Seq[String]]], evaluate: (List[(String, Double, Double)]) => Int): Int = {
    // identify what persons remain.
    val personsRepresented: Set[PersonID] = v.state.map(_.tag.personId).toSet
    val remainingPersons = globalAlts.filter(person => !personsRepresented(person._1))

    // add selections of theirs randomly
    val setToEvaluate: GenIterable[MCTSAltPath] =
      remainingPersons.map{
      person =>
        val alts = person._2.toVector
        val selectedAlt = alts(random.nextInt(alts.size))
        MCTSAltPath(selectedAlt._1, selectedAlt._2)
    } ++ v.state

    // evaluate the cost
    val edgesAndFlows: GenMap[String, Int] =
      setToEvaluate.flatMap(_.edges).groupBy(identity).mapValues(_.size)

    val evaluatedCosts: List[(String, Double, Double)] =
      edgesAndFlows.flatMap {
        e =>
          graph.edgeById(e._1) match {
            case None => None
            case Some(edge) =>
              edge.attribute.linkCostFlow match {
                case None => None
                case Some(previousCost) =>
                  edge.attribute.costFlow(e._2) match {
                    case None => None
                    case Some(updatedCost) =>
                      Some(e._1, previousCost, updatedCost)
                  }
              }
          }
      }.toList

    evaluate(evaluatedCosts)
  }


  /**
    * the step where we either expand an unexplored node or we try running a leaf node we have found before
    * @param v a node in the tree as we traverse from the root toward a leaf
    * @param Cp a coefficient that is used by the bestChild method for balancing exploration and exploitation of the graph
    * @param remainingTags a function that gives us the remaining tags not yet explored by v
    * @param selectionMethod a user-defined function for selecting an unexplored node to expand
    * @return
    */
  @tailrec
  def treePolicy(
    v: MCTSTreeNode,
    Cp: Double,
    remainingTags: (Seq[MCTSAltPath]) => Seq[MCTSAltPath],
    selectionMethod: (GenMap[Tag, () => Option[MCTSTreeNode]]) => MCTSAltPath): MCTSTreeNode = {
    v.children match {
      case None =>
        // terminal case: return v
        v
      case Some(childrenOfParent) =>
        val someUnexplored: Boolean = childrenOfParent.exists(_._2().isEmpty)
        if (someUnexplored) {
          // not fully expanded case
          expand(v, remainingTags, selectionMethod)
        } else {
          MCTSTreeNode.bestChild(v, Cp) match {
            case None =>
              // terminal, so return this parent node
              v
            case Some(bestChild) =>
              // recurse on the best child to continue tree search
              treePolicy(bestChild, Cp, remainingTags, selectionMethod)
          }
        }
    }
  }


  /**
    * MCTS Expand function chooses a child to expand via a provided selection method and attaches that new node to the tree
    * @param v the parent node we are attaching to
    * @param remainingTags a function that takes the child's state and gives us whatever unexplored tags remain
    * @return the updated parent and the new child as a tuple
    */
  def expand(
    v: MCTSTreeNode,
    remainingTags: (Seq[MCTSAltPath]) => Seq[MCTSAltPath],
    selectionMethod: (GenMap[Tag, () => Option[MCTSTreeNode]]) => MCTSAltPath): MCTSTreeNode = {

    v.children match {
      case None =>
        // expand called on terminal leaf.
        v
      case Some(childrenOfParent) =>
        val selectedChild: MCTSAltPath = selectionMethod(childrenOfParent)
        val newState: Seq[MCTSAltPath] = v.state :+ selectedChild
        val scaffoldGrandChildren: Option[Map[Tag, () => Option[MCTSTreeNode]]] =
          remainingTags(newState) match {
            case Nil => None
            case xs: Seq[MCTSAltPath] => Some {
              xs.map {
                remaining => (remaining.tag, () => None)
              }.toMap
            }
          }

        val newChild: MCTSTreeNode =
          MCTSTreeNode(
            0,
            0,
            newState,
            scaffoldGrandChildren,
            Some(selectedChild.tag),
            () => Some(v)
          )

        v.addChild(selectedChild.tag, newChild)

        newChild
    }
  }

  /**
    * a backpropogation method for updating the entire branch leading to this tree with the reward result
    * @param v a node in the traversal from leaf to root
    * @param delta the value we are adding to reward for nodes along this traversal
    * @return the root node
    */
  @tailrec
  def backup(v: MCTSTreeNode, delta: Int): MCTSTreeNode =
    v.parent() match {
      case None =>
        // root node. update and return
        v.updateReward(delta)
      case Some(parent) =>
        // v has a parent, so we want to update v and recurse on parent
        val updated = v.updateReward(delta)
        backup(parent, delta)
//        parent.children match {
//          case None =>
//            // somehow v is a child of parent, but parent isn't a parent of v
//            // TODO: some kind of tree recovery here
//            backup(parent, delta)
//          case Some(children) =>
//            v.action match {
//              case None =>
//                // somehow we have a relation between v and its parent, but the action is undefined. tree recovery?
//                backup(parent, delta)
//              case Some(tag) =>
//                val relationUpdated = v.copy(children = Some(children.updated(tag, () => Some(updated))))
//                backup(relationUpdated, delta)
//            }
//        }
    }

  @tailrec
  def bestPath(v: MCTSTreeNode, solution: Seq[Tag] = Seq()): Seq[Tag] = {
    v.children match {
      case None =>
        // hit the leaf. finish recurse and return
        v.action match {
          case None =>
            Seq()
          case Some(tag) =>
            tag +: solution
        }
      case Some(childrenExist) =>
        MCTSTreeNode.bestChild(v) match {
          case None => solution // a partial solution
          case Some(bestChild) =>
            bestChild.action match {
              case None => Seq()
              case Some(tag) =>
                bestPath(bestChild, tag +: solution)
            }
        }
    }
  }

  type PersonID = String

  case class Tag(personId: PersonID, alternate: Int)

  case class MCTSAltPath(tag: Tag, edges: Seq[String])

  // MCTSTreeNode must be a mutable tree structure
  // https://stackoverflow.com/questions/8042356/why-no-immutable-double-linked-list-in-scala-collections
  class MCTSTreeNode(
    var visits: Int,
    var reward: Int,
    val state: Seq[MCTSAltPath],
    var children: Option[GenMap[Tag, () => Option[MCTSTreeNode]]],
    val action: Option[Tag],
    val parent: () => Option[MCTSTreeNode]) {

    // mutable operations
    def updateReward(rewardUpdate: Int): MCTSTreeNode = {
      reward = reward + rewardUpdate
      visits += 1
      this
    }

    def addChild(tag: Tag, node: MCTSTreeNode): MCTSTreeNode = {
      children match {
        case None => this
        case Some(childrenToUpdate) =>
          children = Some(childrenToUpdate.updated(tag, () => Some(node)))
          this
      }
    }
    // everything else is handled by mutability

//    /**
//      * update reward and visit data points during backtracking function
//      * @param reward the reward resulted from the simulation
//      * @return this node with updated statistics
//      */
//    def update(reward: Int): MCTSTreeNode =
//      this.copy(visits = this.visits + 1, reward = this.reward + reward)

    /**
      * Upper Confidence Bound For Trees
      * @param Cp Exploration Coefficient. Cp > 0 will give some weight to exploration. 0 ignores exploration.
      * @param parentVisits the number of visits for the parent node
      * @return
      */
    def evaluateUCT(Cp: Double, parentVisits: Int): Double = {
      val exploitation: Double = reward / visits
      val exploration: Double =
        if (Cp == 0)
          0D
      else if (visits == 0)
          Double.MaxValue
        else
          2 * Cp * math.sqrt(
            (2.0D * math.log(parentVisits)) /
              visits
          )


      exploitation + exploration
    }

//    override def toString: String = {
//      def depth: String = (for { i <- state.indices } yield "-").mkString("")
//      children match {
//        case None => action match {
//          case None => ""
//          case Some(tag) => s"$depth${tag.personId}#${tag.alternate} - $visits visits, $reward reward\n"
//        }
//        case Some(childrenToPrint) =>
//          childrenToPrint.map {
//            child =>
//              child._2() match {
//                case None => ""
//                case Some(childNode) =>
//                  action match {
//                    case None =>
//                      s"${depth}root - $visits visits, $reward reward\n${childNode.toString}"
//                    case Some(tag) =>
//                      s"$depth${tag.personId}#${tag.alternate} - $visits visits, $reward reward\n${childNode.toString}"
//                  }
//              }
//          }.mkString("")
//      }
//    }

    override def toString: String = {
      def depth: String = (for { i <- state.indices } yield "-").mkString("")
      parent() match {
        case None => // root node
          children match {
            case None => // root with no children
              s"root - $visits visits, $reward reward\n"
            case Some(childrenToPrint) =>
              val recurseResult: String =
                childrenToPrint
                  .map {
                    child =>
                      child._2() match {
                        case None => "" // unexplored child
                        case Some(childToPrint) =>
                          childToPrint.toString
                      }
                  }.mkString("")
              s"root - $visits visits, $reward reward\n$recurseResult"
          }
        case Some(_p) =>
          val tagData: String = action match {
            case None => ""
            case Some(tag) => s"${tag.personId}#${tag.alternate}"
          }
          children match {
            case None => // root with no children
              s"$depth$tagData - $visits visits, $reward reward\n"
            case Some(childrenToPrint) =>
              val recurseResult: String =
                childrenToPrint.map {
                  child =>
                    child._2() match {
                      case None => "" // unexplored child
                      case Some(childToPrint) =>
                        childToPrint.toString
                    }
                }.mkString("")
              s"$depth$tagData - $visits visits, $reward reward\n$recurseResult"
          }
      }
    }
  }

  object MCTSTreeNode {

    def apply(visits: Int, reward: Int, state: Seq[MCTSAltPath], children: Option[GenMap[Tag, () => Option[MCTSTreeNode]]], action: Option[Tag], parent: () => Option[MCTSTreeNode]): MCTSTreeNode =
      new MCTSTreeNode(visits,reward,state,children,action,parent)

    /**
      * find the best child of a parent node based on the selection policy of this MCTS algorithm
      * @param parent the parent node
      * @param Cp a coefficient that emphasizes exploration. By default, set to zero (ie no exploration)
      * @return the best child, or None if children was empty or if parent is a terminal element
      */
    def bestChild(parent: MCTSTreeNode, Cp: Double = 0D): Option[MCTSTreeNode] = {
      parent.children match {
        case None => None
        case Some(childrenOfParent) =>
          val children = childrenOfParent.flatMap(_._2())
          if (children.isEmpty) {
            None
          } else {
            val bestChild: MCTSTreeNode =
              children
                .map {
                  child =>
                    (child.evaluateUCT(Cp, parent.visits), child)
                }
                .maxBy(_._1)
                ._2

            Some(bestChild)
          }
      }
    }

    /**
      * finds the currently best solution or sub-solution
      * @param currentNode should be called with the root node
      * @return the best solution or sub-solution that was found
      */
    @tailrec
    def finalTraversal(currentNode: MCTSTreeNode): Seq[MCTSAltPath] = {
      bestChild(currentNode) match {
        case None => currentNode.state
        case Some(child) => finalTraversal(child)
      }
    }
  }
}
