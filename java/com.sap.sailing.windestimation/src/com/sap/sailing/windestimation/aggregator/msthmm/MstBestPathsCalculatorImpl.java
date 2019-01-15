package com.sap.sailing.windestimation.aggregator.msthmm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.sap.sailing.windestimation.aggregator.hmm.GraphLevelInference;
import com.sap.sailing.windestimation.aggregator.hmm.GraphNode;
import com.sap.sailing.windestimation.aggregator.hmm.IntersectedWindRange;
import com.sap.sailing.windestimation.aggregator.hmm.WindCourseRange.CombinationModeOnViolation;
import com.sap.sailing.windestimation.aggregator.msthmm.MstManeuverGraphGenerator.MstManeuverGraphComponents;
import com.sap.sse.common.Util.Pair;

public class MstBestPathsCalculatorImpl implements MstBestPathsCalculator {

    private final MstGraphNodeTransitionProbabilitiesCalculator transitionProbabilitiesCalculator;

    public MstBestPathsCalculatorImpl(MstGraphNodeTransitionProbabilitiesCalculator transitionProbabilitiesCalculator) {
        this.transitionProbabilitiesCalculator = transitionProbabilitiesCalculator;
    }

    @Override
    public MstGraphNodeTransitionProbabilitiesCalculator getTransitionProbabilitiesCalculator() {
        return transitionProbabilitiesCalculator;
    }

