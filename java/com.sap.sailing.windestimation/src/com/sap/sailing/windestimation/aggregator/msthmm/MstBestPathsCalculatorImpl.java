package com.sap.sailing.windestimation.aggregator.msthmm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.sailing.windestimation.aggregator.graph.DijkstraShortestPathFinderImpl;
import com.sap.sailing.windestimation.aggregator.graph.DijsktraShortestPathFinder;
import com.sap.sailing.windestimation.aggregator.graph.InnerGraphSuccessorSupplier;
import com.sap.sailing.windestimation.aggregator.graph.Tree;
import com.sap.sailing.windestimation.aggregator.hmm.GraphLevelInference;
import com.sap.sailing.windestimation.aggregator.hmm.GraphNode;
import com.sap.sailing.windestimation.aggregator.hmm.IntersectedWindRange;
import com.sap.sailing.windestimation.aggregator.hmm.WindCourseRange;
import com.sap.sailing.windestimation.aggregator.hmm.WindCourseRange.CombinationModeOnViolation;
import com.sap.sailing.windestimation.aggregator.msthmm.MstManeuverGraphGenerator.MstManeuverGraphComponents;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;

/**
 * Infers best path within Minimum Spanning Tree (MST) using an adapted variant of Viterbi for conventional HMM models
 * which allows to label each provided maneuver with its most suitable maneuver type.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class MstBestPathsCalculatorImpl implements MstBestPathsCalculator {

    private final MstGraphNodeTransitionProbabilitiesCalculator transitionProbabilitiesCalculator;

    public MstBestPathsCalculatorImpl(MstGraphNodeTransitionProbabilitiesCalculator transitionProbabilitiesCalculator) {
        this.transitionProbabilitiesCalculator = transitionProbabilitiesCalculator;
    }

    @Override
    public MstGraphNodeTransitionProbabilitiesCalculator getTransitionProbabilitiesCalculator() {
        return transitionProbabilitiesCalculator;
    }

    @Override
    public List<GraphLevelInference<MstGraphLevel>> getBestNodes(MstManeuverGraphComponents graphComponents) {
        Map<MstGraphLevel, MstBestPathsPerLevel> bestPathsPerLevel = new HashMap<>();
        // trace back to the graphComponents' root starting at the leaves
        for (MstGraphLevel currentLevel : graphComponents.getLeaves()) {
            MstBestPathsPerLevel bestPathsUntilLevel = new MstBestPathsPerLevel(currentLevel);
            // initialize bestPathsUntilLevel using the confidences of the maneuver classifications
            // in the leaf node:
            for (GraphNode<MstGraphLevel> currentNode : currentLevel.getLevelNodes()) {
                double probability = currentNode.getConfidence();
                bestPathsUntilLevel.addBestPreviousNodeInfo(currentNode, null, probability,
                        currentNode.getValidWindRange().toIntersected());
            }
            bestPathsPerLevel.put(currentLevel, bestPathsUntilLevel);
            while ((currentLevel = currentLevel.getParent()) != null) {
                // computeBestPathsToNextLevel will return immediately if not all children have been
                // evaluated yet and will return false in this case;
                // this leads to something like a "breadth-first search" which first
                // evaluates all children of a parent, and only after evaluating the last child can
                // progress to the common parent.
                // Ascending further after the computeBestPathsToNextLevel for currentLevel has been aborted
                // doesn't seem to make much sense because for all parents up to the root not all their children
                // have been evaluated, so all those calls again are aborted.
                if (!computeBestPathsToNextLevel(currentLevel, bestPathsPerLevel)) {
                    break;
                }
            }
        }
        List<GraphLevelInference<MstGraphLevel>> inference = inferShortestPath(graphComponents.getRoot(), bestPathsPerLevel);
        return inference;
    }
    
    /**
     * The {@code graphComponents} represent a {@link Tree} of {@link MstGraphLevel} nodes, each of which contains a
     * {@link GraphNode} per possible maneuver classification, e.g., one for the "tack" hypothesis, one for the "gybe"
     * hypothesis, and so on. These inner nodes are virtually connected with the nodes of the parent and child
     * {@link MstGraphLevel} objects. These virtual connections are rated using the
     * {@link MstGraphNodeTransitionProbabilitiesCalculator} which takes pair of {@link GraphNode}s and judges the
     * likelihood of the true wind direction (TWD) change implied by their hypothetical classifications, considering
     * their time and space distance from each other.
     * <p>
     * 
     * A path in the tree from a leaf to the root hence forms a graph over the {@link GraphNode}s contained in this
     * path, with the virtual edges connecting the nodes of adjacent overarching tree nodes. We apply a
     * {@link DijkstraShortestPathFinder Dijkstra Shortest Path algorithm} to the inner graph produced by the path from
     * each leaf to the root, equating "short" with "high quality" and considering the quality of a path to be the
     * product of the {@link GraphNode}s' {@link GraphNode#getQuality() quality} and the transition probability computed
     * by the {@link MstGraphNodeTransitionProbabilitiesCalculator}. The {@link InnerGraphSuccessorSupplier} also adds
     * artificial root and leaf nodes to the inner graph to have a single start and end node.
     * <p>
     * 
     * Solving the "shortest path" problem for each leaf-to-root path gives as many picks of a "best" {@link GraphNode}
     * per {@link MstGraphLevel} as there are paths from leaves to the tree root passing through that
     * {@link MstGraphLevel} tree node. In particular, there will be exactly one such pick for all leaves, and as many
     * for the root as there are leaves.
     * <p>
     * 
     * If several "best picks" exist for a {@link MstGraphLevel} tree node, they may not be consistent. In case there is
     * a tie (e.g., one path picking "tack" and one path picking "gybe"), no selection is made. If there is a majority,
     * the majority's decision is selected, and the ratio of paths with the winning selection over the total number of
     * paths through that node, multiplied by the {@link DijsktraShortestPathFinder.Result#getPathQuality() overall path
     * probability} is used to scale the maneuver classification's confidence.
     * 
     * @return a set of {@link GraphLevelInference} objects for a subset of the {@link MstGraphLevel} objects in the
     *         graph, each combined with the {@link GraphLevelInference#getGraphNode() graph node} selected within the
     *         {@link GraphLevelInference#getGraphLevel() overarching tree node} and the respective
     *         {@link GraphLevelInference#getConfidence() confidence}. For some nodes of the overarching tree no
     *         conclusion may have been reached for a valid classification which is why this may cover only a true
     *         subset of the nodes of the overarching tree.
     */
    public List<GraphLevelInference<MstGraphLevel>> getBestNodes2(MstManeuverGraphComponents graphComponents) {
        for (MstGraphLevel leaf : graphComponents.getLeaves()) {
            final DijsktraShortestPathFinder<GraphNode<MstGraphLevel>> dijsktraShortestPathFinder = new DijkstraShortestPathFinderImpl<>();
            final InnerGraphSuccessorSupplier<GraphNode<MstGraphLevel>, MstGraphLevel> innerGraphSuccessorSupplier =
                    new InnerGraphSuccessorSupplier<GraphNode<MstGraphLevel>, MstGraphLevel>(graphComponents,
                    // supplier for artificial nodes; always full confidence and full possible wind course range
                    (final String name)->new GraphNode<MstGraphLevel>(/* maneuverType */ null, /* tackAfter */ null, new WindCourseRange(0, 360), /* confidence */ 1.0, /* indexInLevel */ 0, /* graphLevel */ null) {
                        @Override
                        public String toString() {
                            return name;
                        }
            });
            dijsktraShortestPathFinder.getShortestPath(innerGraphSuccessorSupplier.getArtificialLeaf(leaf), innerGraphSuccessorSupplier.getArtificialRoot(),
                    innerGraphSuccessorSupplier, (n1, n2)->0.0 /* TODO */);
        }
        return null; // TODO
    }

    private List<GraphLevelInference<MstGraphLevel>> inferShortestPath(MstGraphLevel root,
            Map<MstGraphLevel, MstBestPathsPerLevel> bestPathsPerLevel) {
        MstBestPathsPerLevel bestPathsUntilLevel = bestPathsPerLevel.get(root);
        double maxProbability = 0;
        GraphNode<MstGraphLevel> bestRootNode = null;
        List<GraphLevelInference<MstGraphLevel>> result = new ArrayList<>();
        for (GraphNode<MstGraphLevel> lastNode : root.getLevelNodes()) {
            double probability = bestPathsUntilLevel.getBestPreviousNodeInfo(lastNode).getProbabilityFromStart();
            if (maxProbability < probability) {
                maxProbability = probability;
                bestRootNode = lastNode;
            }
        }
        double confidence = maxProbability;
        GraphLevelInference<MstGraphLevel> entry = new GraphLevelInference<>(root, bestRootNode,
                confidence * bestRootNode.getConfidence());
        result.add(entry);
        inferShortestPath(root, bestRootNode, confidence, result, bestPathsPerLevel);
        return result;
    }

    private void inferShortestPath(MstGraphLevel lastLevel, GraphNode<MstGraphLevel> lastNode, double confidence,
            List<GraphLevelInference<MstGraphLevel>> result, Map<MstGraphLevel, MstBestPathsPerLevel> bestPathsPerLevel) {
        if (Util.isEmpty(lastLevel.getChildren())) {
            return;
        }
        MstBestPathsPerLevel lastLevelInfo = bestPathsPerLevel.get(lastLevel);
        MstBestManeuverNodeInfo lastNodeInfo = lastLevelInfo.getBestPreviousNodeInfo(lastNode);
        for (Pair<MstGraphLevel, GraphNode<MstGraphLevel>> child : lastNodeInfo.getPreviousGraphLevelsWithBestPreviousNodes()) {
            MstGraphLevel currentLevel = child.getA();
            GraphNode<MstGraphLevel> currentNode = child.getB();
            while (currentLevel != null) {
                GraphLevelInference<MstGraphLevel> entry = new GraphLevelInference<>(currentLevel, currentNode,
                        confidence * currentNode.getConfidence());
                result.add(entry);
                if (Util.size(currentLevel.getChildren()) == 1) {
                    MstBestPathsPerLevel currentLevelInfo = bestPathsPerLevel.get(currentLevel);
                    MstBestManeuverNodeInfo currentNodeInfo = currentLevelInfo.getBestPreviousNodeInfo(currentNode);
                    currentNode = currentNodeInfo.getPreviousGraphLevelsWithBestPreviousNodes().get(0).getB();
                    currentLevel = currentLevel.getChildren().iterator().next();
                } else {
                    inferShortestPath(currentLevel, currentNode, confidence, result, bestPathsPerLevel);
                    currentLevel = null;
                }
            }
        }
    }

    /**
     * @return {@code false} if no update was computed for the {@code currentLevel}; {@code true} otherwise. Callers can
     *         use this information to stop any ascending in the MST towards the root because calls for direct and
     *         transitive parent nodes will abort, too, due to missing information for {@code currentLevel}.
     */
    private boolean computeBestPathsToNextLevel(MstGraphLevel currentLevel,
            Map<MstGraphLevel, MstBestPathsPerLevel> bestPathsPerLevel) {
        // check that all branches until current (joining) node are processed, or else return immediately
        List<MstBestPathsPerLevel> bestPathsUntilPreviousLevels = new ArrayList<>();
        for (MstGraphLevel previousLevel : currentLevel.getChildren()) {
            MstBestPathsPerLevel bestPathsUntilPreviousLevel = bestPathsPerLevel.get(previousLevel);
            if (bestPathsUntilPreviousLevel == null) {
                return false;
            }
            bestPathsUntilPreviousLevels.add(bestPathsUntilPreviousLevel);
        }
        MstBestPathsPerLevel bestPathsUntilLevel = new MstBestPathsPerLevel(currentLevel);
        // the edges connecting the four nodes of the currentLevel to the four nodes of each child level
        // are now analyzed and evaluated. The probability for the path to the currentNode is determined
        // as the product of the probability for the path to the previous node (which already includes
        // the previous node's own classification confidence) and the transition probability to the
        // current node (determined by the TWD delta between the wind range carried through to the previous
        // node and the wind range computed for the current node) and the classification confidence of
        // the current node.
        for (GraphNode<MstGraphLevel> currentNode : currentLevel.getLevelNodes()) {
            List<Pair<MstGraphLevel, GraphNode<MstGraphLevel>>> currentNodeBestPreviousNodes = new ArrayList<>();
            double currentNodeProbabilityFromStart = currentNode.getConfidence();
            IntersectedWindRange finalBestIntersectedWindRange = null;
            for (MstBestPathsPerLevel bestPathsUntilPreviousLevel : bestPathsUntilPreviousLevels) {
                double bestProbabilityFromStart = 0;
                MstGraphLevel previousLevel = bestPathsUntilPreviousLevel.getCurrentLevel();
                GraphNode<MstGraphLevel> bestPreviousNode = null;
                IntersectedWindRange bestIntersectedWindRange = null;
                for (GraphNode<MstGraphLevel> previousNode : previousLevel.getLevelNodes()) {
                    IntersectedWindRange previousNodeIntersectedWindRange = bestPathsUntilPreviousLevel
                            .getBestPreviousNodeInfo(previousNode).getIntersectedWindRange();
                    Pair<IntersectedWindRange, Double> newWindRangeAndProbability = transitionProbabilitiesCalculator
                            .mergeWindRangeAndGetTransitionProbability(currentNode, currentLevel, new PreviousNodeInfo(
                                    previousLevel, previousNode, previousNodeIntersectedWindRange));
                    double transitionProbability = newWindRangeAndProbability.getB();
                    // TODO is normalization across different previousLevel objects correct here? Wouldn't we need to normalize across all children, if we normalize here at all?
                    double probabilityFromStart = bestPathsUntilPreviousLevel
                            .getBestPreviousNodeInfo(previousNode).getProbabilityFromStart() * transitionProbability;
                    if (probabilityFromStart > bestProbabilityFromStart) {
                        bestProbabilityFromStart = probabilityFromStart;
                        bestPreviousNode = previousNode;
                        bestIntersectedWindRange = newWindRangeAndProbability.getA();
                    }
                }
                currentNodeProbabilityFromStart *= bestProbabilityFromStart;
                currentNodeBestPreviousNodes.add(new Pair<>(previousLevel, bestPreviousNode));
                finalBestIntersectedWindRange = finalBestIntersectedWindRange == null ? bestIntersectedWindRange
                        : finalBestIntersectedWindRange.intersect(bestIntersectedWindRange,
                                CombinationModeOnViolation.EXPANSION);
            }
            bestPathsUntilLevel.addBestPreviousNodeInfo(currentNode, currentNodeBestPreviousNodes,
                    currentNodeProbabilityFromStart, finalBestIntersectedWindRange);
        }
        bestPathsPerLevel.put(currentLevel, bestPathsUntilLevel);
        return true;
    }

}
