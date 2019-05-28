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
 * Infers best path within MST using an adapted variant of Viterbi for conventional HMM models which allows to label
 * each provided maneuver with its most suitable maneuver type.
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
        for (MstGraphLevel currentLevel : graphComponents.getLeafs()) {
            MstBestPathsPerLevel bestPathsUntilLevel = new MstBestPathsPerLevel(currentLevel);
            for (GraphNode currentNode : currentLevel.getLevelNodes()) {
                double probability = currentNode.getConfidence() / currentLevel.getLevelNodes().size();
                bestPathsUntilLevel.addBestPreviousNodeInfo(currentNode,
                        null, probability, currentNode.getValidWindRange().toIntersected());
            }
            bestPathsPerLevel.put(currentLevel, bestPathsUntilLevel);
            while ((currentLevel = currentLevel.getParent()) != null) {
                computeBestPathsToNextLevel(currentLevel, bestPathsPerLevel);
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
        double probabilitiesSum = 0;
        List<GraphLevelInference> result = new ArrayList<>();
        for (GraphNode lastNode : root.getLevelNodes()) {
            double probability = bestPathsUntilLevel.getNormalizedProbabilityToNodeFromStart(lastNode);
            probabilitiesSum += probability;
            if (maxProbability < probability) {
                maxProbability = probability;
                bestRootNode = lastNode;
            }
        }
        double confidence = maxProbability / probabilitiesSum;
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

    private void computeBestPathsToNextLevel(MstGraphLevel nextLevel,
            Map<MstGraphLevel, MstBestPathsPerLevel> bestPathsPerLevel) {
        MstGraphLevel currentLevel = nextLevel;
        // check that all branches until current (joining) node are processed
        List<MstBestPathsPerLevel> bestPathsUntilPreviousLevels = new ArrayList<>();
        for (MstGraphLevel previousLevel : currentLevel.getChildren()) {
            MstBestPathsPerLevel bestPathsUntilPreviousLevel = bestPathsPerLevel.get(previousLevel);
            if (bestPathsUntilPreviousLevel == null) {
                return;
            }
            bestPathsUntilPreviousLevels.add(bestPathsUntilPreviousLevel);
        }
        MstBestPathsPerLevel bestPathsUntilLevel = new MstBestPathsPerLevel(currentLevel);
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
                    double probabilityFromStart = bestPathsUntilPreviousLevel
                            .getNormalizedProbabilityToNodeFromStart(previousNode) * transitionProbability;
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
            bestPathsUntilLevel.addBestPreviousNodeInfo(currentNode,
                    currentNodeBestPreviousNodes, currentNodeProbabilityFromStart, finalBestIntersectedWindRange);
            bestPathsPerLevel.put(currentLevel, bestPathsUntilLevel);
        }
    }

}