    /*
     * public void computeBackwardProbabilities() { GraphLevel currentLevel = lastLevel; BestPathsPerLevel
     * bestPathsUntilLastLevel = bestPathsPerLevel.get(currentLevel); for (GraphNode currentNode :
     * currentLevel.getLevelNodes()) { // double probability = currentNode.getConfidence(); BestManeuverNodeInfo
     * currentNodeInfo = bestPathsUntilLastLevel.getBestPreviousNodeInfo(currentNode); // Pair<IntersectedWindRange,
     * Double> newWindRangeAndProbability = transitionProbabilitiesCalculator //
     * .mergeWindRangeAndGetTransitionProbability(currentNode, currentLevel, currentNodeInfo, nextNode, // nextLevel);
     * currentNodeInfo.setBackwardProbability(1.0); } GraphLevel nextLevel = currentLevel; while ((currentLevel =
     * currentLevel.getPreviousLevel()) != null) { BestPathsPerLevel bestPathsUntilLevel =
     * bestPathsPerLevel.get(currentLevel); BestPathsPerLevel bestPathsUntilNextLevel =
     * bestPathsPerLevel.get(nextLevel); for (GraphNode currentNode : currentLevel.getLevelNodes()) {
     * BestManeuverNodeInfo currentNodeInfo = bestPathsUntilLevel.getBestPreviousNodeInfo(currentNode); double
     * backwardProbability = 0; for (GraphNode nextNode : nextLevel.getLevelNodes()) { Pair<IntersectedWindRange,
     * Double> newWindRangeAndProbability = transitionProbabilitiesCalculator
     * .mergeWindRangeAndGetTransitionProbability(currentNode, currentLevel, currentNodeInfo, nextNode, nextLevel);
     * backwardProbability += nextNode.getConfidence() * newWindRangeAndProbability.getB()
     * bestPathsUntilNextLevel.getNormalizedBackwardProbability(nextNode); }
     * currentNodeInfo.setBackwardProbability(backwardProbability); } nextLevel = currentLevel; } }
     */
    @Override
    public List<GraphLevelInference> getBestNodes(MstManeuverGraphComponents graphComponents) {
        Map<MstGraphLevel, MstBestPathsPerLevel> bestPathsPerLevel = new HashMap<>();
        for (MstGraphLevel currentLevel : graphComponents.getLeafs()) {
            MstBestPathsPerLevel bestPathsUntilLevel = new MstBestPathsPerLevel(currentLevel);
            for (GraphNode currentNode : currentLevel.getLevelNodes()) {
                double probability = currentNode.getConfidence() / currentLevel.getLevelNodes().size();
                /* MstBestManeuverNodeInfo currentNodeInfo = */bestPathsUntilLevel.addBestPreviousNodeInfo(currentNode,
                        null, probability, currentNode.getValidWindRange().toIntersected());
                // currentNodeInfo.setForwardProbability(probability);
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
        /*
         * if (preciseConfidence) { if (!bestPathsUntilLastLevel.isBackwardProbabilitiesComputed()) {
         * computeBackwardProbabilities(); } GraphLevel firstLevel = lastLevel; while (firstLevel.getPreviousLevel() !=
         * null) { firstLevel = firstLevel.getPreviousLevel(); } probabilitiesSum =
         * bestPathsUntilLastLevel.getForwardProbabilitiesSum(); // BestPathsPerLevel firstLevelInfo =
         * bestPathsPerLevel.get(firstLevel); // double backwardProbabilitiesSum = 0.0; // for (GraphNode currentNode :
         * firstLevel.getLevelNodes()) { // backwardProbabilitiesSum += currentNode.getConfidence() /
         * firstLevel.getLevelNodes().size() // *
         * firstLevelInfo.getBestPreviousNodeInfo(currentNode).getBackwardProbability(); // } // double
         * forwardProbabilitiesSum = bestPathsUntilLastLevel.getForwardProbabilitiesSum(); // probabilitiesSum =
         * (forwardProbabilitiesSum + backwardProbabilitiesSum) / 2.0; } else { for (GraphNode node :
         * root.getLevelNodes()) { double probability =
         * bestPathsUntilLevel.getNormalizedProbabilityToNodeFromStart(node); probabilitiesSum += probability; } }
         */
        List<GraphLevelInference> result = new LinkedList<>();
        for (GraphNode lastNode : root.getLevelNodes()) {
            double probability = bestPathsUntilLevel.getNormalizedProbabilityToNodeFromStart(lastNode);
            probabilitiesSum += probability;
            if (maxProbability < probability) {
                maxProbability = probability;
                bestRootNode = lastNode;
            }
        }
        double confidence = maxProbability / probabilitiesSum;
        GraphLevelInference entry = new GraphLevelInference(root, bestRootNode, confidence);
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
                /*
                 * double nodeConfidence; if (preciseConfidence) { nodeConfidence =
                 * currentLevelInfo.getNormalizedForwardBackwardProbability(currentNode); } else { nodeConfidence =
                 * lastNodeProbability / probabilitiesSum; }
                 */
                GraphLevelInference entry = new GraphLevelInference(currentLevel, currentNode, confidence);
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
                // double forwardProbability = 0;
                double bestProbabilityFromStart = 0;
                MstGraphLevel previousLevel = bestPathsUntilPreviousLevel.getCurrentLevel();
                GraphNode bestPreviousNode = null;
                IntersectedWindRange bestIntersectedWindRange = null;
                for (GraphNode previousNode : previousLevel.getLevelNodes()) {
                    IntersectedWindRange previousNodeIntersectedWindRange = bestPathsUntilPreviousLevel
                            .getBestPreviousNodeInfo(previousNode).getIntersectedWindRange();
                    Pair<IntersectedWindRange, Double> newWindRangeAndProbability = transitionProbabilitiesCalculator
                            .mergeWindRangeAndGetTransitionProbability(previousNode, previousLevel,
                                    previousNodeIntersectedWindRange, currentNode, currentLevel);
                    double transitionProbability = newWindRangeAndProbability.getB();
                    // double transitionObservationMultipliedProbability = transitionProbability *
                    // currentNode.getConfidence();
                    double probabilityFromStart = bestPathsUntilPreviousLevel
                            .getNormalizedProbabilityToNodeFromStart(previousNode) * transitionProbability;
                    // forwardProbability += transitionProbability
                    // * bestPathsUntilPreviousLevel.getNormalizedForwardProbability(previousNode);
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
            /* MstBestManeuverNodeInfo currentNodeInfo = */bestPathsUntilLevel.addBestPreviousNodeInfo(currentNode,
                    currentNodeBestPreviousNodes, currentNodeProbabilityFromStart, finalBestIntersectedWindRange);
            // currentNodeInfo.setForwardProbability(forwardProbability);
            bestPathsPerLevel.put(currentLevel, bestPathsUntilLevel);
        }
    }

}
