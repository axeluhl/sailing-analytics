package com.sap.sailing.windestimation.aggregator.msthmm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.sailing.windestimation.aggregator.hmm.BestManeuverNodeInfo;
import com.sap.sailing.windestimation.aggregator.hmm.BestPathsPerLevel;
import com.sap.sailing.windestimation.aggregator.hmm.GraphLevel;
import com.sap.sailing.windestimation.aggregator.hmm.GraphLevelInference;
import com.sap.sailing.windestimation.aggregator.hmm.GraphNode;
import com.sap.sailing.windestimation.aggregator.hmm.IntersectedWindRange;
import com.sap.sailing.windestimation.aggregator.msthmm.MstManeuverGraphGenerator.MstManeuverGraphComponents;
import com.sap.sse.common.Util.Pair;

public class MstToSequenceBestPathsCalculatorImpl implements MstBestPathsCalculator {

    private final MstGraphNodeTransitionProbabilitiesCalculator transitionProbabilitiesCalculator;

    public MstToSequenceBestPathsCalculatorImpl(
            MstGraphNodeTransitionProbabilitiesCalculator transitionProbabilitiesCalculator) {
        this.transitionProbabilitiesCalculator = transitionProbabilitiesCalculator;
    }

    @Override
    public MstGraphNodeTransitionProbabilitiesCalculator getTransitionProbabilitiesCalculator() {
        return transitionProbabilitiesCalculator;
    }

    @Override
    public List<GraphLevelInference> getBestNodes(MstManeuverGraphComponents graphComponents) {
        Map<GraphLevel, BestPathsPerLevel> bestPathsPerLevel = new HashMap<>();
        Map<MstGraphLevel, GraphLevel> graphLevelMapping = new HashMap<>();
        for (MstGraphLevel currentMstLevel : graphComponents.getLeafs()) {
            GraphLevel currentLevel = convertToGraphLevel(currentMstLevel, graphLevelMapping);
            BestPathsPerLevel bestPathsUntilLevel = new BestPathsPerLevel(currentLevel);
            for (GraphNode currentNode : currentLevel.getLevelNodes()) {
                double probability = currentNode.getConfidence() / currentLevel.getLevelNodes().size();
                bestPathsUntilLevel.addBestPreviousNodeInfo(currentNode, null, probability,
                        currentNode.getValidWindRange().toIntersected());
            }
            bestPathsPerLevel.put(currentLevel, bestPathsUntilLevel);
            while ((currentMstLevel = currentMstLevel.getParent()) != null
                    && computeBestPathsToNextLevel(currentMstLevel, bestPathsPerLevel, graphLevelMapping))
                ;
        }
        GraphLevel root = graphLevelMapping.get(graphComponents.getRoot());
        List<GraphLevelInference> inference = inferShortestPath(root, bestPathsPerLevel);
        return inference;
    }

    private List<GraphLevelInference> inferShortestPath(GraphLevel root,
            Map<GraphLevel, BestPathsPerLevel> bestPathsPerLevel) {
        BestPathsPerLevel bestPathsUntilLevel = bestPathsPerLevel.get(root);
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
        GraphLevelInference entry = new GraphLevelInference(root, bestRootNode, confidence);
        result.add(entry);
        inferShortestPath(root, bestRootNode, confidence, result, bestPathsPerLevel);
        return result;
    }

    private void inferShortestPath(GraphLevel lastLevel, GraphNode lastNode, double confidence,
            List<GraphLevelInference> result, Map<GraphLevel, BestPathsPerLevel> bestPathsPerLevel) {
        if (lastLevel.getPreviousLevel() == null) {
            return;
        }
        BestPathsPerLevel lastLevelInfo = bestPathsPerLevel.get(lastLevel);
        BestManeuverNodeInfo lastNodeInfo = lastLevelInfo.getBestPreviousNodeInfo(lastNode);
        GraphNode currentNode = lastNodeInfo.getBestPreviousNode();
        GraphLevel currentLevel = lastLevel.getPreviousLevel();
        if (currentLevel.isVirtual()) {
            GraphLevel realCurrentLevelWithNewBranch = currentLevel.getNonVirtualNodeToRepresent();
            GraphLevelInference entry = new GraphLevelInference(realCurrentLevelWithNewBranch, currentNode, confidence);
            result.add(entry);
            inferShortestPath(realCurrentLevelWithNewBranch, currentNode, confidence, result, bestPathsPerLevel);
        } else {
            GraphLevelInference entry = new GraphLevelInference(currentLevel, currentNode, confidence);
            result.add(entry);
        }
        inferShortestPath(currentLevel, currentNode, confidence, result, bestPathsPerLevel);
    }

    private GraphLevel convertToGraphLevel(MstGraphLevel mstGraphLevel,
            Map<MstGraphLevel, GraphLevel> graphLevelMapping) {
        GraphLevel graphLevel = new GraphLevel(mstGraphLevel.getManeuverClassification(),
                mstGraphLevel.getLevelNodes());
        graphLevelMapping.put(mstGraphLevel, graphLevel);
        return graphLevel;
    }

