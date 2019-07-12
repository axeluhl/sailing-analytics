package com.sap.sailing.windestimation.aggregator.msthmm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.sailing.windestimation.aggregator.hmm.GraphLevelInference;
import com.sap.sailing.windestimation.aggregator.hmm.GraphNode;
import com.sap.sailing.windestimation.aggregator.hmm.IntersectedWindRange;
import com.sap.sailing.windestimation.aggregator.hmm.WindCourseRange.CombinationModeOnViolation;
import com.sap.sailing.windestimation.aggregator.msthmm.MstManeuverGraphGenerator.MstManeuverGraphComponents;
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
    public List<GraphLevelInference> getBestNodes(MstManeuverGraphComponents graphComponents) {
        Map<MstGraphLevel, MstBestPathsPerLevel> bestPathsPerLevel = new HashMap<>();
        // trace back to the graphComponents' root starting at the leaves
        for (MstGraphLevel currentLevel : graphComponents.getLeaves()) {
            MstBestPathsPerLevel bestPathsUntilLevel = new MstBestPathsPerLevel(currentLevel);
            // initialize bestPathsUntilLevel using the confidences of the maneuver classifications
            // in the leaf node:
            for (GraphNode currentNode : currentLevel.getLevelNodes()) {
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
        List<GraphLevelInference> inference = inferShortestPath(graphComponents.getRoot(), bestPathsPerLevel);
        return inference;
    }

    private List<GraphLevelInference> inferShortestPath(MstGraphLevel root,
            Map<MstGraphLevel, MstBestPathsPerLevel> bestPathsPerLevel) {
        MstBestPathsPerLevel bestPathsUntilLevel = bestPathsPerLevel.get(root);
        double maxProbability = 0;
        GraphNode bestRootNode = null;
        List<GraphLevelInference> result = new ArrayList<>();
        for (GraphNode lastNode : root.getLevelNodes()) {
            double probability = bestPathsUntilLevel.getBestPreviousNodeInfo(lastNode).getProbabilityFromStart();
            if (maxProbability < probability) {
                maxProbability = probability;
                bestRootNode = lastNode;
            }
        }
        double confidence = maxProbability;
        GraphLevelInference entry = new GraphLevelInference(root, bestRootNode,
                confidence * bestRootNode.getConfidence());
        result.add(entry);
        inferShortestPath(root, bestRootNode, confidence, result, bestPathsPerLevel);
        return result;
    }

    private void inferShortestPath(MstGraphLevel lastLevel, GraphNode lastNode, double confidence,
            List<GraphLevelInference> result, Map<MstGraphLevel, MstBestPathsPerLevel> bestPathsPerLevel) {
        if (lastLevel.getChildren().isEmpty()) {
            return;
        }
        MstBestPathsPerLevel lastLevelInfo = bestPathsPerLevel.get(lastLevel);
        MstBestManeuverNodeInfo lastNodeInfo = lastLevelInfo.getBestPreviousNodeInfo(lastNode);
        for (Pair<MstGraphLevel, GraphNode> child : lastNodeInfo.getPreviousGraphLevelsWithBestPreviousNodes()) {
            MstGraphLevel currentLevel = child.getA();
            GraphNode currentNode = child.getB();
            while (currentLevel != null) {
                GraphLevelInference entry = new GraphLevelInference(currentLevel, currentNode,
                        confidence * currentNode.getConfidence());
                result.add(entry);
                if (currentLevel.getChildren().size() == 1) {
                    MstBestPathsPerLevel currentLevelInfo = bestPathsPerLevel.get(currentLevel);
                    MstBestManeuverNodeInfo currentNodeInfo = currentLevelInfo.getBestPreviousNodeInfo(currentNode);
                    currentNode = currentNodeInfo.getPreviousGraphLevelsWithBestPreviousNodes().get(0).getB();
                    currentLevel = currentLevel.getChildren().get(0);
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
        for (GraphNode currentNode : currentLevel.getLevelNodes()) {
            List<Pair<MstGraphLevel, GraphNode>> currentNodeBestPreviousNodes = new ArrayList<>();
            double currentNodeProbabilityFromStart = currentNode.getConfidence();
            IntersectedWindRange finalBestIntersectedWindRange = null;
            for (MstBestPathsPerLevel bestPathsUntilPreviousLevel : bestPathsUntilPreviousLevels) {
                double bestProbabilityFromStart = 0;
                MstGraphLevel previousLevel = bestPathsUntilPreviousLevel.getCurrentLevel();
                GraphNode bestPreviousNode = null;
                IntersectedWindRange bestIntersectedWindRange = null;
                for (GraphNode previousNode : previousLevel.getLevelNodes()) {
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
