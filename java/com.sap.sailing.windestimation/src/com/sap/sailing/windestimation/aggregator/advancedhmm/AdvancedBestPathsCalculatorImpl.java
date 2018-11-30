package com.sap.sailing.windestimation.aggregator.advancedhmm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.sap.sailing.windestimation.aggregator.advancedhmm.AdvancedManeuverGraphGenerator.AdvancedManeuverGraphComponents;
import com.sap.sailing.windestimation.aggregator.hmm.GraphLevelInference;
import com.sap.sailing.windestimation.aggregator.hmm.GraphNode;
import com.sap.sailing.windestimation.aggregator.hmm.GraphNodeTransitionProbabilitiesCalculator;
import com.sap.sailing.windestimation.aggregator.hmm.IntersectedWindRange;
import com.sap.sse.common.Util.Pair;

public class AdvancedBestPathsCalculatorImpl implements AdvancedBestPathsCalculator {

    private final GraphNodeTransitionProbabilitiesCalculator transitionProbabilitiesCalculator;

    public AdvancedBestPathsCalculatorImpl(
            GraphNodeTransitionProbabilitiesCalculator transitionProbabilitiesCalculator) {
        this.transitionProbabilitiesCalculator = transitionProbabilitiesCalculator;
    }

    @Override
    public GraphNodeTransitionProbabilitiesCalculator getTransitionProbabilitiesCalculator() {
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
    public List<GraphLevelInference> getBestNodes(AdvancedManeuverGraphComponents graphComponents) {
        Map<AdvancedGraphLevel, AdvancedBestPathsPerLevel> bestPathsPerLevel = new HashMap<>();
        for (AdvancedGraphLevel currentLevel : graphComponents.getLeafs()) {
            AdvancedBestPathsPerLevel bestPathsUntilLevel = new AdvancedBestPathsPerLevel(currentLevel);
            for (GraphNode currentNode : currentLevel.getLevelNodes()) {
                double probability = currentNode.getConfidence() / currentLevel.getLevelNodes().size();
                /* AdvancedBestManeuverNodeInfo currentNodeInfo = */bestPathsUntilLevel
                        .addBestPreviousNodeInfo(currentNode, null, probability);
                // currentNodeInfo.setForwardProbability(probability);
            }
            bestPathsPerLevel.put(currentLevel, bestPathsUntilLevel);
            AdvancedGraphLevel nextLevel = currentLevel;
            while ((nextLevel = nextLevel.getParent()) != null) {
                computeBestPathsToNextLevel(currentLevel.getParent(), bestPathsPerLevel);
            }
        }
        List<GraphLevelInference> inference = inferShortestPath(graphComponents.getRoot(), bestPathsPerLevel);
        return inference;
    }

    private List<GraphLevelInference> inferShortestPath(AdvancedGraphLevel root,
            Map<AdvancedGraphLevel, AdvancedBestPathsPerLevel> bestPathsPerLevel) {
        AdvancedBestPathsPerLevel bestPathsUntilLevel = bestPathsPerLevel.get(root);
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

    private void inferShortestPath(AdvancedGraphLevel lastLevel, GraphNode lastNode, double confidence,
            List<GraphLevelInference> result, Map<AdvancedGraphLevel, AdvancedBestPathsPerLevel> bestPathsPerLevel) {
        if (lastLevel.getChildren().isEmpty()) {
            return;
        }
        AdvancedBestPathsPerLevel lastLevelInfo = bestPathsPerLevel.get(lastLevel);
        AdvancedBestManeuverNodeInfo lastNodeInfo = lastLevelInfo.getBestPreviousNodeInfo(lastNode);
        for (Pair<AdvancedGraphLevel, GraphNode> child : lastNodeInfo.getPreviousGraphLevelsWithBestPreviousNodes()) {
            AdvancedGraphLevel currentLevel = child.getA();
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
                    AdvancedBestPathsPerLevel currentLevelInfo = bestPathsPerLevel.get(currentLevel);
                    AdvancedBestManeuverNodeInfo currentNodeInfo = currentLevelInfo
                            .getBestPreviousNodeInfo(currentNode);
                    currentNode = currentNodeInfo.getPreviousGraphLevelsWithBestPreviousNodes().get(0).getB();
                    currentLevel = currentLevel.getChildren().get(0);
                } else {
                    inferShortestPath(currentLevel, currentNode, confidence, result, bestPathsPerLevel);
                    currentLevel = null;
                }
            }
        }
    }

    private void computeBestPathsToNextLevel(AdvancedGraphLevel nextLevel,
            Map<AdvancedGraphLevel, AdvancedBestPathsPerLevel> bestPathsPerLevel) {
        AdvancedGraphLevel currentLevel = nextLevel;
        // check that all branches until current (joining) node are processed
        List<AdvancedBestPathsPerLevel> bestPathsUntilPreviousLevels = new ArrayList<>();
        for (AdvancedGraphLevel previousLevel : currentLevel.getChildren()) {
            AdvancedBestPathsPerLevel bestPathsUntilPreviousLevel = bestPathsPerLevel.get(previousLevel);
            if (bestPathsUntilPreviousLevel == null) {
                return;
            }
        }
        AdvancedBestPathsPerLevel bestPathsUntilLevel = new AdvancedBestPathsPerLevel(currentLevel);
        for (GraphNode currentNode : currentLevel.getLevelNodes()) {
            List<Pair<AdvancedGraphLevel, GraphNode>> currentNodeBestPreviousNodes = new ArrayList<>();
            double currentNodeProbabilityFromStart = currentNode.getConfidence();
            for (AdvancedBestPathsPerLevel bestPathsUntilPreviousLevel : bestPathsUntilPreviousLevels) {
                // double forwardProbability = 0;
                double bestProbabilityFromStart = 0;
                AdvancedGraphLevel previousLevel = bestPathsUntilPreviousLevel.getCurrentLevel();
                GraphNode bestPreviousNode = null;
                for (GraphNode previousNode : previousLevel.getLevelNodes()) {
                    Pair<IntersectedWindRange, Double> newWindRangeAndProbability = transitionProbabilitiesCalculator
                            .mergeWindRangeAndGetTransitionProbability(previousNode, previousLevel, currentNode,
                                    currentLevel);
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
                    }
                }
                currentNodeProbabilityFromStart *= bestProbabilityFromStart;
                currentNodeBestPreviousNodes.add(new Pair<>(previousLevel, bestPreviousNode));
            }
            /* AdvancedBestManeuverNodeInfo currentNodeInfo = */bestPathsUntilLevel.addBestPreviousNodeInfo(currentNode,
                    currentNodeBestPreviousNodes, currentNodeProbabilityFromStart);
            // currentNodeInfo.setForwardProbability(forwardProbability);
            bestPathsPerLevel.put(currentLevel, bestPathsUntilLevel);
        }
    }

}
