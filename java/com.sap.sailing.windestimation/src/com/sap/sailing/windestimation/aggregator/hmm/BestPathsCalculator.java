package com.sap.sailing.windestimation.aggregator.hmm;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.sap.sse.common.Util.Pair;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class BestPathsCalculator {

    private GraphLevel lastLevel;
    private Map<GraphLevel, BestPathsPerLevel> bestPathsPerLevel;
    private final boolean preciseConfidence;
    private final GraphNodeTransitionProbabilitiesCalculator transitionProbabilitiesCalculator;

    public BestPathsCalculator(GraphNodeTransitionProbabilitiesCalculator transitionProbabilitiesCalculator) {
        this(true, transitionProbabilitiesCalculator);
    }

    public BestPathsCalculator(boolean preciseConfidence,
            GraphNodeTransitionProbabilitiesCalculator transitionProbabilitiesCalculator) {
        this.preciseConfidence = preciseConfidence;
        this.transitionProbabilitiesCalculator = transitionProbabilitiesCalculator;
    }

    public GraphNodeTransitionProbabilitiesCalculator getTransitionProbabilitiesCalculator() {
        return transitionProbabilitiesCalculator;
    }

    public void computeBestPathsFromScratch() {
        GraphLevel previousLevel = lastLevel;
        if (previousLevel != null) {
            // find first level
            while (previousLevel.getPreviousLevel() != null) {
                previousLevel = previousLevel.getPreviousLevel();
            }
            computeBestPathsFromScratch(previousLevel);
        }
    }

    public void computeBestPathsFromScratch(GraphLevel firstLevel) {
        resetState();
        GraphLevel currentLevel = firstLevel;
        do {
            computeBestPathsToNextLevel(currentLevel);
        } while ((currentLevel = currentLevel.getNextLevel()) != null);
    }

    public void recomputeBestPathsFromLevel(GraphLevel fromLevel) {
        List<GraphLevel> levelsToKeep = new LinkedList<>();
        GraphLevel currentLevel = fromLevel.getPreviousLevel();
        while (currentLevel != null) {
            levelsToKeep.add(currentLevel);
            currentLevel = currentLevel.getPreviousLevel();
        }
        Iterator<Entry<GraphLevel, BestPathsPerLevel>> iterator = bestPathsPerLevel.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<GraphLevel, BestPathsPerLevel> entry = iterator.next();
            if (!levelsToKeep.contains(entry.getKey())) {
                iterator.remove();
            }
        }
        lastLevel = fromLevel.getPreviousLevel();
        currentLevel = fromLevel;
        do {
            computeBestPathsToNextLevel(currentLevel);
        } while (currentLevel != null);
    }

    public void resetState() {
        lastLevel = null;
        bestPathsPerLevel = null;
    }

    public void computeBestPathsToNextLevel(GraphLevel nextLevel) {
        GraphLevel previousLevel = nextLevel.getPreviousLevel();
        if (previousLevel != lastLevel) {
            throw new IllegalArgumentException(
                    "The previous level of next level does not match with the last level processed by this calculator");
        }
        GraphLevel currentLevel = nextLevel;
        if (previousLevel == null) {
            bestPathsPerLevel = new HashMap<>();
            BestPathsPerLevel bestPathsUntilLevel = new BestPathsPerLevel(currentLevel);
            for (GraphNode currentNode : currentLevel.getLevelNodes()) {
                double probability = currentNode.getConfidence() / currentLevel.getLevelNodes().size();
                BestManeuverNodeInfo currentNodeInfo = bestPathsUntilLevel.addBestPreviousNodeInfo(currentNode, null,
                        probability);
                currentNodeInfo.setForwardProbability(probability);
            }
            bestPathsPerLevel.put(currentLevel, bestPathsUntilLevel);
        } else {
            BestPathsPerLevel bestPathsUntilPreviousLevel = bestPathsPerLevel.get(previousLevel);
            BestPathsPerLevel bestPathsUntilLevel = new BestPathsPerLevel(currentLevel);
            for (GraphNode currentNode : currentLevel.getLevelNodes()) {
                double bestProbabilityFromStart = 0;
                double forwardProbability = 0;
                GraphNode bestPreviousNode = null;
                for (GraphNode previousNode : previousLevel.getLevelNodes()) {
                    Pair<IntersectedWindRange, Double> newWindRangeAndProbability = transitionProbabilitiesCalculator
                            .mergeWindRangeAndGetTransitionProbability(previousNode, previousLevel, currentNode,
                                    currentLevel);
                    double transitionObservationMultipliedProbability = newWindRangeAndProbability.getB()
                            * currentNode.getConfidence();
                    double probabilityFromStart = bestPathsUntilPreviousLevel.getNormalizedProbabilityToNodeFromStart(
                            previousNode) * transitionObservationMultipliedProbability;
                    forwardProbability += transitionObservationMultipliedProbability
                            * bestPathsUntilPreviousLevel.getNormalizedForwardProbability(previousNode);
                    if (probabilityFromStart > bestProbabilityFromStart) {
                        bestProbabilityFromStart = probabilityFromStart;
                        bestPreviousNode = previousNode;
                    }
                }
                BestManeuverNodeInfo currentNodeInfo = bestPathsUntilLevel.addBestPreviousNodeInfo(currentNode,
                        bestPreviousNode, bestProbabilityFromStart);
                currentNodeInfo.setForwardProbability(forwardProbability);
            }
            bestPathsPerLevel.put(currentLevel, bestPathsUntilLevel);
        }
        this.lastLevel = currentLevel;
    }

    public List<GraphLevelInference> getBestPath(GraphLevel lastLevel, GraphNode lastNode) {
        double probabilitiesSum = 0;
        BestPathsPerLevel bestPathsUntilLastLevel = bestPathsPerLevel.get(lastLevel);
        double lastNodeProbability = bestPathsUntilLastLevel.getNormalizedProbabilityToNodeFromStart(lastNode);
        if (preciseConfidence) {
            if (!bestPathsUntilLastLevel.isBackwardProbabilitiesComputed()) {
                computeBackwardProbabilities();
            }
            GraphLevel firstLevel = lastLevel;
            while (firstLevel.getPreviousLevel() != null) {
                firstLevel = firstLevel.getPreviousLevel();
            }
            probabilitiesSum = bestPathsUntilLastLevel.getForwardProbabilitiesSum();
            // BestPathsPerLevel firstLevelInfo = bestPathsPerLevel.get(firstLevel);
            // double backwardProbabilitiesSum = 0.0;
            // for (GraphNode currentNode : firstLevel.getLevelNodes()) {
            // backwardProbabilitiesSum += currentNode.getConfidence() / firstLevel.getLevelNodes().size()
            // * firstLevelInfo.getBestPreviousNodeInfo(currentNode).getBackwardProbability();
            // }
            // double forwardProbabilitiesSum = bestPathsUntilLastLevel.getForwardProbabilitiesSum();
            // probabilitiesSum = (forwardProbabilitiesSum + backwardProbabilitiesSum) / 2.0;
        } else {
            for (GraphNode node : lastLevel.getLevelNodes()) {
                double probability = bestPathsUntilLastLevel.getNormalizedProbabilityToNodeFromStart(node);
                probabilitiesSum += probability;
            }
        }
        List<GraphLevelInference> result = new LinkedList<>();
        GraphNode currentNode = lastNode;
        GraphLevel currentLevel = lastLevel;
        while (currentLevel != null) {
            BestPathsPerLevel currentLevelInfo = bestPathsPerLevel.get(currentLevel);
            BestManeuverNodeInfo currentNodeInfo = currentLevelInfo.getBestPreviousNodeInfo(currentNode);
            double nodeConfidence;
            if (preciseConfidence) {
                nodeConfidence = currentLevelInfo.getNormalizedForwardBackwardProbability(currentNode);
            } else {
                nodeConfidence = lastNodeProbability / probabilitiesSum;
            }
            GraphLevelInference entry = new GraphLevelInference(currentLevel, currentNode, nodeConfidence);
            result.add(0, entry);
            currentNode = currentNodeInfo.getBestPreviousNode();
            currentLevel = currentLevel.getPreviousLevel();
        }
        return result;
    }

    public List<GraphLevelInference> getBestPath(GraphLevel lastLevel) {
        BestPathsPerLevel bestPathsUntilLevel = bestPathsPerLevel.get(lastLevel);
        double maxProbability = 0;
        GraphNode bestLastNode = null;
        for (GraphNode lastNode : lastLevel.getLevelNodes()) {
            double probability = bestPathsUntilLevel.getNormalizedProbabilityToNodeFromStart(lastNode);
            if (maxProbability < probability) {
                maxProbability = probability;
                bestLastNode = lastNode;
            }
        }
        return getBestPath(lastLevel, bestLastNode);
    }

    public boolean isPreciseConfidence() {
        return preciseConfidence;
    }

    public void computeBackwardProbabilities() {
        GraphLevel currentLevel = lastLevel;
        BestPathsPerLevel bestPathsUntilLastLevel = bestPathsPerLevel.get(currentLevel);
        for (GraphNode currentNode : currentLevel.getLevelNodes()) {
            // double probability = currentNode.getConfidence();
            BestManeuverNodeInfo currentNodeInfo = bestPathsUntilLastLevel.getBestPreviousNodeInfo(currentNode);
            // Pair<IntersectedWindRange, Double> newWindRangeAndProbability = transitionProbabilitiesCalculator
            // .mergeWindRangeAndGetTransitionProbability(currentNode, currentLevel, currentNodeInfo, nextNode,
            // nextLevel);
            currentNodeInfo.setBackwardProbability(1.0);
        }
        GraphLevel nextLevel = currentLevel;
        while ((currentLevel = currentLevel.getPreviousLevel()) != null) {
            BestPathsPerLevel bestPathsUntilLevel = bestPathsPerLevel.get(currentLevel);
            BestPathsPerLevel bestPathsUntilNextLevel = bestPathsPerLevel.get(nextLevel);
            for (GraphNode currentNode : currentLevel.getLevelNodes()) {
                BestManeuverNodeInfo currentNodeInfo = bestPathsUntilLevel.getBestPreviousNodeInfo(currentNode);
                double backwardProbability = 0;
                for (GraphNode nextNode : nextLevel.getLevelNodes()) {
                    Pair<IntersectedWindRange, Double> newWindRangeAndProbability = transitionProbabilitiesCalculator
                            .mergeWindRangeAndGetTransitionProbability(currentNode, currentLevel, nextNode, nextLevel);
                    backwardProbability += nextNode.getConfidence() * newWindRangeAndProbability.getB()
                            * bestPathsUntilNextLevel.getNormalizedBackwardProbability(nextNode);
                }
                currentNodeInfo.setBackwardProbability(backwardProbability);
            }
            nextLevel = currentLevel;
        }
    }

}