    private boolean computeBestPathsToNextLevel(MstGraphLevel currentMstLevel,
            Map<GraphLevel, BestPathsPerLevel> bestPathsPerLevel, Map<MstGraphLevel, GraphLevel> graphLevelMapping) {
        // check that all branches until current (joining) node are processed
        List<BestPathsPerLevel> bestPathsUntilPreviousLevels = new ArrayList<>();
        for (MstGraphLevel previousMstLevel : currentMstLevel.getChildren()) {
            GraphLevel previousLevel = graphLevelMapping.get(previousMstLevel);
            if (previousLevel == null) {
                return false;
            }
            BestPathsPerLevel bestPathsUntilPreviousLevel = bestPathsPerLevel.get(previousLevel);
            bestPathsUntilPreviousLevels.add(bestPathsUntilPreviousLevel);
        }
        GraphLevel currentLevel = convertToGraphLevel(currentMstLevel, graphLevelMapping);
        if (bestPathsUntilPreviousLevels.size() <= 1) {
            BestPathsPerLevel bestPathsUntilPreviousLevel = bestPathsUntilPreviousLevels.get(0);
            GraphLevel previousLevel = bestPathsUntilPreviousLevel.getCurrentLevel();
            previousLevel.appendNextManeuverNodesLevel(currentLevel);
            computeTransition(bestPathsPerLevel, currentLevel, bestPathsUntilPreviousLevel);
        } else {
            BestPathsPerLevel[] shortestRoute = getShortestRouteThroughCheckpointsToTargetLevel(
                    bestPathsUntilPreviousLevels, currentLevel);
            BestPathsPerLevel previous = shortestRoute[0];
            for (int i = 1; i < shortestRoute.length; i++) {
                BestPathsPerLevel current = shortestRoute[i];
                GraphLevel currentVirtual = new GraphLevel(current);
                previous.getCurrentLevel().appendNextManeuverNodesLevel(currentVirtual);
                computeTransition(bestPathsPerLevel, currentVirtual, previous);
                previous = bestPathsPerLevel.get(currentVirtual);
            }
            previous.getCurrentLevel().appendNextManeuverNodesLevel(currentLevel);
            computeTransition(bestPathsPerLevel, currentLevel, previous);
        }
        return true;
    }

    // TODO make more efficient
    private BestPathsPerLevel[] getShortestRouteThroughCheckpointsToTargetLevel(List<BestPathsPerLevel> checkPoints,
            GraphLevel targetLevel) {
        int n = checkPoints.size();
        BestPathsPerLevel[] elements = checkPoints.toArray(new BestPathsPerLevel[n]);
        int maxIterations = (int) Math.pow(n, n);
        double bestDistance = Double.MAX_VALUE;
        BestPathsPerLevel[] bestSequence = null;
        SKIP: for (int i = 0; i < maxIterations; i++) {
            BestPathsPerLevel[] currentSequence = new BestPathsPerLevel[n];
            for (int j = 0; j < n; j++) {
                int elementIndex = (int) (i / Math.pow(n, j)) % n;
                currentSequence[j] = elements[elementIndex];
            }
            BestPathsPerLevel[] sortedArray = currentSequence.clone();
            Arrays.sort(sortedArray);
            BestPathsPerLevel previous = null;
            for (int k = 0; k < sortedArray.length; k++) {
                BestPathsPerLevel current = sortedArray[k];
                if (current == previous) {
                    continue SKIP;
                }
                previous = current;
            }
            double distanceSum = 0;
            previous = null;
            for (int k = 0; k < currentSequence.length; k++) {
                BestPathsPerLevel current = currentSequence[k];
                if (previous != null) {
                    double distance = transitionProbabilitiesCalculator.getCompoundDistance(
                            previous.getCurrentLevel().getManeuver(), current.getCurrentLevel().getManeuver());
                    distanceSum += distance;
                }
                previous = current;
            }
            double distanceToTarget = transitionProbabilitiesCalculator
                    .getCompoundDistance(previous.getCurrentLevel().getManeuver(), targetLevel.getManeuver());
            distanceSum += distanceToTarget;
            if (distanceSum < bestDistance) {
                bestDistance = distanceSum;
                bestSequence = currentSequence;
            }
        }
        return bestSequence;
    }

    private void computeTransition(Map<GraphLevel, BestPathsPerLevel> bestPathsPerLevel, GraphLevel currentLevel,
            BestPathsPerLevel bestPathsUntilPreviousLevel) {
        GraphLevel previousLevel = bestPathsUntilPreviousLevel.getCurrentLevel();
        BestPathsPerLevel bestPathsUntilLevel = new BestPathsPerLevel(currentLevel);
        for (GraphNode currentNode : currentLevel.getLevelNodes()) {
            double bestProbabilityFromStart = 0;
            GraphNode bestPreviousNode = null;
            IntersectedWindRange bestIntersectedWindRange = null;
            for (GraphNode previousNode : previousLevel.getLevelNodes()) {
                IntersectedWindRange previousNodeIntersectedWindRange = bestPathsUntilPreviousLevel
                        .getBestPreviousNodeInfo(previousNode).getIntersectedWindRange();
                Pair<IntersectedWindRange, Double> newWindRangeAndProbability = transitionProbabilitiesCalculator
                        .mergeWindRangeAndGetTransitionProbability(previousNode, previousLevel,
                                previousNodeIntersectedWindRange, currentNode, currentLevel);
                double transitionObservationMultipliedProbability = newWindRangeAndProbability.getB()
                        * currentNode.getConfidence();
                double probabilityFromStart = bestPathsUntilPreviousLevel.getNormalizedProbabilityToNodeFromStart(
                        previousNode) * transitionObservationMultipliedProbability;
                if (probabilityFromStart > bestProbabilityFromStart) {
                    bestProbabilityFromStart = probabilityFromStart;
                    bestPreviousNode = previousNode;
                    bestIntersectedWindRange = newWindRangeAndProbability.getA();
                }
            }
            bestPathsUntilLevel.addBestPreviousNodeInfo(currentNode, bestPreviousNode, bestProbabilityFromStart,
                    bestIntersectedWindRange);
        }
        bestPathsPerLevel.put(currentLevel, bestPathsUntilLevel);
    }

}
